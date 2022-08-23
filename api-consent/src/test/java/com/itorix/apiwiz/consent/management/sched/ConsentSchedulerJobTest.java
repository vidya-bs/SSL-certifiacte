package com.itorix.apiwiz.consent.management.sched;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Map;
@Slf4j
public class ConsentSchedulerJobTest {

	ConsentSchedulerJob schedulerJob = new ConsentSchedulerJob();

	// @Test
	public void checkConsentServerEndPoint()
			throws UnsupportedEncodingException, IllegalBlockSizeException, NoSuchPaddingException,
			NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException {
		RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());

		HttpHeaders headers = new HttpHeaders();
		String tenantKey = "b6d4c6bb-446d-47e7-b4e8-64689c3f6f82";
		String publicKey = "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAIIG91vZYfbmEnz35rpdw6EiHz5Us4nYuO68ZW8/KGiTU8rzcPtEavNt6DKKVTR916Rmqh5RiOtMNDv8PobkkvUCAwEAAQ==";
		headers.set("x-tenant", tenantKey);
		headers.set("x-consent-apikey", schedulerJob.encryptText(tenantKey, publicKey));
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<Map<String, String>> httpEntity = new HttpEntity<>(headers);
		String consentServerPath = "https://dev-consent.apiwiz.io/consent-api/v1/consents/expire";
		log.debug("Making a call to {}", consentServerPath);
		ResponseEntity<String> exchange = restTemplate.exchange(consentServerPath, HttpMethod.PATCH, httpEntity,
				String.class);
		log.info(String.valueOf(exchange));
	}

}