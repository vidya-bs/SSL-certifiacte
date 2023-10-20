package com.itorix.apiwiz.ibm.apic.connector.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;
public final class HttpUtil {

	public static ResponseEntity<Object> response(String message, HttpStatus status){
		if(message != null && !message.isEmpty()){
			Map<String,Object> responseMap = new HashMap<>();
			responseMap.put("status",status.value());
			responseMap.put("message",message);
			return new ResponseEntity<Object>(responseMap,status);
		}
		return new ResponseEntity<Object>(status);
	}
}
