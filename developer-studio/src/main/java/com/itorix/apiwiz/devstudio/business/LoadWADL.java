package com.itorix.apiwiz.devstudio.business;

import javax.xml.xpath.XPathExpressionException;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface LoadWADL {
	
	public String getWADLTargetOperations(String document) throws XPathExpressionException, JsonProcessingException;
	
	public String getWADLProxyOperations(String document) throws XPathExpressionException, JsonProcessingException;

}
