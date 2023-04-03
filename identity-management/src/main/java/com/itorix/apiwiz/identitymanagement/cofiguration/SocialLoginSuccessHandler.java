package com.itorix.apiwiz.identitymanagement.cofiguration;

import com.itorix.apiwiz.identitymanagement.helper.IdentityManagementHelper;
import com.itorix.apiwiz.identitymanagement.model.social.MappedOAuth2User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
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
public class SocialLoginSuccessHandler implements AuthenticationSuccessHandler {

	private static Logger logger = LoggerFactory.getLogger(SocialLoginSuccessHandler.class);

	@Value("${itorix.core.social.login.coreapi.dev.prefix}")
	private String CORE_API_DEV_PREFIX;
	@Value("${itorix.core.social.login.coreapi.stage.prefix}")
	private String CORE_API_STAGE_PREFIX;

	@Value("${itorix.core.social.login.astrum.dev.hostname}")
	private String ASTRUM_DEV_HOSTNAME;
	@Value("${itorix.core.social.login.astrum.stage.hostname}")
	private String ASTRUM_STAGE_HOSTNAME;
	@Value("${itorix.core.social.login.astrum.prod.hostname}")
	private String ASTRUM_PROD_HOSTNAME;
	@Value("${itorix.core.social.login.apiwiz.parent.domain}")
	private String APIWIZ_PARENT_DOMAIN;
	private String REDIRECT = "redirect";

	@Value("${itorix.core.accounts.ui}")
	private String ACCOUNTS_UI;

	@Autowired
	private OAuth2UserMapper userService;

	@Autowired
	private IdentityManagementHelper identityManagementHelper;


	private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {

		String tenant = "";
		String sourceDomain = "";
		Cookie[] cookies = request.getCookies();

		for (int i = 0; i < cookies.length; i++) {
			if (cookies[i].getName().equalsIgnoreCase("requested-tenant")) {
				tenant = cookies[i].getValue();
				sourceDomain = cookies[i].getDomain();
				break;
			}
		}

		logger.info("Social Login Success for tenant:" + tenant);

		MappedOAuth2User mappedOAuth2User = (MappedOAuth2User) authentication.getPrincipal();
		Map<String, Object> oauth2ValidationResults = new HashMap<>();
		oauth2ValidationResults = !tenant.isEmpty() ?
				identityManagementHelper.oauth2Validation(mappedOAuth2User, tenant) :
				oauth2ValidationResults;

		String redirectPath = getRedirectPath(tenant, sourceDomain, oauth2ValidationResults);

		response.sendRedirect(redirectPath);
	}

	private String getRedirectPath(String tenant, String sourceDomain,
			Map<String, Object> oauth2ValidationResults) {

		try {
			String oauth2ValidationError = oauth2ValidationResults.get("oauth2ErrorMessage").toString();
			String oauth2ErrorMessageEncoded = URLEncoder.encode(oauth2ValidationError, "UTF-8");
			String response = ACCOUNTS_UI;

			if (oauth2ErrorMessageEncoded.equalsIgnoreCase("Success")) {
				response = String.format("%s/social-login?j=%s", ACCOUNTS_UI,
						oauth2ValidationResults.get("x-token-v2").toString());
				logger.info("Social Login Success - Redirect Url : " + response);
			} else {
				response = String.format("%s/social-login?error=%s", ACCOUNTS_UI, oauth2ErrorMessageEncoded);
				logger.error("Social Login Error - Redirect Url : " + response);
			}
			return response;
		} catch (Exception ex) {
			try {
				logger.error("Social Login Post Processing Error :" + ex.getMessage());
				return String.format("%s/social-login?error=%s", ACCOUNTS_UI, URLEncoder.encode(
						"Looks like we are unable to service this request at the moment. Please try again later. If the issue persists please contact the application support team",
						"UTF-8"));
			} catch (UnsupportedEncodingException unsupportedEncodingException) {
				return ACCOUNTS_UI;
			}
		}

	}
}
