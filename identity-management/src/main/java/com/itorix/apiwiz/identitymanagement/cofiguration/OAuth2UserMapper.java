package com.itorix.apiwiz.identitymanagement.cofiguration;

import com.itorix.apiwiz.identitymanagement.model.social.MappedOAuth2User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Service
public class OAuth2UserMapper extends DefaultOAuth2UserService  {

	@Autowired
	private RestTemplate restTemplate;

	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		StringBuilder userEmailBuilder = new StringBuilder();

		if(userRequest.getClientRegistration().getRegistrationId().equalsIgnoreCase("github")){
			String token = userRequest.getAccessToken().getTokenValue();
			HttpHeaders headers = new HttpHeaders();
			headers.setBearerAuth(token);

			ResponseEntity<Object> emailFetchUriResponse = restTemplate.exchange("https://api.github.com/user/emails",
					HttpMethod.GET,new HttpEntity<>(headers),Object.class);

			if(emailFetchUriResponse.getStatusCode().is2xxSuccessful()){
				List<LinkedHashMap<String,Object>> emails = (List<LinkedHashMap<String, Object>>) emailFetchUriResponse.getBody();
				{
					for (LinkedHashMap<String, Object> element : emails){
						String email = element.get("email").toString();
						if(!email.contains("noreply.github")){
							userEmailBuilder.append(email);
							break;
						}
					}
				}
			}
		}

		OAuth2User user =  super.loadUser(userRequest);
		MappedOAuth2User mappedOAuth2User = new MappedOAuth2User(user);
		mappedOAuth2User.setEmail(userEmailBuilder.toString());


		return mappedOAuth2User;
	}

}
