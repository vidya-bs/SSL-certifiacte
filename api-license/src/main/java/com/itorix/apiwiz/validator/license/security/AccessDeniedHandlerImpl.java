package com.itorix.apiwiz.validator.license.security;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itorix.apiwiz.validator.license.model.ErrorCodes;
import com.itorix.apiwiz.validator.license.model.ErrorObj;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

@Component
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {

	public static final String ACCESS_DENIED = "License-1004";

    @Override
    public void handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AccessDeniedException e) throws IOException, ServletException {

        ErrorObj error = new ErrorObj();
		error.setErrorMessage(ErrorCodes.errorMessage.get(ACCESS_DENIED), ACCESS_DENIED);
		httpServletResponse.setStatus(ErrorCodes.responseCode.get(ACCESS_DENIED));
		OutputStream out = httpServletResponse.getOutputStream();
		ObjectMapper mapper = new ObjectMapper();
		mapper.writeValue(out, error);
		out.flush();
    }
}