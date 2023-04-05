package com.itorix.apiwiz.identitymanagement.cofiguration;

import com.itorix.apiwiz.identitymanagement.helper.IdentityManagementHelper;
import com.itorix.apiwiz.identitymanagement.model.social.MappedOAuth2User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

@Component
@ConditionalOnProperty(prefix = "itorix.core.social.login",name="enabled",havingValue = "true")
public class SocialLoginFailureHandler implements AuthenticationFailureHandler {

	private static Logger logger = LoggerFactory.getLogger(SocialLoginFailureHandler.class);

	@Value("${itorix.core.accounts.ui}")
	private String ACCOUNTS_UI;


	@Override public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException {
		logger.error("Social Login Auth Failure" + exception.getMessage());
		logger.error("Trace:" + exception.getStackTrace().toString());

		response.sendRedirect(String.format("%s/social-login?error=%s", ACCOUNTS_UI,"AuthenticationFailed"));
	}
}
