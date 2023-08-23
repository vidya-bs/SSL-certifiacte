package com.itorix.apiwiz.common.util.s3;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.itorix.apiwiz.common.model.integrations.s3.S3Integration;
import com.itorix.apiwiz.common.util.StorageIntegration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;


@Component("S3")
public class S3Utils extends StorageIntegration {

	private static final Logger logger = LoggerFactory.getLogger(S3Utils.class);

	@Autowired
	S3Connection s3Connection;

	@Value("${itorix.core.application.url}")
	private String host;

	@Value("${server.contextPath}")
	private String context;

	/**
	 * public static void main(String[] args) throws IOException { S3Utils
	 * s3Utils = new S3Utils(); s3Utils.uplaodFile("AKIA24SOXSG7ZZH23ZDA",
	 * "7p2duuU+sGjyWoEhKS6y+nGwaKCu4WDgwVRbjXL2",
	 * Regions.fromName("us-west-2"), "apiwiz-workspace-assets",
	 * "Document-test/1551935259542.zip", "/Itorix/temp/1551935259542.zip"); }
	 */

	public String uploadFile(String key, String secret, Regions region, String bucketName, String path, String filePath)
			throws IOException {
		logger.debug("Upload file to S3: {}", path);
		AWSCredentials credentials = new BasicAWSCredentials(key, secret);
		AmazonS3 s3client = AmazonS3ClientBuilder.standard()
				.withCredentials(new AWSStaticCredentialsProvider(credentials)).withRegion(region).build();

		s3client.putObject(bucketName, path, new File(filePath));
		return getURL(bucketName, region.getName(), path);
	}

	public String uploadFile(String key, String secret, Regions region, String bucketName, String path,
							 InputStream input) throws IOException {
		logger.debug("Upload file to S3: {}, Bucket Name:{}, Access Key {}", path,bucketName,key);
		AWSCredentials credentials = new BasicAWSCredentials(key, secret);
		AmazonS3 s3client = AmazonS3ClientBuilder.standard()
				.withCredentials(new AWSStaticCredentialsProvider(credentials)).withRegion(region).build();

		s3client.putObject(bucketName, path, input, null);
		return getURL(bucketName, region.getName(), path);
	}

	public String uploadWebsiteResource(String key, String secret, Regions region, String bucketName, String path,
			InputStream input) throws IOException {
		logger.debug("Upload file to S3: {}", path);
		AWSCredentials credentials = new BasicAWSCredentials(key, secret);
		AmazonS3Client s3client = (AmazonS3Client) AmazonS3ClientBuilder.standard()
				.withCredentials(new AWSStaticCredentialsProvider(credentials)).withRegion(region).build();

		s3client.putObject(bucketName, path, input, null);
		return s3client.getResourceUrl(bucketName,path);
	}

	private String getURL(String bucket, String region, String key) {
		// String URL =
		// "https://<bucket-name>.s3-<region-name>.amazonaws.com/<key>";
		String URL = "<host><context>/v1/download/<key>?type=s3";

		URL = URL.replaceAll("<host>", host).replaceAll("<context>", context).replaceAll("<key>", key);
		return URL;
	}

	public InputStream getFile(String key, String secret, Regions region, String bucketName, String path)
			throws IOException {
		logger.debug("Get file from S3: {}", path);
		AWSCredentials credentials = new BasicAWSCredentials(key, secret);
		AmazonS3 s3client = AmazonS3ClientBuilder.standard()
				.withCredentials(new AWSStaticCredentialsProvider(credentials)).withRegion(region).build();
		S3Object s3object = s3client.getObject(bucketName, path);
		InputStream inputStream = s3object.getObjectContent();
		return inputStream;
	}
	public void deleteFile(String key, String secret, Regions region, String bucketName, String path)
			throws IOException{
		logger.debug("Delete file from S3: {}", path);
		AWSCredentials credentials = new BasicAWSCredentials(key, secret);
		AmazonS3 s3client = AmazonS3ClientBuilder.standard()
				.withCredentials(new AWSStaticCredentialsProvider(credentials)).withRegion(region).build();
		try {
			S3Object s3object = s3client.getObject(bucketName, path);
			s3client.deleteObject(bucketName, s3object.getKey());
		} catch (Exception e) {
			logger.error("File not deleted from S3");
			logger.error(e.getMessage());
		}
	}
	@Override
	public String uploadFile(String path, String data) throws Exception {
		S3Integration s3Integration = s3Connection.getS3Integration();
		return uploadFile(s3Integration.getKey(), s3Integration.getDecryptedSecret(), Regions.fromName(s3Integration.getRegion()), s3Integration.getBucketName(), path, data);
	}

	@Override
	public String uploadFile(String path, InputStream data) throws Exception {
		S3Integration s3Integration = s3Connection.getS3Integration();
		return uploadFile(s3Integration.getKey(), s3Integration.getDecryptedSecret(), Regions.fromName(s3Integration.getRegion()), s3Integration.getBucketName(), path, data);
	}

	public String uploadWebsiteResource(String path, InputStream data) throws Exception{
		S3Integration s3Integration = s3Connection.getS3Integration();
		return uploadWebsiteResource(s3Integration.getKey(), s3Integration.getDecryptedSecret(),
				Regions.fromName(s3Integration.getRegion()), s3Integration.getBucketName(), path, data);
	}

	@Override
	public InputStream getFile(String path) throws Exception {
		S3Integration s3Integration = s3Connection.getS3Integration();
		return getFile(s3Integration.getKey(), s3Integration.getDecryptedSecret(), Regions.fromName(s3Integration.getRegion()), s3Integration.getBucketName(), path);
	}

	@Override
	public void deleteFile(String path) throws Exception {
		S3Integration s3Integration = s3Connection.getS3Integration();
		deleteFile(s3Integration.getKey(), s3Integration.getDecryptedSecret(), Regions.fromName(s3Integration.getRegion()), s3Integration.getBucketName(), path);
	}

}
