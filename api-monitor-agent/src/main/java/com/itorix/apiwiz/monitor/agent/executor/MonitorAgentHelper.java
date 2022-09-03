package com.itorix.apiwiz.monitor.agent.executor;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.itorix.apiwiz.monitor.agent.api.factory.APIFactory;
import com.itorix.apiwiz.monitor.model.request.FormParam;
import com.itorix.apiwiz.monitor.model.request.Header;
import com.itorix.apiwiz.monitor.model.request.MonitorRequest;
import com.itorix.apiwiz.monitor.model.request.QueryParam;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class MonitorAgentHelper {

    private final static Logger logger = LoggerFactory.getLogger(MonitorAgentHelper.class);
    private static final MustacheFactory mf = new DefaultMustacheFactory();

    public HttpResponse invokeMonitorApi(MonitorRequest monitorRequest, Map<String, String> globalVars,
            Map<String, String> encryptedVariables, Map<String, Integer> testStatus,
            SSLConnectionSocketFactory sslConnectionFactory, int timeout) throws Exception {

        if (monitorRequest.getVerb() == null) {
            logger.error("verb is null for {} ", monitorRequest.getId());
            return null;
        }

        monitorRequest.setPath(fillTemplate(monitorRequest.getPath(), globalVars));
        String path = monitorRequest.getPath();
        path = computeQueryParams(path, monitorRequest.getRequest().getQueryParams(), globalVars);

        monitorRequest.setSchemes(fillTemplate(monitorRequest.getSchemes(), globalVars));
        monitorRequest.setHost(fillTemplate(monitorRequest.getHost(), globalVars));
        monitorRequest.setPort(fillTemplate(monitorRequest.getPort(), globalVars));

        path = monitorRequest.getSchemes() + "://" + monitorRequest.getHost() + ":" + monitorRequest.getPort() + path;

        if (path != null) {
            monitorRequest.setPath(path);
        }

        Map<String, String> headers = computeHeaders(monitorRequest.getRequest().getHeaders(), globalVars);
        HttpResponse response = null;
        String reqBody = null;

        if (monitorRequest.getRequest() != null && monitorRequest.getRequest().getBody() != null
                && monitorRequest.getRequest().getBody().getData() != null) {
            logger.debug("Getting monitor request data");
            reqBody = fillTemplate(monitorRequest.getRequest().getBody().getData(), globalVars);
            String reqBodyToSet = fillTemplate(monitorRequest.getRequest().getBody().getData(), encryptedVariables);
            monitorRequest.getRequest().getBody().setData(reqBodyToSet);
        }

        if (monitorRequest.getVerb().equalsIgnoreCase(MonitorAgentRunner.API.GET.toString())) {
            response = APIFactory.invokeGet(path, headers, monitorRequest.getName(), sslConnectionFactory, timeout);
        } else if (monitorRequest.getVerb().equalsIgnoreCase(MonitorAgentRunner.API.POST.toString())) {

            String content = null;
            if (monitorRequest.getRequest() != null
                    && (!CollectionUtils.isEmpty(monitorRequest.getRequest().getFormParams())
                            || !CollectionUtils.isEmpty(monitorRequest.getRequest().getFormURLEncoded()))) {

                List<NameValuePair> generateNameValuePairs = null;
                if (!CollectionUtils.isEmpty(monitorRequest.getRequest().getFormParams())) {
                    generateNameValuePairs = generateNameValuePairs(monitorRequest.getRequest().getFormParams(),
                            globalVars);
                    content = "multi-part";
                } else {
                    generateNameValuePairs = generateNameValuePairs(monitorRequest.getRequest().getFormURLEncoded(),
                            globalVars);
                    content = "form-url-encoded";
                }
                response = APIFactory.invokePost(path, headers, generateNameValuePairs, content, reqBody,
                        monitorRequest.getName(), sslConnectionFactory, timeout);
            } else {
                response = APIFactory.invokePost(path, headers, null, content, reqBody, monitorRequest.getName(),
                        sslConnectionFactory, timeout);
            }
        } else if (monitorRequest.getVerb().equalsIgnoreCase(MonitorAgentRunner.API.PUT.toString())) {

            String contentType = null;
            if (monitorRequest.getRequest() != null
                    && (!CollectionUtils.isEmpty(monitorRequest.getRequest().getFormParams())
                            || !CollectionUtils.isEmpty(monitorRequest.getRequest().getFormURLEncoded()))) {

                List<NameValuePair> generateNameValuePairs = null;
                if (!CollectionUtils.isEmpty(monitorRequest.getRequest().getFormParams())) {
                    generateNameValuePairs = generateNameValuePairs(monitorRequest.getRequest().getFormParams(),
                            globalVars);
                    contentType = "multi-part";
                } else {
                    generateNameValuePairs = generateNameValuePairs(monitorRequest.getRequest().getFormURLEncoded(),
                            globalVars);
                    contentType = "form-url-encoded";
                }
                response = APIFactory.invokePut(path, headers, generateNameValuePairs, contentType, reqBody,
                        monitorRequest.getName(), sslConnectionFactory, timeout);
            } else {
                response = APIFactory.invokePut(path, headers, null, contentType, reqBody, monitorRequest.getName(),
                        sslConnectionFactory, timeout);
            }

        } else if (monitorRequest.getVerb().equalsIgnoreCase(MonitorAgentRunner.API.DELETE.toString())) {
            response = APIFactory.invokeDelete(path, headers, reqBody, monitorRequest.getName(), sslConnectionFactory,
                    timeout);
        } else if (monitorRequest.getVerb().equalsIgnoreCase(MonitorAgentRunner.API.OPTIONS.toString())) {
            response = APIFactory.invokeOptions(path, headers, reqBody, monitorRequest.getName(), sslConnectionFactory,
                    timeout);
        } else if (monitorRequest.getVerb().equalsIgnoreCase(MonitorAgentRunner.API.PATCH.toString())) {
            response = APIFactory.invokePatch(path, headers, reqBody, monitorRequest.getName(), sslConnectionFactory,
                    timeout);
        }

        ResponseManager responseManager = new ResponseManager();
        responseManager.gatherResponseData(response, monitorRequest.getResponse(), globalVars, encryptedVariables);

        return response;

    }

    private List<NameValuePair> generateNameValuePairs(List<FormParam> formParams, Map<String, String> globalVars) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        if (formParams != null) {
            for (FormParam formParam : formParams) {
                formParam.setValue(fillTemplate(formParam.getValue(), globalVars));
                params.add(new BasicNameValuePair(formParam.getName(), formParam.getValue()));
            }
        }
        return params;
    }

    public Map<String, String> computeHeaders(List<Header> headers, Map<String, String> globalVars) {
        Map<String, String> headerMap = new HashMap<String, String>();
        if (null == headers) {
            return headerMap;
        }
        for (Header header : headers) {
            header.setValue(fillTemplate(header.getValue(), globalVars));
            headerMap.put(header.getName(), header.getValue());
        }
        return headerMap;
    }

    public String fillTemplate(String input, Map<String, String> vars) {
        if (input == null) {
            return null;
        }
        if (vars == null) {
            return input;
        }
        Writer writer = new StringWriter();
        Mustache mustache = mf.compile(new StringReader(input), "headers");
        mustache.execute(writer, vars);
        return writer.toString();
    }

    private String computeQueryParams(String path, List<QueryParam> queryParams, Map<String, String> globalVars) {
        if (path != null) {
            if (!path.contains("?")) {
                path = path + "?";
            }
        } else {
            path = "?";
        }
        if (queryParams != null) {
            for (QueryParam param : queryParams) {
                param.setValue(fillTemplate(param.getValue(), globalVars));
                path = path + "&" + param.getName() + "=" + param.getValue();
            }
        } else {
            if (path.endsWith("?")) {
                path = path.substring(0, path.length() - 1);
            }
        }
        return path;
    }

}
