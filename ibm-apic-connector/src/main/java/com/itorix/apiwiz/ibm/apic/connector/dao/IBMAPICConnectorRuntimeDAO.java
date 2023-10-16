package com.itorix.apiwiz.ibm.apic.connector.dao;

import com.itorix.apiwiz.common.util.encryption.RSAEncryption;
import com.itorix.apiwiz.ibm.apic.connector.model.ConnectorCardRequest;
import com.itorix.apiwiz.ibm.apic.connector.model.ConnectorCardResponse;
import com.itorix.apiwiz.identitymanagement.dao.IdentityManagementDao;
import com.itorix.apiwiz.identitymanagement.model.UserSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class IBMAPICConnectorRuntimeDAO {
	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private IdentityManagementDao identityManagementDao;

	@Autowired
	private RSAEncryption rsaEncryption;

	public List<ConnectorCardResponse> getAllConnectors() {
		return mongoTemplate.findAll(ConnectorCardResponse.class);
	}
	public ConnectorCardResponse insertConnector(ConnectorCardRequest connectorCardRequest,String jsessionId)
			throws Exception {

		ConnectorCardResponse connectorCardResponse = new ConnectorCardResponse();
		connectorCardResponse.setOrgName(connectorCardRequest.getOrgName());
		connectorCardResponse.setRegion(connectorCardRequest.getRegion());
		connectorCardResponse.setApiKey(rsaEncryption.encryptText(connectorCardRequest.getApiKey()));
		connectorCardResponse.setClientId(rsaEncryption.encryptText(connectorCardRequest.getClientId()));
		connectorCardResponse.setClientSecret(rsaEncryption.encryptText(connectorCardRequest.getClientSecret()));

		connectorCardResponse.setCts(System.currentTimeMillis());
		connectorCardResponse.setMts(System.currentTimeMillis());
		UserSession session = identityManagementDao.findUserSession(jsessionId);
		String name = session.getFirstName() + " " + session.getLastName();
		connectorCardResponse.setCreatedBy(name);
		connectorCardResponse.setModifiedBy(name);

		return mongoTemplate.save(connectorCardResponse);
	}
	public ConnectorCardResponse updateConnector(ConnectorCardResponse updatedConnectorConfig, String jsessionId)
			throws Exception {
		Query query = Query.query(Criteria.where("_id").is(updatedConnectorConfig.getId()));
		UserSession session = identityManagementDao.findUserSession(jsessionId);
		String name = session.getFirstName() + " " + session.getLastName();
		long modifiedTime = System.currentTimeMillis();

		Update update = new Update()
				.set("orgName", updatedConnectorConfig.getOrgName())
				.set("region", updatedConnectorConfig.getRegion())
				.set("apiKey", rsaEncryption.encryptText(updatedConnectorConfig.getApiKey()))
				.set("clientId", rsaEncryption.encryptText(updatedConnectorConfig.getClientId()))
				.set("clientSecret", rsaEncryption.encryptText(updatedConnectorConfig.getClientSecret()))
				.set("mts", modifiedTime)
				.set("modifiedBy", name);

		mongoTemplate.updateFirst(query, update, ConnectorCardResponse.class);

		updatedConnectorConfig.setModifiedBy(name);
		updatedConnectorConfig.setMts(modifiedTime);

		return updatedConnectorConfig;
	}
	public void deleteConnector(String id) {
		Query query = Query.query(Criteria.where("_id").is(id));
		mongoTemplate.findAndRemove(query,ConnectorCardResponse.class);
	}
}
