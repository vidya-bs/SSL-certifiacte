package com.itorix.apiwiz.devportal.dao;



import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.util.http.HTTPUtil;


@Component("devportalDao")
public class DevportalDao{

	/**
	 * @param httpConn
	 * @param method
	 * @return
	 * @throws ItorixException
	 */
	public ResponseEntity<String> proxyService(HTTPUtil httpConn, String method) throws ItorixException{
		try {
			ResponseEntity<String> response;
			if(method.equals("POST"))
				response = httpConn.doPost();
			else 
				response = httpConn.doGet();
			HttpStatus statusCode = response.getStatusCode();
			if (statusCode.is2xxSuccessful())
				return new ResponseEntity<String>(response.getBody(), HttpStatus.OK);
			else if (statusCode.value() >= 401 && statusCode.value() <= 403)
				throw new ItorixException("Invalid Apigee Credentials " + statusCode.value(), "Config-1007");
			else
				throw new ItorixException("invalid request data " + statusCode.value(), "Config-1000");
		} catch (ItorixException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new ItorixException(ex.getMessage(), "Config-1000", ex);
		}
	}

}
