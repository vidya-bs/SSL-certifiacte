package com.itorix.apiwiz.sso.handler;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itorix.apiwiz.sso.exception.ErrorCodes;
import com.itorix.apiwiz.sso.exception.ErrorObj;

@Component
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint {

    public static final String SSO_2 = "SSO-2";

    @Override
    public void commence(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
            AuthenticationException e) throws IOException, ServletException {
        // InsufficientAuthenticationException
        ErrorObj error = new ErrorObj();
        error.setErrorMessage(ErrorCodes.errorMessage.get(SSO_2), SSO_2);
        httpServletResponse.setStatus(ErrorCodes.responseCode.get(SSO_2));
        OutputStream out = httpServletResponse.getOutputStream();
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(out, error);
        out.flush();

    }
}