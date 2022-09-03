package com.itorix.apiwiz.monitor.agent.executor.validators;

import javax.xml.xpath.XPathExpressionException;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsonValidator extends ResponseValidator {

    private DocumentContext context;

    public JsonValidator() {
    }

    public JsonValidator(String response) {
        context = JsonPath.parse(response);
    }

    /**
     *
     * @param path
     * 
     * @return @throws
     */
    public Object getAttributeValue(String path) throws XPathExpressionException {
        Object value = null;
        try {
            // if(context.read(path) instanceof JSONArray) {
            // JSONArray array = context.read(path);
            // if(array.size() == 1) {
            // value = array.get(0).toString();
            // } else {
            // throw new Exception("Invalid JSON Path specified");
            // }
            // } else {
            // value = context.read(path).toString();
            // }
            value = context.read(path);
        } catch (Exception ex) {
            log.error("Exception occurred",ex);
        }
        return value;
    }

    public void setAttributeValue(String path, String value) throws XPathExpressionException {
        context.set(path, value);
    }

    public String getUpdatedObjectAsString() throws Exception {
        return context.jsonString();
    }
}