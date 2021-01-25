package com.itorix.apiwiz.monitor.agent.dao;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.ssl.SSLContextBuilder;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.itorix.apiwiz.monitor.agent.executor.exception.ItorixException;
import com.itorix.apiwiz.monitor.agent.executor.model.ErrorCodes;
import com.itorix.apiwiz.monitor.agent.util.RSAEncryption;
import com.itorix.apiwiz.monitor.model.Certificates;
import com.itorix.apiwiz.monitor.model.Variables;
import com.itorix.apiwiz.monitor.model.collection.MonitorCollections;
import com.itorix.apiwiz.monitor.model.collection.Schedulers;
import com.itorix.apiwiz.monitor.model.execute.ExecutionResult;
import com.itorix.apiwiz.monitor.model.request.MonitorRequest;

@Component
public class MonitorAgentExecutorDao {


	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Value("${server.ssl.key-alias:null}")
	private String keyAlias;

	@Value("${server.ssl.key-store-password:null}")
	private String keyStorepassword;

	@Value("${server.ssl.key-password:null}")
	private String keypassword;

	@Value("${server.ssl.key-store:null}")
	private String keyStoreFilePath;

	@Autowired
	private ResourceLoader resourceLoader;

	private static final Logger log = LoggerFactory.getLogger(MonitorAgentExecutorDao.class);

	public Variables getVariablesById(String id) {
		Query query = new Query(Criteria.where("_id").is(id));
		Variables var = mongoTemplate.findOne(query, Variables.class);
		return var;
	}


	public SSLConnectionSocketFactory getSSLConnectionFactory(String sslReference) throws ItorixException {
		Query query = new Query(Criteria.where("name").is(sslReference));
		Certificates certificate = mongoTemplate.findOne(query, Certificates.class);
		if (certificate != null) {
			try (ByteArrayInputStream instream = new ByteArrayInputStream(certificate.getContent());) {

				Resource storeFile = resourceLoader.getResource(keyStoreFilePath);
				KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
				try {
					trustStore.load(instream, (new RSAEncryption()).decryptText(certificate.getPassword()).toCharArray());
				} catch (Exception e) {
					log.error("Could not load certificate to truststore",e);
					throw new ItorixException(ErrorCodes.errorMessage.get("Testsuite-14"), "Testsuite-14");
				}

				KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
				keyStore.load(storeFile.getInputStream(), keyStorepassword.toCharArray());

				return new SSLConnectionSocketFactory(new SSLContextBuilder()
						.loadTrustMaterial(trustStore, null)
						.loadKeyMaterial(keyStore, keypassword.toCharArray()).build());


			} catch (KeyManagementException | UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException
					| CertificateException | IOException e) {
				log.error("Could not load certificate to truststore",e);
				throw new ItorixException(ErrorCodes.errorMessage.get("Testsuite-14"), "Testsuite-14");
			}
		}
		return null;
	}



	public Schedulers getScheduler(String collectionId, String schedulerId) {

		Query query = new Query(new Criteria().andOperator(Criteria.where("id").is(collectionId),
				Criteria.where("schedulers").elemMatch(Criteria.where("id").is(schedulerId))));
		query.fields().include("schedulers.$");
		MonitorCollections find = mongoTemplate.findOne(query, MonitorCollections.class);
		if (find != null) {
			if (!CollectionUtils.isEmpty(find.getSchedulers())) {
				return find.getSchedulers().get(0);
			}
		}
		return null;
	}

	public List<MonitorRequest> getMonitorRequests(String collectionId) {
		MonitorCollections monitorCollections = mongoTemplate.findById(collectionId, MonitorCollections.class);
		if (monitorCollections != null) {
			return monitorCollections.getMonitorRequest();
		}
		return null;
	}

	public MonitorCollections getMonitorCollections(String collectionId, String schedulerId) {
		Query query = new Query(new Criteria().andOperator(Criteria.where("id").is(collectionId),
				Criteria.where("schedulers").elemMatch(Criteria.where("id").is(schedulerId))));
		query.fields().include("schedulers.$").include("notifications").include("name");
		MonitorCollections collection = mongoTemplate.findOne(query, MonitorCollections.class);
		if (collection != null) {
			return collection;
		}
		return null;
	}


	public void createExecutionResult(ExecutionResult request) {
		request.setId(new ObjectId().toString());
		mongoTemplate.insert(request);
	}


	public List<String> getRequestSequence(String collectionId) {
		List<String> requestSequence = new ArrayList<>();
		Query query = new Query().addCriteria(Criteria.where("id").is(collectionId));
		query.fields().include("sequence");
		MonitorCollections monitorCollection = mongoTemplate.findOne(query, MonitorCollections.class);
		if (monitorCollection != null) {
			requestSequence = monitorCollection.getSequence();
		}
		return requestSequence;
	}
}