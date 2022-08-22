package com.itorix.apiwiz.test.dao;

import com.itorix.apiwiz.test.db.TestExecutorEntity;
import com.itorix.apiwiz.test.executor.beans.*;
import com.itorix.apiwiz.test.executor.exception.ItorixException;
import com.itorix.apiwiz.test.executor.model.DashboardStats;
import com.itorix.apiwiz.test.executor.model.DashboardSummary;
import com.itorix.apiwiz.test.executor.model.ErrorCodes;
import com.itorix.apiwiz.test.util.RSAEncryption;
import com.mongodb.client.result.UpdateResult;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.ssl.SSLContextBuilder;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.List;

@Component
public class TestSuiteExecutorDao {

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


	private static final Logger log = LoggerFactory.getLogger(TestSuiteExecutorDao.class);

	public List<TestSuiteResponse> getPendingTestSuites(int rows) {
		Query query = new Query(Criteria.where("status").is(TestExecutorEntity.STATUSES.SCHEDULED.getValue())).with(Sort.by(Direction.ASC, "_id"))
				.limit(rows);
		return mongoTemplate.find(query, TestSuiteResponse.class);
	}

	public TestSuiteResponse getTestSuiteResponseById(String id) {
		Query query = new Query(Criteria.where("_id").is(id));
		return mongoTemplate.findOne(query, TestSuiteResponse.class);
	}

	public TestSuite getTestSuiteById(String id) {
		Query query = new Query(Criteria.where("_id").is(id));
		TestSuite var = mongoTemplate.findOne(query, TestSuite.class);
		return var;
	}

	public Variables getVariablesById(String id) {
		Query query = new Query(Criteria.where("_id").is(id));
		Variables var = mongoTemplate.findOne(query, Variables.class);
		return var;
	}

	public void updateTestSuiteStatus(String testsuiteResponseId, TestSuiteResponse response, String status)
			throws Exception {
		Query query = new Query(Criteria.where("_id").is(testsuiteResponseId));
		if (response == null) {
			response = mongoTemplate.findOne(query, TestSuiteResponse.class);
		}

		response.setStatus(status);
		response.setModifiedUserName("itorix");
		response.setModifiedBy("itorix");
		response.setMts(System.currentTimeMillis());
		Document dbDoc = new Document();
		mongoTemplate.getConverter().write(response, dbDoc);
		Update update = Update.fromDocument(dbDoc, "_id");
		UpdateResult result = mongoTemplate.updateFirst(query, update, TestSuiteResponse.class);
		if (result.getModifiedCount() == 0) {
			throw new Exception("Error while updating the testsuite response");
		}
		if (response.getStatus().equalsIgnoreCase(TestSuiteResponse.STATUSES.COMPLETED.getValue())
				|| response.getStatus().equalsIgnoreCase(TestSuiteResponse.STATUSES.CANCELLED.getValue())) {
			saveDashboardStats(response);
		}
	}

	public void updateTestSuiteField(String testSuiteResponseId, String fieldName, String fieldValue) {
		Query query = new Query(Criteria.where("_id").is(testSuiteResponseId));
		Update update = new Update();
		update.set(fieldName, fieldValue);
		mongoTemplate.updateFirst(query, update, TestSuiteResponse.class);
	}

	public MaskFields getMaskFields() {
		List<MaskFields> maskFields = mongoTemplate.findAll(MaskFields.class);
		return maskFields.isEmpty() ? null : maskFields.get(0);
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
					throw new ItorixException(ErrorCodes.errorMessage.get("Testsuite-1008004"), "Testsuite-1008004");
				}

				KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
				keyStore.load(storeFile.getInputStream(), keyStorepassword.toCharArray());

