package com.itorix.apiwiz.identitymanagement.helper.oauth2;

import com.itorix.apiwiz.common.model.MetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
@ConditionalOnProperty(prefix = "itorix.core.social.login",name="enabled",havingValue = "true")
public class OAuth2Helper {

	private static String GOOGLE = "google";
	private static String GITHUB = "github";
	private static String LINKEDIN="linkedin";

	@Autowired
	@Qualifier("masterMongoTemplate")
	private MongoTemplate masterMongoTemplate;

	@GetMapping("/oauth2-redirect")
	public ResponseEntity<Void> oauth2Redirect(
			@RequestParam(value = "tenant") String tenant,
			@RequestParam(value = "subdomain") String subdomain,
			@RequestParam(value = "provider") String provider) {

		HttpHeaders headers = new HttpHeaders();

		headers.setLocation(
				URI.create(String.format("https://%s/itorix/oauth2/authorization/%s",subdomain, provider)));
		headers.set("requested-tenant", tenant);
		headers.set("Set-Cookie","requested-tenant=" + tenant + "; domain=" + subdomain);
		return new ResponseEntity<>(headers, HttpStatus.FOUND);

	}

	@GetMapping("/social-logins/enabled")
	public ResponseEntity<Object> getEnabledSocialLogins(){
		Query query = new Query();
		query.addCriteria(Criteria.where("key").is("social-logins"));
		Map<String,Boolean> enabledLogins = new HashMap<>();
		MetaData metaData = masterMongoTemplate.findOne(query,MetaData.class);

		if(metaData != null){
			String data = metaData.getMetadata();
			if(data != null){
				List<String> supportedSocialLoginProviders = Arrays.asList(GITHUB,GOOGLE,LINKEDIN);
				for(String provider : supportedSocialLoginProviders){
					enabledLogins.put(provider, data.contains(provider));
				}
			}
		}

		return new ResponseEntity<>(enabledLogins,HttpStatus.OK);
	}

}
