package com.itorix.apiwiz.validator.license.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itorix.apiwiz.validator.license.model.ErrorCodes;
import com.itorix.apiwiz.validator.license.model.ErrorObj;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

@Component
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint {


	public static final String ACCESS_DENIED = "License-1004";



	@Override
	public void commence(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
			AuthenticationException e) throws IOException, ServletException {
		//InsufficientAuthenticationException
			ErrorObj error = new ErrorObj();
			error.setErrorMessage(ErrorCodes.errorMessage.get(ACCESS_DENIED), ACCESS_DENIED);
			httpServletResponse.setStatus(ErrorCodes.responseCode.get(ACCESS_DENIED));
			OutputStream out = httpServletResponse.getOutputStream();
			ObjectMapper mapper = new ObjectMapper();
			mapper.writeValue(out, error);
			out.flush();

	}
}