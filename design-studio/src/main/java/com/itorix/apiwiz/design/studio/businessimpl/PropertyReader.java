package com.itorix.apiwiz.design.studio.businessimpl;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
@Slf4j
public class PropertyReader {

	public Properties readClassPathPropertyFile(String filePath) {
		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = this.getClass().getClassLoader().getResourceAsStream(filePath);

			prop.load(input);
		} catch (IOException ex) {
			log.error("Exception occurred", ex);
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					log.error("Exception occurred", e);
				}
			}
		}
		return prop;
	}

	public Properties readPropertiesFile(String filePath) {
		Properties prop = new Properties();
		FileInputStream input = null;
		try {
			input = new FileInputStream(new File(filePath));
			prop.load(input);
		} catch (IOException ex) {
			log.error("Exception occurred", ex);
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					log.error("Exception occurred", e);
				}
			}
		}
		return prop;
	}
}
