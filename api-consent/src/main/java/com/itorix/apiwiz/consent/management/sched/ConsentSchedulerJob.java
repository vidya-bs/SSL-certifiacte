package com.itorix.apiwiz.consent.management.sched;

import com.itorix.apiwiz.consent.management.dao.ConsentManagementDao;
import com.itorix.apiwiz.identitymanagement.model.TenantContext;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Map;

import static javax.crypto.Cipher.ENCRYPT_MODE;

@Component
@DisallowConcurrentExecution
@Slf4j
public class ConsentSchedulerJob implements Job {

	@Autowired
	@Value("${itorix.consent.agent.path:null}")
	private String consentServerPath;

	@Qualifier("internalRestTemplate")
	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private ConsentManagementDao consentManagementDao;

	private static final String CONSENT_EXPIRY_ENDPOINT = "/v1/consents/expire";

	private static final String API_KEY_NAME = "x-consent-apikey";
	private static final String TENANT_ID = "x-tenant";

	@SneakyThrows
	@Override
	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
		try {
			JobDataMap jobDataMap = jobExecutionContext.getTrigger().getJobDataMap();
			String tenantId = jobDataMap.getString("tenantId");

			log.debug("Invoked Execute to expire consents for tenant {} ", tenantId);
			TenantContext.setCurrentTenant(tenantId);
			String publicKey = consentManagementDao.getConsentPublicKey();

			if (publicKey == null || "".equals(publicKey)) {
				log.warn("Public key isn't configured for tenant {}. Skipping consent expiry notification", tenantId);
				return;
			}

			String tenantKey = consentManagementDao.getWorkspaceKey(tenantId);

			HttpHeaders headers = new HttpHeaders();
			headers.set(TENANT_ID, tenantKey);
			headers.set(API_KEY_NAME, encryptText(tenantKey, publicKey));
			headers.setContentType(MediaType.APPLICATION_JSON);

			HttpEntity<Map<String, String>> httpEntity = new HttpEntity<>(headers);
			String consentServerPath = this.consentServerPath + CONSENT_EXPIRY_ENDPOINT;
			log.debug("Making a call to {}", consentServerPath);
			ResponseEntity<String> exchange = restTemplate.exchange(consentServerPath, HttpMethod.PATCH, httpEntity,
					String.class);

			log.debug("Successfully invoked expire end point {} {} ", consentServerPath, exchange);

			if (exchange != null && !exchange.getStatusCode().is2xxSuccessful())
				log.debug("Error returned from expire endpoint {} ", exchange.getBody());
		} catch (Exception ex) {
			log.error("error while invoking consent agent {} ", ex);
		}
	}

	public String encryptText(String msg, String publicKey)
			throws NoSuchAlgorithmException, InvalidKeySpecException, UnsupportedEncodingException,
			IllegalBlockSizeException, BadPaddingException, InvalidKeyException, NoSuchPaddingException {
		log.debug("Msg {} and public key {} before encryption", msg, publicKey);
		X509EncodedKeySpec ks = new X509EncodedKeySpec(
				Base64.getDecoder().decode(publicKey.getBytes(StandardCharsets.UTF_8)));
		KeyFactory kf = KeyFactory.getInstance("RSA");
		PublicKey pub = kf.generatePublic(ks);
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(ENCRYPT_MODE, pub);
		String encryptedText = Base64.getEncoder().encodeToString(cipher.doFinal(msg.getBytes("UTF-8")));
		log.debug("Encrypted {}", encryptedText);
		return encryptedText;
	}

	public static void main(String[] args)
			throws UnsupportedEncodingException, IllegalBlockSizeException, NoSuchPaddingException,
			NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException {
		ConsentSchedulerJob schedulerJob = new ConsentSchedulerJob();
		String s = schedulerJob.encryptText("80264bbd-6f76-40c4-8c3a-ac70f8183c26",
				"MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAIIG91vZYfbmEnz35rpdw6EiHz5Us4nYuO68ZW8/KGiTU8rzcPtEavNt6DKKVTR916Rmqh5RiOtMNDv8PobkkvUCAwEAAQ==");
		log.info(s);
	}

}
