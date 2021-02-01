package com.itorix.apiwiz.monitor.agent.security;

import java.io.IOException;
import java.util.Arrays;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.itorix.apiwiz.monitor.agent.util.RSAEncryption;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JsessionAuthFilter extends OncePerRequestFilter {

	public final static long MILLIS_PER_DAY = 24 * 60 * 60 * 1000L;

	@Value(value = "${itorix.core.security.apikey}")
	String apiKey;

	@Qualifier("masterMongoTemplate")
	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private RSAEncryption rsaEncryption;

	private static final String API_KEY_NAME = "x-apikey";

	public static final String SESSION_TOKEN_NAME = "JSESSIONID";

	@Override
	protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
			throws IOException, ServletException {

		String apiKeyHeader = req.getHeader(API_KEY_NAME);

		if (SecurityContextHolder.getContext().getAuthentication() == null) {

			try {
				if (StringUtils.hasText(apiKeyHeader) && rsaEncryption.decryptText(apiKey).equals(apiKeyHeader)) {
					Authentication authentication = new UsernamePasswordAuthenticationToken("test", null,
							Arrays.asList(new SimpleGrantedAuthority("test")));
					SecurityContextHolder.getContext().setAuthentication(authentication);
				}
			} catch (Exception e) {
				log.error("error occured during decrypting apikey", e);
			}
		}
		chain.doFilter(req, res);

	}
}