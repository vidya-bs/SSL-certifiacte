package com.itorix.apiwiz.validator.license.security;

import com.itorix.apiwiz.validator.license.model.ErrorCodes;
import com.itorix.apiwiz.validator.license.model.ItorixException;
import com.itorix.apiwiz.validator.license.util.RSAEncryption;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

@Component
@Slf4j
public class JsessionAuthFilter extends OncePerRequestFilter {

	public final static long MILLIS_PER_DAY = 24 * 60 * 60 * 1000L;

	@Value(value = "${itorix.core.security.apikey}")
	private String apiKey;

	@Value(value = "${itorix.core.security.apikey.update}")
	private String updateKey;


	@Autowired
	private RSAEncryption rsaEncryption;

	private static final String API_KEY_NAME = "x-apikey";


	@Override
	protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
			throws IOException, ServletException {

		String apiKeyHeader = req.getHeader(API_KEY_NAME);

		if (SecurityContextHolder.getContext().getAuthentication() == null) {
			if (StringUtils.hasText(apiKeyHeader)) {
				try {
					if (req.getMethod().equalsIgnoreCase("get")) {
						unSecureCallValidations(apiKeyHeader);
					} else {
						unSecureUpdateCallValidations(apiKeyHeader);
					}
					Authentication authentication = new UsernamePasswordAuthenticationToken("test", null,
							Arrays.asList(new SimpleGrantedAuthority("test")));
					SecurityContextHolder.getContext().setAuthentication(authentication);
				} catch (Exception e) {
					log.error("Exception occurred while validating api token ");
				}

			}

			chain.doFilter(req, res);

		}
	}


	private void unSecureCallValidations(String apiKeyHeader)
			throws Exception {
		apiKeyHeader = this.rsaEncryption.decryptText(apiKeyHeader);
		if(apiKeyHeader == null){
			throw new ItorixException(ErrorCodes.errorMessage.get("License-1003"), "License-1003");
		}else{
			if(apiKeyHeader.equals(rsaEncryption.decryptText(apiKey))){
				return;
			}else{
				throw new ItorixException(ErrorCodes.errorMessage.get("License-1003"), "License-1003");
			}
		}
	}


	private void unSecureUpdateCallValidations(String apiKeyHeader) throws Exception {
		apiKeyHeader = this.rsaEncryption.decryptText(apiKeyHeader);
		if(apiKeyHeader == null){
			throw new ItorixException(ErrorCodes.errorMessage.get("License-1003"), "License-1003");
		}else{
			if(apiKeyHeader.equals(rsaEncryption.decryptText(updateKey))){
				return;
			}else{
				throw new ItorixException(ErrorCodes.errorMessage.get("License-1003"), "License-1003");
			}
		}
	}
}