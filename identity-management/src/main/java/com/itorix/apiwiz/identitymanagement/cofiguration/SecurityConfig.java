package com.itorix.apiwiz.identitymanagement.cofiguration;

import com.itorix.apiwiz.identitymanagement.helper.IdentityManagementHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	private static Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

	@Autowired
	private JsessionAuthFilter jsessionAuthFilter;

	@Autowired(required = false)
	private SocialLoginSuccessHandler socialLoginSuccessHandler;

	@Autowired(required = false)
	private SocialLoginFailureHandler socialLoginFailureHandler;

	@Autowired
	private OAuth2UserMapper userService;

	@Autowired
	private IdentityManagementHelper identityManagementHelper;

	@Value("${itorix.core.accounts.ui}")
	private String ACCOUNTS_UI;

	@Override
	protected void configure(HttpSecurity http) throws Exception {

		if(socialLoginSuccessHandler != null){
			logger.info("Loading Identity Management with Social Login Flow : Enabled");
			http.csrf().disable().authorizeRequests()
					.antMatchers("/v1/users/login", "/registration/**", "/login/**", "/v1/**", "/v2/**","/oauth2-redirect","/social-logins/**","/actuator/**","/oauth2/**")
					.permitAll().anyRequest().authenticated().and()
					.oauth2Login()
					.tokenEndpoint()
					.accessTokenResponseClient(authorizationCodeTokenResponseClient()).and()
					.successHandler(socialLoginSuccessHandler)
					.failureHandler(socialLoginFailureHandler);
			http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
		}else{
			logger.info("Loading Identity Management with Social Login Flow : Disabled");
			http.csrf().disable().authorizeRequests()
					.antMatchers("/v1/users/login", "/v1/**", "/v2/**")
					.permitAll().anyRequest().authenticated();
			http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
		}

		
		http.addFilterBefore(jsessionAuthFilter, UsernamePasswordAuthenticationFilter.class);
	}
	private OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> authorizationCodeTokenResponseClient() {
		OAuth2AccessTokenResponseHttpMessageConverter tokenResponseHttpMessageConverter =
				new OAuth2AccessTokenResponseHttpMessageConverter();
		tokenResponseHttpMessageConverter.setTokenResponseConverter(new CustomAccessTokenResponseConvertor());

		RestTemplate restTemplate = new RestTemplate(Arrays.asList(
				new FormHttpMessageConverter(), tokenResponseHttpMessageConverter));
		restTemplate.setErrorHandler(new OAuth2ErrorResponseErrorHandler());

		DefaultAuthorizationCodeTokenResponseClient tokenResponseClient = new DefaultAuthorizationCodeTokenResponseClient();
		tokenResponseClient.setRestOperations(restTemplate);

		return tokenResponseClient;
	}
}
