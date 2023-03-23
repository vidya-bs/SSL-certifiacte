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

		String hostname = getRedirectHostName(tenant, sourceDomain, oauth2ValidationResults);

		String redirectPath = String.format("https://%s.%s/", hostname, APIWIZ_PARENT_DOMAIN);
		for (String key : oauth2ValidationResults.keySet()) {
			if(!key.equalsIgnoreCase(REDIRECT)){
				Cookie cookie = new Cookie(key, URLEncoder.encode(oauth2ValidationResults.get(key).toString(), "UTF-8"));
				cookie.setMaxAge(3600);
				cookie.setPath("/");
				cookie.setDomain(APIWIZ_PARENT_DOMAIN);
				response.addCookie(cookie);
			}
		}

		response.sendRedirect(redirectPath);
	}

	private String getRedirectHostName(String tenant, String sourceDomain,
			Map<String, Object> oauth2ValidationResults) {
		if (oauth2ValidationResults.containsKey(REDIRECT) && oauth2ValidationResults.get(REDIRECT)
				.equals("astrum")) {
			if (sourceDomain.contains(CORE_API_DEV_PREFIX)) {
				return ASTRUM_DEV_HOSTNAME;
			} else if (sourceDomain.contains(CORE_API_STAGE_PREFIX)) {
				return ASTRUM_STAGE_HOSTNAME;
			} else {
				return ASTRUM_PROD_HOSTNAME;
			}
		}

		return tenant;
	}
}
