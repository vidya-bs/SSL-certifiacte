package com.itorix.apiwiz.common.util.s3;

import java.util.List;

import groovy.util.logging.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.itorix.apiwiz.common.model.integrations.Integration;
import com.itorix.apiwiz.common.model.integrations.s3.S3Integration;
import com.itorix.apiwiz.common.properties.ApplicationProperties;

@Component
public class S3Connection {

	@Autowired
	ApplicationProperties applicationProperties;

	@Autowired
	private MongoTemplate mongoTemplate;

	public S3Integration getS3Integration() {
		S3Integration s3Integration = getIntegration().getS3Integration();
		if (s3Integration == null) {
			String key = applicationProperties.getS3key();
			String secret = applicationProperties.getS3secret();
			String bucketName = applicationProperties.getS3bucketName();
			String region = applicationProperties.getS3region();
			if (null != key && null != secret && null != bucketName && null != region) {
				s3Integration = new S3Integration();
				s3Integration.setKey(key);
				s3Integration.setSecret(secret);
				s3Integration.setRegion(region);
				s3Integration.setBucketName(bucketName);
			}
		}
		return s3Integration;
	}

	private Integration getIntegration() {
		Query query = new Query();
		query.addCriteria(Criteria.where("type").is("S3"));
		List<Integration> dbIntegrationList = mongoTemplate.find(query, Integration.class);
		Integration integration = new Integration();
		if (dbIntegrationList != null && dbIntegrationList.size() > 0)
			return dbIntegrationList.get(0);
		return integration;
	}

}
