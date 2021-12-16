package com.itorix.apiwiz.consent.management.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.security.KeyStore;

@Slf4j
@Configuration
public class ConsentSchedulerConfig {

    @Autowired
    private ResourceLoader resourceLoader;

    @Value("${server.ssl.key-store-password:null}")
    private String keyStorepassword;

    @Value("${server.ssl.key-password:null}")
    private String keypassword;

    @Value("${server.ssl.key-store:null}")
    private String keyStoreFilePath;

    @Bean(name="internalRestTemplate")
    public RestTemplate internalRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        KeyStore keyStore;
        HttpComponentsClientHttpRequestFactory requestFactory = null;

        try {
            keyStore = KeyStore.getInstance("jks");
            Resource storeFile = resourceLoader.getResource(keyStoreFilePath);
            keyStore.load(storeFile.getInputStream(), keyStorepassword.toCharArray());
            SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(
                    new SSLContextBuilder().loadTrustMaterial(null, new TrustSelfSignedStrategy())
                            .loadKeyMaterial(keyStore, keypassword.toCharArray()).build(),
                    NoopHostnameVerifier.INSTANCE);
            HttpClient httpClient = HttpClients.custom().setSSLSocketFactory(socketFactory).build();
            requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
            restTemplate.setRequestFactory(requestFactory);
            return restTemplate;
        } catch (Exception exception) {
            return new RestTemplate(new HttpComponentsClientHttpRequestFactory());
        }
    }

}
