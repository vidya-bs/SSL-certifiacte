package com.itorix.apiwiz.test.security;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.Arrays;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.itorix.apiwiz.test.executor.beans.User;
import com.itorix.apiwiz.test.executor.beans.UserSession;
import com.itorix.apiwiz.test.executor.model.TenantContext;
import com.itorix.apiwiz.test.serviceImpl.TestSuiteAgentServiceImpl;
import com.itorix.apiwiz.test.util.RSAEncryption;

@Component
public class JsessionAuthFilter extends OncePerRequestFilter {

	public final static long MILLIS_PER_DAY = 24 * 60 * 60 * 1000L;

	private static final Logger log = LoggerFactory.getLogger(JsessionAuthFilter.class);

	@Value(value = "${itorix.core.security.apikey}")
	String apiKey;

	private static final String TENANT_ID = "tenantId";



	private static final String API_KEY_NAME = "x-apikey";

	@Qualifier("masterMongoTemplate")
	@Autowired
	private MongoTemplate mongoTemplate;

	public static final String SESSION_TOKEN_NAME = "JSESSIONID";

	@Override
	protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
			throws IOException, ServletException {
		String jsessionId = req.getHeader(SESSION_TOKEN_NAME);

		String apiKeyHeader = req.getHeader(API_KEY_NAME);
		if (jsessionId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
			User user = getUser(req);
			if (user != null) {
				TenantContext.setCurrentTenant(getSession(jsessionId).getWorkspaceId());
				Authentication authentication = new UsernamePasswordAuthenticationToken(user.getLoginId(), null,
						Arrays.asList(new SimpleGrantedAuthority("test")));
				SecurityContextHolder.getContext().setAuthentication(authentication);
			}
		}

		if(!StringUtils.isEmpty(apiKeyHeader)){
			try {
				if (StringUtils.hasText(apiKeyHeader) && new RSAEncryption().decryptText(apiKey).equals(apiKeyHeader)) {
					String tenantName = ((String) req.getHeader(TENANT_ID));
					TenantContext.setCurrentTenant(tenantName);
					Authentication authentication = new UsernamePasswordAuthenticationToken("test", null,
							Arrays.asList(new SimpleGrantedAuthority("test")));
					SecurityContextHolder.getContext().setAuthentication(authentication);
				}
			} catch (Exception e) {
				log.error("error occured during decrypting apikey", e);
			}

		}
		chain.doFilter(req, res);
	}

	private User getUser(HttpServletRequest req) {
		String sessionId = getSessionId(req);
		if (sessionId != null) {
			UserSession userSessionToken = mongoTemplate.findById(sessionId, UserSession.class);
			if (userSessionToken != null) {
				if (System.currentTimeMillis() - userSessionToken.getLoginTimestamp() <= MILLIS_PER_DAY) {
					System.currentTimeMillis();
					return mongoTemplate.findById(userSessionToken.getUserId(), User.class);

				}
			}

		}
		return null;
	}

	private String getSessionId(HttpServletRequest request) {
		String sessionId = request.getHeader(SESSION_TOKEN_NAME);

		if (sessionId == null) {
			Cookie[] cookies = request.getCookies();
			if (null != cookies) {
				for (Cookie cookie : cookies) {
					if (cookie.getName().equals(SESSION_TOKEN_NAME)) {
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

	private UserSession getSession(String sessionId) {
		return mongoTemplate.findById(sessionId, UserSession.class);
	}
}