package com.itorix.apiwiz.monitor.agent.executor;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;
import org.xml.sax.SAXException;

import com.itorix.apiwiz.monitor.agent.executor.validators.JsonValidator;
import com.itorix.apiwiz.monitor.agent.executor.validators.ResponseValidator;
import com.itorix.apiwiz.monitor.agent.executor.validators.XmlValidator;
import com.itorix.apiwiz.monitor.model.request.Response;
import com.itorix.apiwiz.monitor.model.request.Variable;
@Slf4j
public class ResponseManager {

    public ResponseValidator gatherResponseData(HttpResponse actualResponse, Response response,
            Map<String, String> vars, Map<String, String> encryptedVariables)
            throws ParseException, IOException, ParserConfigurationException, SAXException {

        ResponseValidator validator = null;
        if (response.getBody() != null && response.getBody().getType() != null) {
            log.debug("Fetching validators");
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
        // response.setStatus(actualResponse.getStatusLine().getStatusCode());
        response.setMessage(actualResponse.getStatusLine().getReasonPhrase());

        if (response.getVariables() != null) {
            for (Variable variable : response.getVariables()) {
                if (variable.getReference() != null) {
                    if (variable.getReference().equalsIgnoreCase("headers")) {
                        vars.put(variable.getName(), headerMap.get(variable.getName()));
                    } else if (variable.getReference().equalsIgnoreCase("body")) {
                        try {
                            vars.put(variable.getName(), validator.getAttributeValue(variable.getValue()).toString());
                        } catch (Exception e) {
                            log.error("Exception while getting attribute value");
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
                encryptedVariables.put(variable.getName(), vars.get(variable.getName()));
            }
        }

        return validator;
    }
}