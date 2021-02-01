package com.itorix.apiwiz.test.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.itorix.apiwiz.test.security.AccessDeniedHandlerImpl;
import com.itorix.apiwiz.test.security.AuthenticationEntryPointImpl;
import com.itorix.apiwiz.test.security.JsessionAuthFilter;

@EnableWebSecurity
@Configuration
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class SecurityConfig {

	@Autowired
	private JsessionAuthFilter jsessionAuthFilter;

	@Autowired
	private AuthenticationEntryPointImpl authenticationEntryPointImpl;

	@Autowired
	private AccessDeniedHandlerImpl accessDeniedHandler;

	@Configuration
	public class DefaultWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {

		@Override
		protected void configure(HttpSecurity http) throws Exception {

			// @formatter:off
			http.csrf().disable().authorizeRequests().antMatchers("/actuator/**").permitAll().anyRequest().authenticated().and().exceptionHandling()
					.authenticationEntryPoint(authenticationEntryPointImpl).accessDeniedHandler(accessDeniedHandler)
					.and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
			http.addFilterBefore(jsessionAuthFilter, UsernamePasswordAuthenticationFilter.class);

			// @formatter:on
		}
	}

}
