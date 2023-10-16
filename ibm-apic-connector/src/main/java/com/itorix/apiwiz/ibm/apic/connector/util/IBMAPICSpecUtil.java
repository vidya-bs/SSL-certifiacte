package com.itorix.apiwiz.ibm.apic.connector.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itorix.apiwiz.common.model.designstudio.Swagger3VO;
import com.itorix.apiwiz.common.model.designstudio.SwaggerVO;
import com.itorix.apiwiz.common.util.encryption.RSAEncryption;
import com.itorix.apiwiz.ibm.apic.connector.model.ConnectorCardResponse;
import com.itorix.apiwiz.ibm.apic.connector.serviceImpl.ConnectorCardServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;
public final class IBMAPICSpecUtil {

	private static final Logger logger = LoggerFactory.getLogger(IBMAPICSpecUtil.class);

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private RSAEncryption rsaEncryption;

	@Autowired
	private RestTemplate restTemplate;

	public void importSpecsFromIBMAPIC(String orgName) throws Exception {
		Query query = Query.query(Criteria.where("orgName").is(orgName));
		ConnectorCardResponse connectorCardResponse = mongoTemplate.findOne(query,ConnectorCardResponse.class);

		String region = connectorCardResponse.getRegion();
		String apiKey = rsaEncryption.decryptText(connectorCardResponse.getApiKey());
		String clientId = rsaEncryption.decryptText(connectorCardResponse.getClientId());
		String clientSecret = rsaEncryption.decryptText(connectorCardResponse.getClientSecret());

		//Get Access Token
		String tokenUrl = String.format("https://api-manager.%s.apiconnect.automation.ibm.com/api/token",region);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

		// Create the request body with your API key, client ID, and client secret
		Map<String, String> requestBody = new HashMap<>();
		requestBody.put("api_key", apiKey);
		requestBody.put("client_id", clientId);
		requestBody.put("client_secret", clientSecret);
		requestBody.put("grant_type", "api_key");

		HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

		try{
			ObjectMapper jsonMapper = new ObjectMapper();
			ResponseEntity<Map<String, Object>> responseEntity = restTemplate.exchange(tokenUrl, HttpMethod.POST, request, new ParameterizedTypeReference<Map<String, Object>>() {});
			if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
				String accessToken = responseEntity.getBody().get("access_token").toString();

				//Get All Catalogs from IBM APIC Org
				try{
					HttpHeaders catalogHeaders = new HttpHeaders();
					catalogHeaders.setContentType(MediaType.APPLICATION_JSON);
					catalogHeaders.set("Authorization", "Bearer " + accessToken);

					HttpEntity<Map<String, String>> catalogRequestHeaders = new HttpEntity<>(catalogHeaders);

					String catalogUrl = String.format("https://api-manager.%s.apiconnect.automation.ibm.com/api/catalogs",region);
					ResponseEntity<Map<String, Object>> catalogResponseEntity = restTemplate.exchange(catalogUrl, HttpMethod.GET, catalogRequestHeaders, new ParameterizedTypeReference<Map<String, Object>>() {});

					if (catalogResponseEntity.getStatusCode().is2xxSuccessful()) {
						Map<String, Object> responseBody = catalogResponseEntity.getBody();
						List<Map<String, String>> catalogList = new ArrayList<>();

						if (responseBody.containsKey("results")) {
							List<Map<String, Object>> results = (List<Map<String, Object>>) responseBody.get("results");

							for (Map<String, Object> result : results) {
								Map<String, String> catalogInfo = new HashMap<>();
								catalogInfo.put("name", result.get("name").toString());

								// Extract orgId from the org_url field
								String orgUrl = result.get("org_url").toString();
								String[] parts = orgUrl.split("/");
								String orgId = parts[parts.length - 1];
								catalogInfo.put("orgId", orgId);

								catalogList.add(catalogInfo);
							}
						}

						//Get All OAS from each Catalog
						catalogList.forEach(catalog->{
							try{
								String oasListUrl = String.format("https://api-manager.%s.apiconnect.automation.ibm.com/api/catalogs/%s/production/apis",region,catalog.get("orgId"));
								ResponseEntity<Map<String, Object>> oasListResponseEntity = restTemplate.exchange(oasListUrl, HttpMethod.GET, catalogRequestHeaders, new ParameterizedTypeReference<Map<String, Object>>() {});

								if (oasListResponseEntity.getStatusCode().is2xxSuccessful()) {
									Map<String, Object> oasListResponseBody = oasListResponseEntity.getBody();
									if (oasListResponseBody.containsKey("results")) {
										List<Map<String, Object>> oasResults = (List<Map<String, Object>>) oasListResponseBody.get("results");
										for (Map<String, Object> oas : oasResults){
											String ibmSpecName = oas.get("name").toString();
											String ibmSpecVersion = oas.get("version").toString();
											try{
												//Use the SpecName and Version to Get the OAS Spec
												String oasUrl = String.format("https://api-manager.%s.apiconnect.automation.ibm.com/api/catalogs/%s/%s/apis/%s/%s?fields=add(catalog_api)",region,catalog.get("orgId"),catalog.get("name"),ibmSpecName,ibmSpecVersion);
												ResponseEntity<Map<String, Object>> oasResponseEntity = restTemplate.exchange(oasUrl, HttpMethod.GET, catalogRequestHeaders, new ParameterizedTypeReference<Map<String, Object>>() {});

												if (oasResponseEntity.getStatusCode().is2xxSuccessful()){
													Map<String,Object> oasObjectResponseBody = oasResponseEntity.getBody();
													String oasSpecJson = jsonMapper.writeValueAsString(oasObjectResponseBody.get("catalog_api"));
													if(oasObjectResponseBody.get("oai_version").toString().equalsIgnoreCase("openapi2")){

														try{
															//Save OAS2 to Design Studio in Approved State
															SwaggerVO oas2Spec = new SwaggerVO();
															oas2Spec.setSwaggerId(UUID.randomUUID().toString().replaceAll("-", ""));
															oas2Spec.setSwagger(oasSpecJson);
															oas2Spec.setLock(false);
															oas2Spec.setName(ibmSpecName);
															oas2Spec.setStatus("Approved");
															oas2Spec.setCts(System.currentTimeMillis());
															oas2Spec.setMts(System.currentTimeMillis());
															oas2Spec.setCreatedBy("System");
															oas2Spec.setModifiedBy("System");

															Query numSpecsWithSameNameQuery = new Query(Criteria.where("name").is(ibmSpecName));
															long numSpecsWithSameName = mongoTemplate.count(numSpecsWithSameNameQuery,SwaggerVO.class);
															oas2Spec.setRevision((int) (numSpecsWithSameName + 1));
															mongoTemplate.save(oas2Spec);
														}catch (Exception saveOAS2ToDesignStudioException){
															logger.error(String.format("Could Not Save OAS2 Spec... Skipping it [name=%s, version=%s, catalog_name=%s]:%s",ibmSpecName,ibmSpecVersion,catalog.get("name"),saveOAS2ToDesignStudioException.getMessage()));
														}

													}else{

														try{
															//Save OAS3 to Design Studio in Approved State
															Swagger3VO oas3Spec = new Swagger3VO();
															oas3Spec.setSwaggerId(UUID.randomUUID().toString().replaceAll("-", ""));
															oas3Spec.setSwagger(oasSpecJson);
															oas3Spec.setLock(false);
															oas3Spec.setName(ibmSpecName);
															oas3Spec.setStatus("Approved");
															oas3Spec.setCts(System.currentTimeMillis());
															oas3Spec.setMts(System.currentTimeMillis());
															oas3Spec.setCreatedBy("System");
															oas3Spec.setModifiedBy("System");

															Query numSpecsWithSameNameQuery = new Query(Criteria.where("name").is(ibmSpecName));
															long numSpecsWithSameName = mongoTemplate.count(numSpecsWithSameNameQuery,Swagger3VO.class);
															oas3Spec.setRevision((int) (numSpecsWithSameName + 1));
															mongoTemplate.save(oas3Spec);
														}catch (Exception saveOAS3ToDesignStudioException){
															logger.error(String.format("Could Not Save OAS3 Spec... Skipping it [name=%s, version=%s, catalog_name=%s]:%s",ibmSpecName,ibmSpecVersion,catalog.get("name"),saveOAS3ToDesignStudioException.getMessage()));
														}

													}
												}
											}catch (Exception fetchOASException){
												logger.error(String.format("Could Not Retrieve IBM APIC OAS Spec... Skipping it [name=%s, version=%s, catalog_name=%s]:%s",ibmSpecName,ibmSpecVersion,catalog.get("name"),fetchOASException.getMessage()));
											}
										}
									}
								}
							}catch (Exception fetchOASListException){
								logger.error("Could Not Retrieve IBM APIC OASList:" + fetchOASListException.getMessage());
							}
						});

						// Import All OAS into Design Studio in 'Approved' state, so it can be used in deployments
					}
				}catch (Exception catalogFetchException){
					logger.error("Could Not Retrieve IBM APIC Catalogs:" + catalogFetchException.getMessage());
				}
			}
		}catch (Exception tokenException){
			logger.error("Could Not Retrieve Access Token:" + tokenException.getMessage());
		}
	}
}
