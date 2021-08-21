package com.itorix.apiwiz.common.postman;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itorix.apiwiz.common.model.postman.PostmanCollection;
import com.itorix.apiwiz.common.model.postman.PostmanEnvironment;

public class PostmanReader {
	private static final Logger logger = Logger.getLogger(PostmanReader.class);
	ObjectMapper om;

	public PostmanReader() {
		om = new ObjectMapper();
		om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	public PostmanCollection readCollectionFileClasspath(String fileOnClasspath)
			throws JsonParseException, JsonMappingException, IOException {
		String fileName = fileOnClasspath.substring(fileOnClasspath.indexOf(":") + 1);
		InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);

		PostmanCollection collection = om.readValue(stream, PostmanCollection.class);
		stream.close();
		return collection;
	}

	public PostmanEnvironment readEnvironmentFileClasspath(String fileOnClasspath)
			throws JsonParseException, JsonMappingException, IOException {
		String fileName = fileOnClasspath.substring(fileOnClasspath.indexOf(":") + 1);
		InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);

		PostmanEnvironment env = om.readValue(stream, PostmanEnvironment.class);
		stream.close();
		return env;
	}

	public PostmanCollection readCollectionFile(String filePath) throws Exception {
		if (filePath.startsWith("classpath:")) {
			return readCollectionFileClasspath(filePath);
		}
		InputStream stream = new FileInputStream(new File(filePath));

		PostmanCollection collection = om.readValue(stream, PostmanCollection.class);
		stream.close();
		return collection;
	}

	public PostmanCollection readCollectionFile(InputStream stream) throws Exception {

		PostmanCollection collection = om.readValue(stream, PostmanCollection.class);
		stream.close();
		return collection;
	}

	public PostmanEnvironment readEnvironmentFile(InputStream stream) throws Exception {

		PostmanEnvironment env = om.readValue(stream, PostmanEnvironment.class);
		stream.close();
		return env;
	}

	public PostmanEnvironment readEnvironmentFile(String filePath) throws Exception {
		if (filePath == null) {
			return new PostmanEnvironment();
		}
		if (filePath.startsWith("classpath:")) {
			return readEnvironmentFileClasspath(filePath);
		}
		InputStream stream = new FileInputStream(new File(filePath));
		PostmanEnvironment env = om.readValue(stream, PostmanEnvironment.class);
		stream.close();
		return env;
	}

	public static String readFile(String file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = null;
		StringBuilder stringBuilder = new StringBuilder();
		String ls = System.getProperty("line.separator");

		try {
			while ((line = reader.readLine()) != null) {
				stringBuilder.append(line);
				stringBuilder.append(ls);
			}
			logger.info(stringBuilder.toString());
			return stringBuilder.toString();
		} finally {
			reader.close();
		}
	}
}
