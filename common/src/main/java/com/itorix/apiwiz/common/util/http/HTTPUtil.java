package com.itorix.apiwiz.common.util.http;

import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import javax.net.ssl.SSLContext;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class HTTPUtil {
	Logger logger = Logger.getLogger(HTTPUtil.class);

	private RestTemplate restTemplate;

	private ObjectMapper mapper = new ObjectMapper();

	private Object body;
	private String uRL;
	private HttpMethod hTTPMethod;
	private String basicAuth;
	private HttpHeaders headers;

	public HTTPUtil() {
	}

	public HTTPUtil(Object body, String uRL, String basicAuth) {
		this.body = body;
		this.uRL = uRL;
		this.basicAuth = basicAuth;
	}

	public HTTPUtil(String uRL, String basicAuth) {
		this.uRL = uRL;
		this.basicAuth = basicAuth;
	}

	public Object getBody() {
		return body;
	}

	public void setBody(Object body) {
		this.body = body;
	}

	public String getuRL() {
		return uRL;
	}

	public void setuRL(String uRL) {
		this.uRL = uRL;
	}

	public HttpMethod gethTTPMethod() {
		return hTTPMethod;
	}

	public void sethTTPMethod(HttpMethod hTTPMethod) {
		this.hTTPMethod = hTTPMethod;
	}

	public String getBasicAuth() {
		return basicAuth;
	}

	public void setBasicAuth(String basicAuth) {
		this.basicAuth = basicAuth;
	}

	public HttpHeaders getHeaders() {
		return this.headers;
	}

	public void setHeaders(HttpHeaders headers) {
		this.headers = headers;
	}

	public ResponseEntity<String> doGet() {
		this.hTTPMethod = HttpMethod.GET;
		return transport();
	}

	public ResponseEntity<String> doPost() {
		this.hTTPMethod = HttpMethod.POST;
		return transport();
	}

	public ResponseEntity<String> doPut() {
		this.hTTPMethod = HttpMethod.PUT;
		return transport();
	}

	public ResponseEntity<String> doDelete() {
		this.hTTPMethod = HttpMethod.DELETE;
		return transport();
	}

	private ResponseEntity<String> transport() {
		if(this.headers == null)
			this.headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Authorization", this.basicAuth);
		ResponseEntity<String> response = null;
		HttpEntity<Object> httpEntity;
		if (this.hTTPMethod.equals(HttpMethod.GET))
			httpEntity = new HttpEntity<>(headers);
		else
			httpEntity = new HttpEntity<>(this.body, headers);
		logger.debug("HTTPUtil::transport.request::" + getObj(this));
		try {
			restTemplate = this.getRestTemplate();
			response = restTemplate.exchange(this.uRL, hTTPMethod, httpEntity,
					new ParameterizedTypeReference<String>() {
					});
		} catch (Exception e) {
			logger.error("HTTPUtil::transport.response error :: " + getObj(e));
			try {
				throw e;
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		logger.debug("HTTPUtil::transport.response::" + getObj(response));
		return response;
	}

	private String getObj(Object obj) {
		try {
			String out = mapper.writeValueAsString(obj);
			return out;
		} catch (JsonProcessingException e) {
			return "Parse exception";
		}
	}
	
	public RestTemplate getRestTemplate() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
	    TrustStrategy acceptingTrustStrategy = (x509Certificates, s) -> true;
	    SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
	    SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier());
	    CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(csf).build();
	    HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
	    requestFactory.setHttpClient(httpClient);
	    RestTemplate restTemplate = new RestTemplate(requestFactory);
	    return restTemplate;
	}
}
