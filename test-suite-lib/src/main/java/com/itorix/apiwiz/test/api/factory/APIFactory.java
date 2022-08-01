package com.itorix.apiwiz.test.api.factory;

import com.itorix.apiwiz.test.executor.beans.HttpDeleteWithBody;
import org.apache.http.Consts;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class APIFactory {

    private final static Logger logger = LoggerFactory.getLogger(APIFactory.class);

    public static HttpResponse invokeGet(String url, Map<String, String> headers, String testCaseId,
            SSLConnectionSocketFactory sslConnectionFactory, int timeout) throws ClientProtocolException, IOException {
        HttpResponse response = null;

        HttpClient client = getClient(sslConnectionFactory, timeout);
        HttpGet request = new HttpGet(url);
        request = new HttpGet(url);
        addHeaders(request, headers);
        response = client.execute(request);
        return response;
    }

    private static HttpClient getClient(SSLConnectionSocketFactory sslConnectionFactory, int timeout) {
        HttpClient client;
        if (sslConnectionFactory != null) {
            client = HttpClientBuilder.create().setSSLSocketFactory(sslConnectionFactory)
                    .setDefaultRequestConfig(requestConfigWithTimeout(timeout)).build();
        } else {
            client = HttpClientBuilder.create().setDefaultRequestConfig(requestConfigWithTimeout(timeout)).build();
        }
        return client;
    }

    public static RequestConfig requestConfigWithTimeout(int timeoutInMilliseconds) {
        return RequestConfig.copy(RequestConfig.DEFAULT).setSocketTimeout(timeoutInMilliseconds)
                .setConnectTimeout(timeoutInMilliseconds).setConnectionRequestTimeout(timeoutInMilliseconds).build();
    }

    public static HttpResponse invokePost(String url, Map<String, String> headers, List<NameValuePair> params,
            String type, String body, String testCaseId, SSLConnectionSocketFactory sslConnectionFactory, int timeout)
            throws ClientProtocolException, IOException {
        HttpResponse response = null;
        HttpClient client = getClient(sslConnectionFactory, timeout);
        HttpPost request = new HttpPost(url);
        updatePostPutRequest(headers, params, type, body, request);
        response = client.execute(request);
        return response;
    }

    private static void updatePostPutRequest(Map<String, String> headers, List<NameValuePair> params, String type,
            String body, HttpEntityEnclosingRequestBase request) throws UnsupportedEncodingException {
        if (body == null) {

            if (type.equals("multi-part")) {
                headers.remove("Content-Type");
                MultipartEntityBuilder entitybuilder = MultipartEntityBuilder.create();
                entitybuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                for (NameValuePair param : params) {
                    entitybuilder.addTextBody(param.getName(), param.getValue(), ContentType.DEFAULT_BINARY);
                }
                request.setEntity(entitybuilder.build());
            } else {
                List<NameValuePair> formparams = new ArrayList<NameValuePair>();

                for (NameValuePair param : params) {
                    formparams.add(new BasicNameValuePair(param.getName(), param.getValue()));
                }
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, Consts.UTF_8);
                request.setEntity(entity);
            }

        } else {
            request.setEntity(new StringEntity(body));
        }
        addHeaders(request, headers);
    }

    public static HttpResponse invokePut(String url, Map<String, String> headers, List<NameValuePair> params,
            String type, String body, String testCaseId, SSLConnectionSocketFactory sslConnectionFactory, int timeout)
            throws ClientProtocolException, IOException {

        HttpResponse response = null;
        HttpClient client = getClient(sslConnectionFactory, timeout);
        HttpPut request = new HttpPut(url);
        updatePostPutRequest(headers, params, type, body, request);
        response = client.execute(request);
        return response;
    }

    public static HttpResponse invokeDelete(String url, Map<String, String> headers, String body, String testCaseId,
            SSLConnectionSocketFactory sslConnectionFactory, int timeout) throws ClientProtocolException, IOException {
        HttpResponse response = null;
        HttpClient client = getClient(sslConnectionFactory, timeout);
        HttpRequestBase request=null;
        if(null != body && !body.isEmpty()){
         request = new HttpDeleteWithBody(url);  
         addBody(request, body);
        }
        else
        {
        	request=new HttpDelete(url);
        }
        addHeaders(request, headers);

        response = client.execute(request);
        
        return response;
    }

    private static void addBody(HttpRequestBase request, String body) {
        if(null != body && !"".equalsIgnoreCase(body)) {
            StringEntity entity = new StringEntity(body, ContentType.APPLICATION_JSON);
            ((HttpEntityEnclosingRequestBase) request).setEntity(entity);
        }
    }

    public static HttpResponse invokeOptions(String url, Map<String, String> headers, String body, String testCaseId,
            SSLConnectionSocketFactory sslConnectionFactory, int timeout) throws ClientProtocolException, IOException {
        HttpResponse response = null;
        HttpClient client = getClient(sslConnectionFactory, timeout);
        HttpOptions request = new HttpOptions(url);
        addHeaders(request, headers);
        response = client.execute(request);
        return response;
    }

    public static HttpResponse invokePatch(String url, Map<String, String> headers, String body, String testCaseId,
            SSLConnectionSocketFactory sslConnectionFactory, int timeout) throws ClientProtocolException, IOException {
        HttpResponse response = null;
        HttpClient client = getClient(sslConnectionFactory, timeout);
        HttpPatch request = new HttpPatch(url);
        request.setEntity(new StringEntity(body));
        addHeaders(request, headers);
        response = client.execute(request);
        return response;
    }

    private static void addHeaders(HttpRequestBase request, Map<String, String> headers) {
        if (headers == null) {
            return;
        }
        for (Map.Entry<String, String> header : headers.entrySet()) {
            request.addHeader(header.getKey(), header.getValue().toString());
        }
    }
}
