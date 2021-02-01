package com.itorix.apiwiz.sso.handler;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itorix.apiwiz.sso.exception.ErrorCodes;
import com.itorix.apiwiz.sso.exception.ErrorObj;

@Component
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {

	public static final String SSO_1015 = "SSO-1015";

    @Override
    public void handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AccessDeniedException e) throws IOException, ServletException {

        ErrorObj error = new ErrorObj();
		error.setErrorMessage(ErrorCodes.errorMessage.get(SSO_1015), SSO_1015);
		httpServletResponse.setStatus(ErrorCodes.responseCode.get(SSO_1015));
		OutputStream out = httpServletResponse.getOutputStream();
		ObjectMapper mapper = new ObjectMapper();
		mapper.writeValue(out, error);
		out.flush();
    }
}