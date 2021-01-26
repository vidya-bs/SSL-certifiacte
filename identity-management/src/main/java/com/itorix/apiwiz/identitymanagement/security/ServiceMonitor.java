package com.itorix.apiwiz.identitymanagement.security;

import java.lang.reflect.InvocationTargetException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Date;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ErrorObj;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.properties.ApplicationProperties;
import com.itorix.apiwiz.common.util.encryption.RSAEncryption;
import com.itorix.apiwiz.identitymanagement.dao.BaseRepository;
import com.itorix.apiwiz.identitymanagement.logging.LoggerAspect;
import com.itorix.apiwiz.identitymanagement.model.ActivityLog;
import com.itorix.apiwiz.identitymanagement.model.RequestId;
import com.itorix.apiwiz.identitymanagement.model.ServiceRequestContext;
import com.itorix.apiwiz.identitymanagement.model.ServiceRequestContextHolder;
import com.itorix.apiwiz.identitymanagement.model.User;
import com.itorix.apiwiz.identitymanagement.model.UserSession;
import com.itorix.apiwiz.identitymanagement.security.annotation.UnSecure;
import com.itorix.apiwiz.identitymanagement.service.BaseController;

@Aspect
@Component
public class ServiceMonitor extends LoggerAspect {

	public final static long MILLIS_PER_DAY = 24 * 60 * 60 * 1000L;

	@Autowired
	private BaseRepository baseRepository;
	//	@Autowired
	//	private UserSessionRepository userSessionRepository;
	@Autowired
	private HttpServletRequest request;
	@Autowired
	private HttpServletResponse response;
	@Autowired
	private ApplicationProperties applicationProperties;
	@Autowired
	private MongoTemplate mongoTemplate;

	@Qualifier("masterMongoTemplate")
	@Autowired
	private MongoTemplate masterMongoTemplate;

	@Autowired
	private RSAEncryption rsaEncryption;

	@Around("execution(* com.itorix.apiwiz..*.service..*(..))  || execution(* com.itorix.apiwiz..*.serviceImpl..*(..))")
	public Object doAccessCheck(ProceedingJoinPoint thisJoinPoint) throws Throwable {
		Object ob = null;
		try {
			if (isSecureCall(thisJoinPoint)) {
				secureCallValidations(thisJoinPoint);
			} else {
				if (!ignoreUnSecuredValidation(thisJoinPoint)) {
					if(useUpdateKey(thisJoinPoint)){
						unSecureUpdateCallValidations(thisJoinPoint);
					}
					else{
						unSecureCallValidations(thisJoinPoint);
					}
				}
			}
			ServiceRequestContextHolder.getContext().setRequestId(this.createRequestId());
			ob = thisJoinPoint.proceed();
			return ob;
		} catch (ItorixException ex) {
			throw ex;
		} catch (Throwable ex) {
			ex.printStackTrace();
			if (response.getStatus() == 403) {
				ErrorObj error = new ErrorObj();
				error.setErrorMessage(ErrorCodes.errorMessage.get("USER_010"), "USER_010");
				return error;
			}
			throw ex;
		} finally {
			// ServiceRequestContextHolder.setContext(null);
		}
	}

	@After("execution(* com.itorix.apiwiz..*.service..*(..))  || execution(* com.itorix.apiwiz..*.serviceImpl..*(..))")
	public void Activitylog(JoinPoint thisJoinPoint) {
		try {
			Date dateobj = new Date();
			String sessionId = getSessionId(thisJoinPoint);
			// check if session is valid or not
			UserSession userSessionToken = findUserSession(sessionId);
			User dbUser = masterMongoTemplate.findById(userSessionToken.getUserId(), User.class);
			ActivityLog activityLog = new ActivityLog();
			activityLog.setUserId(dbUser.getFirstName() + " " + dbUser.getLastName());
			activityLog.setRequestURI(request.getRequestURI());
			activityLog.setOperation(request.getMethod());
			activityLog.setStatusCode(response.getStatus());
			activityLog.setLast_Changed_At(dateobj.getTime());
			activityLog.setInteractionId(request.getHeader(BaseController.INTERACTION_ID));
			activityLog.setId_user(dbUser.getId());

			if (userSessionToken != null && userSessionToken.getUserId() != null) {
				mongoTemplate.save(activityLog);
			} else {
				System.out.println("NO Activity log as JsessionId is null");
			}
		} catch (Throwable ex) {
		}
	}

	public static ServiceRequestContext getSystemContext() {
		ServiceRequestContext ctx = ServiceRequestContextHolder.getContext();
		UserSession userSessionToken = new UserSession();
		ctx.setUserSessionToken(userSessionToken);
		return ctx;
	}

	private boolean isSecureCall(JoinPoint thisJoinPoint) {
		MethodSignature signature = (MethodSignature) thisJoinPoint.getSignature();
		return signature.getMethod().getAnnotation(UnSecure.class) == null;
	}

	private boolean ignoreUnSecuredValidation(JoinPoint thisJoinPoint){
		MethodSignature signature = (MethodSignature) thisJoinPoint.getSignature();
		return signature.getMethod().getAnnotation(UnSecure.class) != null && signature.getMethod().getAnnotation(UnSecure.class).ignoreValidation();
	}

