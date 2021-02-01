package com.itorix.apiwiz.notification.agent.security;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itorix.apiwiz.notification.agent.executor.model.ErrorCodes;
import com.itorix.apiwiz.notification.agent.executor.model.ErrorObj;

@Component
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {

	public static final String NOTIFICATION_AGENT_2 = "Notification-Agent-1";

    @Override
    public void handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AccessDeniedException e) throws IOException, ServletException {

        ErrorObj error = new ErrorObj();
		error.setErrorMessage(ErrorCodes.errorMessage.get(NOTIFICATION_AGENT_2), NOTIFICATION_AGENT_2);
		httpServletResponse.setStatus(ErrorCodes.responseCode.get(NOTIFICATION_AGENT_2));
		OutputStream out = httpServletResponse.getOutputStream();
		ObjectMapper mapper = new ObjectMapper();
		mapper.writeValue(out, error);
		out.flush();
    }
}