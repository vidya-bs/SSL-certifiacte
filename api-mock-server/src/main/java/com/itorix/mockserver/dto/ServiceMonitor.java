package com.itorix.mockserver.dto;

import com.itorix.mockserver.common.model.ErrorObj;
import com.itorix.mockserver.common.model.ItorixException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
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
public class ServiceMonitor {
    public static Logger LOGGER = LoggerFactory.getLogger(ServiceMonitor.class);
    public static final String SESSION_TOKEN_NAME = "x-tenant";

    @Autowired
    private HttpServletRequest request;
    @Autowired
    private HttpServletResponse response;
    @Qualifier("masterMongoTemplate")
    @Autowired
    private MongoTemplate masterMongoTemplate;

    @Around("execution(* com.itorix.mockserver.controller..*(..))")
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
            LOGGER.error("Exception occurred",ex);
            if (response.getStatus() == 403) {
                ErrorObj error = new ErrorObj();
                error.setErrorMessage(SESSION_TOKEN_NAME + " specified in the header is not valid ", "Identity-1003");
                return error;
            }
            throw ex;
        }
    }

    private String populateTenant(JoinPoint thisJoinPoint)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, ItorixException {
        String key = request.getHeader(SESSION_TOKEN_NAME);
        LOGGER.info("Received Request from {} " , request.getRequestURI());
        if (key != null) {
            Workspace workspace = getWorkspaceWithKey(key);
            if (workspace != null) {
                ServiceRequestContext ctx = ServiceRequestContextHolder.getContext();
                ctx.setTenantId(workspace.getTenant());
            } else {
                throw new ItorixException("invalid " + SESSION_TOKEN_NAME, "Identity-1033");
            }
        } else {
            throw new ItorixException("missing header " + SESSION_TOKEN_NAME, "Identity-1034");
        }
        return key;
    }

    private Workspace getWorkspaceWithKey(String workapaceId) {
        Query query = new Query();
        query.addCriteria(new Criteria().orOperator(Criteria.where("key").is(workapaceId)));
        Workspace workspace = masterMongoTemplate.findOne(query, Workspace.class);
        return workspace;
    }
}
