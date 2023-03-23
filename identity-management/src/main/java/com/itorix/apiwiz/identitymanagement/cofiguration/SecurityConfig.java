package com.itorix.apiwiz.identitymanagement.cofiguration;

import com.itorix.apiwiz.identitymanagement.helper.IdentityManagementHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	private static Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

	@Autowired
	private JsessionAuthFilter jsessionAuthFilter;

	@Autowired(required = false)
	private SocialLoginSuccessHandler socialLoginSuccessHandler;

	@Autowired
	private OAuth2UserMapper userService;

	@Autowired
	private IdentityManagementHelper identityManagementHelper;

	@Override
	protected void configure(HttpSecurity http) throws Exception {

		if(socialLoginSuccessHandler != null){
			logger.info("Loading Identity Management with Social Login Flow : Enabled");
			http.csrf().disable().authorizeRequests()
					.antMatchers("/v1/users/login", "/registration/**", "/login/**", "/v1/**", "/v2/**","/oauth2-redirect","/social-logins/**")
					.permitAll().anyRequest().authenticated().and().oauth2Login()
					.successHandler(socialLoginSuccessHandler);
		}else{
			logger.info("Loading Identity Management with Social Login Flow : Disabled");
			http.csrf().disable().authorizeRequests()
					.antMatchers("/v1/users/login", "/v1/**", "/v2/**")
					.permitAll().anyRequest().authenticated();
		}

		http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and().cors();
		http.addFilterBefore(jsessionAuthFilter, UsernamePasswordAuthenticationFilter.class);
	}
}
