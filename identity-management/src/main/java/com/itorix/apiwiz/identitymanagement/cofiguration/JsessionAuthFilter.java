package com.itorix.apiwiz.identitymanagement.cofiguration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.itorix.apiwiz.identitymanagement.model.UserSession;

@Component
public class JsessionAuthFilter extends OncePerRequestFilter {

	private static final Logger log = LoggerFactory.getLogger(JsessionAuthFilter.class);

	@Qualifier("masterMongoTemplate")
	@Autowired
	private MongoTemplate mongoTemplate;

	public static final String SESSION_TOKEN_NAME = "JSESSIONID";

	@Override
	protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
			throws IOException, ServletException {
		String apiKeyHeader = req.getHeader(SESSION_TOKEN_NAME);
		if (!StringUtils.isEmpty(apiKeyHeader)) {
			try {
				UserSession session = findUserSession(apiKeyHeader);
				if (null != session && null != session.getRoles()) {
					List<GrantedAuthority> authorities = new ArrayList<>();
					for (String role : session.getRoles())
						authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
					if (null != session.getUserType() && session.getUserType().equalsIgnoreCase("SITE_ADMIN"))
						authorities.add(new SimpleGrantedAuthority("ROLE_SITE_ADMIN"));
					authorities.add(new SimpleGrantedAuthority(session.getPlanId().toUpperCase()));
					Authentication authentication = new UsernamePasswordAuthenticationToken(
							"ROLE_" + session.getPlanId().toUpperCase(), null, authorities);
					SecurityContextHolder.getContext().setAuthentication(authentication);
				} else {
					List<GrantedAuthority> authorities = new ArrayList<>();
					Authentication authentication = new UsernamePasswordAuthenticationToken("", null, authorities);
					SecurityContextHolder.getContext().setAuthentication(authentication);
				}
			} catch (Exception e) {
				log.error("error occured durnig setup of authentication ", e);
			}
		}
		chain.doFilter(req, res);
	}

	private UserSession findUserSession(String sessionId) {
		Query query = new Query().addCriteria(new Criteria().orOperator(Criteria.where("id").is(sessionId)));
		return mongoTemplate.findOne(query, UserSession.class);
	}
}
