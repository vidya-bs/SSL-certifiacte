package com.itorix.apiwiz.test.security;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itorix.apiwiz.test.executor.model.ErrorCodes;
import com.itorix.apiwiz.test.executor.model.ErrorObj;

@Component
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint {

	private static final String TEST_SUITE_AGENT_2 = "TestSuiteAgent-2";

	@Override
	public void commence(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
			AuthenticationException e) throws IOException, ServletException {
		//InsufficientAuthenticationException
		ErrorObj error = new ErrorObj();
		error.setErrorMessage(ErrorCodes.errorMessage.get(TEST_SUITE_AGENT_2), TEST_SUITE_AGENT_2);
		httpServletResponse.setStatus(ErrorCodes.responseCode.get(TEST_SUITE_AGENT_2));
		OutputStream out = httpServletResponse.getOutputStream();
		ObjectMapper mapper = new ObjectMapper();
		mapper.writeValue(out, error);
		out.flush();

	}
}