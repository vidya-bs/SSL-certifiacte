package com.itorix.consentserver.dto;


import com.itorix.consentserver.crypto.RSAEncryption;
import com.itorix.consentserver.model.ErrorObj;
import com.itorix.consentserver.model.ItorixException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.InvocationTargetException;

@Aspect
@Component
@Slf4j
public class ServiceMonitor {
    public static final String X_CONSENT_API_KEY = "x-consent-apikey";
    public static Logger LOGGER = LoggerFactory.getLogger(ServiceMonitor.class);
    public static final String X_TENANT_KEY = "x-tenant";
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private HttpServletResponse response;
    @Qualifier("masterMongoTemplate")
    @Autowired
    private MongoTemplate masterMongoTemplate;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private RSAEncryption rsaEncryption;

    @Around("execution(* com.itorix.consentserver.service..*(..))")
    public Object doAccessCheck(ProceedingJoinPoint thisJoinPoint) throws Throwable {
        Object ob = null;
        try {
            populateTenant(thisJoinPoint);
            ServiceRequestContextHolder.getContext().setRequestId(new RequestId());
            ob = thisJoinPoint.proceed();
            return ob;
        } catch (ItorixException ex) {
            throw ex;
        } catch (Throwable ex) {
            ex.printStackTrace();
            if (response.getStatus() == 403) {
                ErrorObj error = new ErrorObj();
                error.setErrorMessage(X_TENANT_KEY + " specified in the header is not valid ", "Identity-1003");
                return error;
            }
            throw ex;
        }
    }

    private String populateTenant(JoinPoint thisJoinPoint)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, ItorixException {
        String key = request.getHeader(X_TENANT_KEY);
        LOGGER.info("Received Request from {} " , request.getRequestURI());
        if (key != null) {
            Workspace workspace = getWorkspaceWithKey(key);
            if (workspace != null) {
                ServiceRequestContext ctx = ServiceRequestContextHolder.getContext();
                ctx.setTenantId(workspace.getTenant());
                Document document = mongoTemplate.findOne(Query.query(Criteria.where("tenantKey").is(key)), Document.class, "Consent.KeyPair");
                String privateKey = document.get("privateKey", String.class);
                String consentApiKey = request.getHeader(X_CONSENT_API_KEY);
                log.debug("Consent API Key {} received for the tenant {} ", consentApiKey, key);
                try {
                    if(consentApiKey == null || !rsaEncryption.decryptText(consentApiKey, privateKey).equals(key)) {
                        throw new ItorixException("Invalid " + X_CONSENT_API_KEY, "Identity-1033");
                    }
                } catch (Exception e) {
                    throw new ItorixException("Invalid " + X_CONSENT_API_KEY, "Identity-1033");
                }

            } else {
                throw new ItorixException("invalid " + X_TENANT_KEY, "Identity-1033");
            }
        } else {
            throw new ItorixException("missing header " + X_TENANT_KEY, "Identity-1034");
        }
        return key;
    }

    private Workspace getWorkspaceWithKey(String workspaceId) {
        Query query = new Query();
        query.addCriteria(new Criteria().orOperator(Criteria.where("key").is(workspaceId)));
        Workspace workspace = masterMongoTemplate.findOne(query, Workspace.class);
        return workspace;
    }
}
