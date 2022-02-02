package com.itorix.apiwiz.devportal.dao;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.ServerRequest.Headers;

import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.util.http.HTTPUtil;

@Component("devportalDao")
public class DevportalDao {

	/**
	 * @param httpConn
	 * @param method
	 * 
	 * @return
	 * 
	 * @throws ItorixException
	 */
	public ResponseEntity<String> proxyService(HTTPUtil httpConn, String method) throws ItorixException {
		try {
			ResponseEntity<String> response;
			if (method.equals("POST"))
				response = httpConn.doPost();
			else if (method.equals("PUT"))
				response = httpConn.doPut();
			else if (method.equals("DELETE"))
				response = httpConn.doDelete();
			else
				response = httpConn.doGet();
			HttpStatus statusCode = response.getStatusCode();
			if (statusCode.is2xxSuccessful()) {
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_JSON);
				ResponseEntity<String> responseEntity = new ResponseEntity<String>(response.getBody(), headers,
						HttpStatus.OK);
				return responseEntity;
			} else if (statusCode.value() >= 401 && statusCode.value() <= 403)
				throw new ItorixException(
						"Request validation failed. Exception connecting to apigee connector. " + statusCode.value(),
						"Configuration-1006");
			else
				throw new ItorixException("invalid request data " + statusCode.value(), "Configuration-1000");
		} catch (ItorixException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Configuration-1000", ex);
		}
	}
}
