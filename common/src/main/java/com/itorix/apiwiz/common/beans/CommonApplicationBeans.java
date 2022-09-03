package com.itorix.apiwiz.common.beans;

import java.security.NoSuchAlgorithmException;

import javax.crypto.NoSuchPaddingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itorix.apiwiz.common.properties.ApplicationProperties;
import com.itorix.apiwiz.common.util.apigee.ApigeeUtil;
import com.itorix.apiwiz.common.util.encryption.RSAEncryption;
import com.itorix.apiwiz.common.util.http.HTTPUtil;
import com.itorix.apiwiz.common.util.http.HttpErrorHandler;
import com.itorix.apiwiz.common.util.mail.MailUtil;
import com.itorix.apiwiz.common.util.scm.ScmUtilImpl;

@Configuration
public class CommonApplicationBeans {
	private static final Logger logger = LoggerFactory.getLogger(CommonApplicationBeans.class);
	@Bean(name = "restTemplate")
	public RestTemplate restTemplateBean() {
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.setErrorHandler(new HttpErrorHandler());
		return restTemplate;
	}

	@Bean(name = "hTTPUtil")
	public HTTPUtil hTTPUtilBean() {
		HTTPUtil hTTPUtil = new HTTPUtil();
		return hTTPUtil;
	}

	@Bean(name = "mapper")
	public ObjectMapper objectMapperBean() {
		ObjectMapper mapper = new ObjectMapper();
		return mapper;
	}

	@Bean(name = "applicationProperties")
	public ApplicationProperties capplicationPropertiesBean() {
		ApplicationProperties applicationProperties = new ApplicationProperties();
		return applicationProperties;
	}

	@Bean(name = "mailUtils")
	public MailUtil mailUtilBean() {
		MailUtil mailUtil = new MailUtil();
		return mailUtil;
	}

	@Bean(name = "rSAEncryption")
	public RSAEncryption rSAEncryptionBean() {
		RSAEncryption rSAEncryption;
		try {
			rSAEncryption = new RSAEncryption();
			return rSAEncryption;
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			logger.error("Exception occurred", e);
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			logger.error("Exception occurred", e);
		}
		return null;
	}

	@Bean(name = "apigeeUtil")
	public ApigeeUtil apigeeUtilBean() {
		ApigeeUtil apigeeUtil = new ApigeeUtil();
		return apigeeUtil;
	}
}