	private boolean useUpdateKey(JoinPoint thisJoinPoint){
		MethodSignature signature = (MethodSignature) thisJoinPoint.getSignature();
		return signature.getMethod().getAnnotation(UnSecure.class) != null && signature.getMethod().getAnnotation(UnSecure.class).useUpdateKey();
	}
	
	private RequestId createRequestId() {
		return new RequestId();
	}

	// Secure Call Validations
	private void secureCallValidations(JoinPoint thisJoinPoint)
			throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, ItorixException {
		String sessionId = getSessionId(thisJoinPoint);
		if (sessionId == null) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			throw new ItorixException(ErrorCodes.errorMessage.get("Session_02"), "Session_02");
		}
		// check if session is valid or not
		UserSession userSessionToken = findUserSession(sessionId);
		if (userSessionToken == null) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			throw new ItorixException(ErrorCodes.errorMessage.get("Session_02"), "Session_02");
		} else {
			if (System.currentTimeMillis() - userSessionToken.getLoginTimestamp() <= MILLIS_PER_DAY) {
				//if(userSessionToken.getStatus().equalsIgnoreCase("active")){
				User user = masterMongoTemplate.findById(userSessionToken.getUserId(), User.class);
				userSessionToken.setUser(user);
				ServiceRequestContext ctx = ServiceRequestContextHolder.getContext();
				ctx.setUserSessionToken(userSessionToken);
				//				}else{
				//					response.setStatus(HttpServletResponse.SC_FORBIDDEN);
				//					throw new ItorixException(ErrorCodes.errorMessage.get("Session_01"), "Session_01");
				//				}
			} else {
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
				throw new ItorixException(ErrorCodes.errorMessage.get("Session_01"), "Session_01");
			}
		}
	}

	// Unsecure Call Validations
	private void unSecureCallValidations(JoinPoint thisJoinPoint)
			throws Exception {
		String apiKey = getSessionAPIKey(thisJoinPoint);
		apiKey = this.rsaEncryption.decryptText(apiKey);
		if(apiKey == null){
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			throw new ItorixException(ErrorCodes.errorMessage.get("Session_02"), "Session_02");
		}else{
			if(apiKey.equals(rsaEncryption.decryptText(applicationProperties.getApiKey()))){
					ServiceRequestContextHolder.setContext(getSystemContext());
			}else{
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				throw new ItorixException(ErrorCodes.errorMessage.get("Session_03"), "Session_03");
			}
		}
	}


	private void unSecureUpdateCallValidations(JoinPoint thisJoinPoint) throws Exception {
		String apiKey = getSessionAPIKey(thisJoinPoint);
		apiKey = this.rsaEncryption.decryptText(apiKey);
		if(apiKey == null){
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			throw new ItorixException(ErrorCodes.errorMessage.get("Session_02"), "Session_02");
		}else{
			String key = applicationProperties.getUpdateApiKey();
			if(key == null)
				key = applicationProperties.getApiKey();
			if(apiKey.equals(rsaEncryption.decryptText(key))){
				ServiceRequestContextHolder.setContext(getSystemContext());
			}else{
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				throw new ItorixException(ErrorCodes.errorMessage.get("Session_03"), "Session_03");
			}
		}
	}

	private String getSessionId(JoinPoint thisJoinPoint)
			throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		String sessionId = null;
		// try to get value of session token from headers
		if (sessionId == null) {
			sessionId = request.getHeader(BaseController.SESSION_TOKEN_NAME);
		}
		// try to get value of session token from cookies if not found in
		// headers
		if (sessionId == null) {
			Cookie[] cookies = request.getCookies();
			if (null != cookies) {
				for (Cookie cookie : cookies) {
					if (cookie.getName().equals(BaseController.SESSION_TOKEN_NAME)) {
						sessionId = cookie.getValue();
						break;
					}
				}
			}
		}
		if (sessionId != null) {
			sessionId = getDecodeValue(sessionId);
		}
		return sessionId;
	}

	private String getSessionAPIKey(JoinPoint thisJoinPoint)
			throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		String apiKey = null;
		// try to get value of session token from headers
		if (apiKey == null) {
			apiKey = request.getHeader(BaseController.API_KEY_NAME);

		}
		// try to get value of session token from cookies if not found in
		// headers
		if (apiKey == null) {
			Cookie[] cookies = request.getCookies();
			if (null != cookies) {
				for (Cookie cookie : cookies) {
					if (cookie.getName().equals(BaseController.API_KEY_NAME)) {
						apiKey = cookie.getValue();
						break;
					}
				}
			}
		}
		if (apiKey != null) {
			apiKey = getDecodeValue(apiKey);
		}
		return apiKey;
	}

	private String getDecodeValue(String value) {
		String decodeValue = null;
		try {
			if (value != null) {
				decodeValue = URLDecoder.decode(value, "UTF-8");
			}
		} catch (Exception e) {
		}
		return decodeValue;
	}

	private UserSession findUserSession(String sessionId) {
		Query query  = new Query().addCriteria(new Criteria().orOperator(Criteria.where("id").is(sessionId)));
		return masterMongoTemplate.findOne(query, UserSession.class);
		//return  userSessionRepository.findOne(sessionId);
	}

}
