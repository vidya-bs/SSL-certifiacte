package com.itorix.apiwiz.test.executor;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.itorix.apiwiz.test.executor.beans.MaskFields;
import com.itorix.apiwiz.test.executor.beans.Response;
import com.itorix.apiwiz.test.executor.beans.Variable;
import com.itorix.apiwiz.test.executor.validators.JsonValidator;
import com.itorix.apiwiz.test.executor.validators.ResponseValidator;
import com.itorix.apiwiz.test.executor.validators.XmlValidator;
import com.itorix.apiwiz.test.util.RSAEncryption;

public class ResponseManager {

	private static final Logger logger = LoggerFactory.getLogger(ResponseManager.class);

	public ResponseValidator gatherResponseData(HttpResponse actualResponse, Response response,
			Map<String, String> vars , Map<String, String> encryptedVariables ,MaskFields maskFields) throws ParseException, IOException, ParserConfigurationException, SAXException {

		ResponseValidator validator = null;
		if (response.getBody() != null && response.getBody().getType() != null) {
			if (actualResponse.getEntity() != null) {
				response.getBody().setData(EntityUtils.toString(actualResponse.getEntity(), "UTF-8"));
			}

			if (response.getBody() != null && response.getBody().getData() != null
					&& !response.getBody().getData().isEmpty()) {
				if (response.getBody().getType().equalsIgnoreCase("json")) {
					validator = new JsonValidator(response.getBody().getData());
				}
				if (response.getBody().getType().equalsIgnoreCase("xml")) {
					validator = new XmlValidator(response.getBody().getData());
				}
			}
		}

		Map<String, String> headerMap = Arrays.stream(actualResponse.getAllHeaders())
				.collect(Collectors.toMap(Header::getName, Header::getValue));
		response.setHeaders(headerMap);
		response.setStatus(actualResponse.getStatusLine().getStatusCode());
		response.setMessage(actualResponse.getStatusLine().getReasonPhrase());

		if (response.getVariables() != null) {
			// actualResponse.getAllHeaders(),
			// EntityUtils.toString(actualResponse.getEntity(), "UTF-8");
			for (Variable variable : response.getVariables()) {
				if (variable.getReference() != null) {
					if (variable.getReference().equalsIgnoreCase("headers")) {
						vars.put(variable.getName(), headerMap.get(variable.getName()));
						if(variable.isEncryption()){
							try {
							headerMap.put(variable.getName() , (new RSAEncryption()).encryptText(headerMap.get(variable.getName())));
							} catch (Exception e) {
								logger.error("error while encrypting text",e);
							}
						}
					} else if (variable.getReference().equalsIgnoreCase("body")) {
						try {
							vars.put(variable.getName(), validator.getAttributeValue(variable.getValue()).toString());
							if(variable.isEncryption()){
							validator.setAttributeValue(variable.getValue(), (new RSAEncryption()).encryptText(vars.get(variable.getName())));
							}
						} catch (Exception e) {
							logger.error("error while encrypting text",e);
						}
					}
					if (variable.getReference().equalsIgnoreCase("status")) {
						if (variable.getName().equals("code"))
							vars.put(variable.getName(),
									Integer.toString(actualResponse.getStatusLine().getStatusCode()));
						else
							vars.put(variable.getName(), actualResponse.getStatusLine().getReasonPhrase());
					}
				}
				if(variable.isEncryption()){
					try {
						encryptedVariables.put(variable.getName(), (new RSAEncryption()).encryptText(vars.get(variable.getName())));
					} catch (Exception e) {
						logger.error("error while encrypting text",e);
					}
				} else {
					encryptedVariables.put(variable.getName(), vars.get(variable.getName()));
				}
			}
		}

		return validator;
	}
}