package com.itorix.apiwiz.test.security;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itorix.apiwiz.test.executor.model.ErrorCodes;
import com.itorix.apiwiz.test.executor.model.ErrorObj;

@Component
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {

    public static final String TEST_SUITE_AGENT_1 = "TestSuiteAgent-1";

    @Override
    public void handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
            AccessDeniedException e) throws IOException, ServletException {

        ErrorObj error = new ErrorObj();
        error.setErrorMessage(ErrorCodes.errorMessage.get(TEST_SUITE_AGENT_1), TEST_SUITE_AGENT_1);
        httpServletResponse.setStatus(ErrorCodes.responseCode.get(TEST_SUITE_AGENT_1));
        OutputStream out = httpServletResponse.getOutputStream();
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(out, error);
        out.flush();
    }
}