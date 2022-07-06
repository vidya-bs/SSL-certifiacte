package com.itorix.apiwiz.common.util.apigeeX;

import java.io.StringReader;
import java.security.PrivateKey;
import java.security.Security;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itorix.apiwiz.common.model.apigeeX.ApigeeXConfigurationVO;
import com.itorix.apiwiz.common.model.apigeeX.ApigeeXEnvironment;
import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

@Component
public class ApigeeXUtill {
	private static final Logger logger = LoggerFactory.getLogger(ApigeeXUtill.class);

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private MongoTemplate mongoTemplate;

	private static final String HOST_URL = "https://apigee.googleapis.com";
	private static final String CLOUD_PLATFORM = "https://www.googleapis.com/auth/cloud-platform";
	
	@Value("${apigeex.host.header:#{null}}")
	private String hostHeader;
	
	static {
		Security.addProvider(new BouncyCastleProvider());
	}
	
	public String getHostHeader() {
		return hostHeader;
	}

	public void setHostHeader(String hostHeader) {
		this.hostHeader = hostHeader;
	}

	public String getApigeeHost(String org) {
		return HOST_URL;
	}

	public String getAccessToken(String jsonKeyStr) throws Exception {
		if (jsonKeyStr == null) {
			throw new ItorixException("missing jsonKey string");
		}
		ObjectMapper mapper = new ObjectMapper();
		JsonNode jsonKey = mapper.readTree(jsonKeyStr);
		JSONObject jsonKey1 = (JSONObject) JSONValue.parseWithException(jsonKeyStr);
		String privateKeyString = (String) jsonKey1.get("private_key");
		if (privateKeyString == null) {
			throw new ItorixException("no service account key provided");
		}

		JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder().issueTime(new Date())
				.expirationTime(getExpiryDate()).claim("scope", CLOUD_PLATFORM)
				.issuer((String) jsonKey.get("client_email").asText())
				.audience((String) jsonKey.get("token_uri").asText());
		SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claimsBuilder.build());
		JWSSigner signer = new RSASSASigner(decodePrivateKey(privateKeyString));
		signedJWT.sign(signer);
		String jwt = signedJWT.serialize();
		String tokenResponse = getToken(jwt);
		// JSONObject payload = (JSONObject)
		// JSONValue.parseWithException(tokenResponse);

		JsonNode payload = mapper.readTree(tokenResponse);
		// System.out.printf(
		// "access_token: %s\n", ((String)
		// (payload.get("access_token"))).replaceAll("\\.+$", ""));
		// System.out.printf("expires_in: %s\n", payload.get("expires_in"));
		return ((String) (payload.get("access_token").asText())).replaceAll("\\.+$", "");
	}

	private PrivateKey decodePrivateKey(String privateKeyString) throws KeyParseException {
		try {
			JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME);
			PEMParser pemParser = new PEMParser(new StringReader(privateKeyString));
			Object object = pemParser.readObject();
			// pemParser.close();
			if (object == null) {
				throw new KeyParseException("unable to read anything when decoding private key");
			}
			if (object instanceof PrivateKeyInfo) {
				return (PrivateKey) converter.getPrivateKey((PrivateKeyInfo) object);
			}
			throw new KeyParseException("unknown object type when decoding private key");
		} catch (KeyParseException exc0) {
			throw exc0;
		} catch (Exception exc1) {
			exc1.printStackTrace();
			throw new KeyParseException("cannot instantiate private key", exc1);
		}
	}

	private Date getExpiryDate() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.SECOND, 120);
		return cal.getTime();
	}

	private String getToken(String assertion) throws Exception {
		String url = "https://oauth2.googleapis.com/token";
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();
		body.add("assertion", assertion);
		body.add("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer");

		HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<MultiValueMap<String, String>>(body,
				headers);
		ResponseEntity<String> response = null;
		try {
			response = restTemplate.exchange(url, HttpMethod.POST, httpEntity,
					new ParameterizedTypeReference<String>() {
					});
		} catch (Exception e) {
			throw e;
		}
		return response.getBody();
	}

	public String getApigeeCredentials(String org, String type) throws Exception {
		Query query = new Query();
		query.addCriteria(Criteria.where("orgName").is(org));
		ApigeeXConfigurationVO configVo = mongoTemplate.findOne(query, ApigeeXConfigurationVO.class);
		return "Bearer " + getAccessToken(configVo.getJsonKey());
	}

	public List<ApigeeXEnvironment> getEnvList(String org, String type) {
		Query query = new Query();
		query.addCriteria(Criteria.where("orgName").is(org));
		ApigeeXConfigurationVO configVo = mongoTemplate.findOne(query, ApigeeXConfigurationVO.class);
		if (configVo != null) {
			return configVo.getEnvironments();
		}
		return null;
	}

	public class KeyParseException extends Exception {
		private static final long serialVersionUID = 0L;

		KeyParseException(String message) {
			super(message);
		}

		KeyParseException(String message, Throwable th) {
			super(message, th);
		}
	}

	public List<String> getEnveronments(String org, String jsonKeyStr) throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		String accessToken = getAccessToken(jsonKeyStr);
		headers.set("Authorization", "Bearer " + accessToken);
		HttpEntity<String> httpEntity = new HttpEntity<String>(headers);
		String url = HOST_URL + "/v1/organizations/" + org + "/environments";
		ResponseEntity<List<String>> response = exchange(url, HttpMethod.GET, httpEntity,
				new ParameterizedTypeReference<List<String>>() {
				});
		return response.getBody();
	}

	private <T> ResponseEntity<T> exchange(String url, HttpMethod method, HttpEntity<?> requestEntity,
			ParameterizedTypeReference<T> responseType, Object... uriVariables) throws ItorixException {
		logger.debug("url = " + url + ": method = " + method + " : requestEntity = " + requestEntity
				+ " : responseType = " + responseType + " : uriVariables = " + uriVariables);

		ResponseEntity<T> response = null;
		try {
			restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
				protected boolean hasError(HttpStatus statusCode) {
					return false;
				}
			});
			response = restTemplate.exchange(url, method, requestEntity, responseType, uriVariables);

		} catch (Exception e) {
			logger.error("url = " + url + " : method = " + method + " : requestEntity = " + requestEntity
					+ " : responseType = " + responseType + " : uriVariables = " + uriVariables + "/n Response : "
					+ response);
			throw new RestClientException(e.getMessage());
		}
		if (response != null) {
			if (response.getStatusCode().value() == 403) {
				throw new ItorixException(ErrorCodes.errorMessage.get("Apigee-1006"), "Apigee-1006");
			} else if (response.getStatusCode().value() == 401) {
				throw new ItorixException(ErrorCodes.errorMessage.get("Apigee-1003"), "Apigee-1003");
			} else if (response.getStatusCode().value() == 500) {
				throw new ItorixException(ErrorCodes.errorMessage.get("Apigee-1002"), "Apigee-1002");
			}
		}
		return response;
	}
}