				return new SSLConnectionSocketFactory(new SSLContextBuilder()
						.loadTrustMaterial(trustStore, null)
						.loadKeyMaterial(keyStore, keypassword.toCharArray()).build());


			} catch (KeyManagementException | UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException
					| CertificateException | IOException e) {
				log.error("Could not load certificate to truststore",e);
				throw new ItorixException(ErrorCodes.errorMessage.get("Testsuite-1008004"), "Testsuite-1008004");
			}
		}
		return null;
	}

	public void saveDashboardStats(TestSuiteResponse testSuiteResponse) {
		DashboardStats stats = new DashboardStats(testSuiteResponse.getTestSuiteId(), testSuiteResponse.getConfigId(),
				testSuiteResponse.getCts(), testSuiteResponse.getMts(), testSuiteResponse.getCreatedUserName(),
				testSuiteResponse.getModifiedUserName(), testSuiteResponse.getCreatedBy(),
				testSuiteResponse.getModifiedBy(), testSuiteResponse.getSuccessRate(),
				testSuiteResponse.getTestSuite().getStatus(), testSuiteResponse.getTestSuiteName());
		mongoTemplate.save(stats);

		DashboardSummary summary = mongoTemplate.findOne(
				new Query(Criteria.where("testSuiteName").is(testSuiteResponse.getTestSuite().getName())),
				DashboardSummary.class);

		if (testSuiteResponse.getStatus() != null && testSuiteResponse.getTestSuite() != null) {
			if (summary == null) {
				if (testSuiteResponse.getTestSuite().getStatus().equalsIgnoreCase("PASS")) {
					mongoTemplate.save(new DashboardSummary(testSuiteResponse.getTestSuiteId(),
							testSuiteResponse.getTestSuite().getName(), 1, 0, 0,
							testSuiteResponse.getTestSuite().getSuccessRate()));

				} else if (testSuiteResponse.getTestSuite().getStatus().equalsIgnoreCase("FAIL")) {
					mongoTemplate.save(new DashboardSummary(testSuiteResponse.getTestSuiteId(),
							testSuiteResponse.getTestSuite().getName(), 0, 1, 0,
							testSuiteResponse.getTestSuite().getSuccessRate()));
				} else {
					mongoTemplate.save(new DashboardSummary(testSuiteResponse.getTestSuiteId(),
							testSuiteResponse.getTestSuite().getName(), 0, 0, 1,
							testSuiteResponse.getTestSuite().getSuccessRate()));
				}
			} else {
				if (testSuiteResponse.getTestSuite().getStatus().equalsIgnoreCase("PASS")) {
					summary.setSuccessCount(summary.getSuccessCount() + 1);
					summary.setSuccessRatio(
							(summary.getSuccessRatio() + testSuiteResponse.getTestSuite().getSuccessRate()) / 2);


				} else if (testSuiteResponse.getTestSuite().getStatus().equalsIgnoreCase("FAIL")) {
					summary.setFailureCount(summary.getFailureCount() + 1);
					summary.setSuccessRatio(
							(summary.getSuccessRatio() + testSuiteResponse.getTestSuite().getSuccessRate()) / 2);
				} else {
					summary.setCancelledCount(summary.getCancelledCount() + 1);
				}
				mongoTemplate.save(summary);
			}

			Query query = new Query(Criteria.where("name").is(testSuiteResponse.getTestSuite().getName()));
			Update update = new Update();
			if(summary != null ){
				update.set("successRatio", summary.getSuccessRatio());
			} else {
				update.set("successRatio", testSuiteResponse.getTestSuite().getSuccessRate());
			}
			update.set("executionStatus",testSuiteResponse.getTestSuite().getStatus());
			mongoTemplate.updateFirst(query, update, TestSuite.class);

		}
	}
	public boolean getTimeoutEnable() {
		com.itorix.apiwiz.common.model.integrations.workspace.WorkspaceIntegration integration =  mongoTemplate.findOne(Query.query(Criteria.where("_id").is("apiwiz.testsuite.scenario.timeout.enabled")), com.itorix.apiwiz.common.model.integrations.workspace.WorkspaceIntegration.class);
		return integration != null && integration.getPropertyValue().equalsIgnoreCase("true");
	}
}