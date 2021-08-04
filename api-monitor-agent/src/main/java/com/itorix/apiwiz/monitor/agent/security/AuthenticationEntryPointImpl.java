package com.itorix.apiwiz.monitor.agent.security;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itorix.apiwiz.monitor.agent.executor.model.ErrorCodes;
import com.itorix.apiwiz.monitor.agent.executor.model.ErrorObj;

@Component
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint {

    public static final String MONITOR_AGENT_2 = "MonitorAgent-2";

    @Override
    public void commence(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
            AuthenticationException e) throws IOException, ServletException {
        // InsufficientAuthenticationException
        ErrorObj error = new ErrorObj();
        error.setErrorMessage(ErrorCodes.errorMessage.get(MONITOR_AGENT_2), MONITOR_AGENT_2);
        httpServletResponse.setStatus(ErrorCodes.responseCode.get(MONITOR_AGENT_2));
        OutputStream out = httpServletResponse.getOutputStream();
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(out, error);
        out.flush();

    }
}