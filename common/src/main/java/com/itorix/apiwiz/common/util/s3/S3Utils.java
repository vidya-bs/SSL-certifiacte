package com.itorix.apiwiz.common.util.s3;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.stereotype.Component;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

@Component
public class S3Utils {

	/**
	public static void main(String[] args) throws IOException {
		S3Utils s3Utils = new S3Utils();
		s3Utils.uplaodFile("AKIA24SOXSG7ZZH23ZDA", "7p2duuU+sGjyWoEhKS6y+nGwaKCu4WDgwVRbjXL2",
				Regions.fromName("us-west-2"), "apiwiz-workspace-assets", "Document-test/1551935259542.zip",
				"/Itorix/temp/1551935259542.zip");
	}
	*/
	
	public String uplaodFile(String key, String secret, Regions region, String bucketName, String path, String filePath)
			throws IOException {
		AWSCredentials credentials = new BasicAWSCredentials(key, secret);
		AmazonS3 s3client = AmazonS3ClientBuilder.standard()
				.withCredentials(new AWSStaticCredentialsProvider(credentials)).withRegion(region).build();

		s3client.putObject(bucketName, path, new File(filePath));
		return getURL(bucketName, region.getName(), path);
	}

	public String uplaodFile(String key, String secret, Regions region, String bucketName, String path,
			InputStream input) throws IOException {
		AWSCredentials credentials = new BasicAWSCredentials(key, secret);
		AmazonS3 s3client = AmazonS3ClientBuilder.standard()
				.withCredentials(new AWSStaticCredentialsProvider(credentials)).withRegion(region).build();

		s3client.putObject(bucketName, path, input, null);
		return getURL(bucketName, region.getName(), path);
	}

	private String getURL(String bucket, String region, String key) {
		String URL = "https://<bucket-name>.s3-<region-name>.amazonaws.com/<key>";
		URL = URL.replaceAll("<bucket-name>", bucket).replaceAll("<region-name>", region).replaceAll("<key>", key);
		return URL;
	}

}
