package com.itorix.apiwiz.consent.management.sched;


import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
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

    private static final String CONSENT_EXPIRY_ENDPOINT = "/v1/consents/expire";

    private static final String API_KEY_NAME = "x-consent-apikey";
    private static final String TENANT_ID = "x-tenant";

    @SneakyThrows
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        log.debug("Invoked Execute to expire consents");
        JobDataMap jobDataMap = jobExecutionContext.getTrigger().getJobDataMap();
        String tenantKey = jobDataMap.getString("tenantKey");
        String publicKey = jobDataMap.getString("publicKey");

        HttpHeaders headers = new HttpHeaders();
        headers.set(TENANT_ID, tenantKey);
        headers.set(API_KEY_NAME, encryptText(tenantKey, publicKey));
        headers.setContentType(MediaType.APPLICATION_JSON);


        HttpEntity<Map<String, String>> httpEntity = new HttpEntity<>(headers);
        String consentServerPath = this.consentServerPath + CONSENT_EXPIRY_ENDPOINT;
        try {
            restTemplate.exchange(consentServerPath, HttpMethod.PATCH, httpEntity, String.class);
        } catch (RestClientException ex) {
            log.error("error returned from consent agent {} ", ex);
        }
    }

    public String encryptText(String msg, String publicKey) throws NoSuchAlgorithmException, InvalidKeySpecException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, NoSuchPaddingException {
        X509EncodedKeySpec ks = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKey.getBytes(StandardCharsets.UTF_8)));
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PublicKey pub = kf.generatePublic(ks);
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(ENCRYPT_MODE, pub);
        return Base64.getEncoder().encodeToString(cipher.doFinal(msg.getBytes("UTF-8")));
    }

}
