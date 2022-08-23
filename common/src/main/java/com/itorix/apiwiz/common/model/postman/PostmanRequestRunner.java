package com.itorix.apiwiz.common.model.postman;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatus.Series;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.itorix.apiwiz.common.postman.HaltTestFolderException;
import com.itorix.apiwiz.common.postman.PostmanRunResult;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PostmanRequestRunner {
	private static final Logger logger = LoggerFactory.getLogger(PostmanRequestRunner.class);
	private PostmanVariables var;
	private boolean haltOnError = false;

	private static HttpComponentsClientHttpRequestFactory httpClientRequestFactory = new HttpComponentsClientHttpRequestFactory();

	public PostmanRequestRunner(PostmanVariables var, boolean haltOnError) {
		this.var = var;
	}

	private RestTemplate setupRestTemplate(PostmanRequest request) {
		RestTemplate restTemplate = new RestTemplate(httpClientRequestFactory);
		if (request.dataMode != null && request.dataMode.equals("urlencoded")) {
			logger.debug("Setting message convertors ");
			List<HttpMessageConverter<?>> converters = new ArrayList<HttpMessageConverter<?>>();
			converters.add(new FormHttpMessageConverter());
			StringHttpMessageConverter stringConv = new StringHttpMessageConverter();
			stringConv.setWriteAcceptCharset(false);
			converters.add(stringConv);
			restTemplate.setMessageConverters(converters);
		}
		restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
			protected boolean hasError(HttpStatus statusCode) {
				return false;
			}
		});
		return restTemplate;
	}

	public boolean run(PostmanRequest request, PostmanRunResult runResult) {
		Map postmanResult = new HashMap();
		postmanResult.put("Request", request);
		runPrerequestScript(request, runResult);
		HttpHeaders headers = request.getHeaders(var);
		if (request.dataMode != null && request.dataMode.equals("urlencoded")) {
			headers.set("Content-Type", "application/x-www-form-urlencoded");
		}
		HttpEntity<String> entity = new HttpEntity<String>(request.getData(var), headers);
		ResponseEntity<String> httpResponse = null;
		RestTemplate restTemplate = setupRestTemplate(request);
		String url = request.getUrl(var);
		URI uri;
		try {
			uri = new URI(url);
		} catch (URISyntaxException e) {
			if (haltOnError)
				throw new HaltTestFolderException();
			else
				return false;
		}

		long startMillis = System.currentTimeMillis();
		logger.debug("Making a call to {}", uri);
		httpResponse = restTemplate.exchange(uri, HttpMethod.valueOf(request.method), entity, String.class);
		Set<Entry<String, List<String>>> entries = entity.getHeaders().entrySet();
		for (Entry<String, List<String>> ent : entries) {
			logger.info(ent.getKey() + ":" + ent.getValue());
		}
		logger.info(" [" + (System.currentTimeMillis() - startMillis) + "ms]");
		logger.info("httpResponse : " + httpResponse);
		postmanResult.put("Response", httpResponse);
		runResult.addResultItem(request.name, postmanResult);
		if (httpResponse.getStatusCode().series() != Series.SERVER_ERROR) {
			return this.evaluateTests(request, httpResponse, runResult, postmanResult);
		} else {
			return false;
		}
	}

	/*
	 * public boolean run(PostmanRequest request, PostmanRunResult
	 * runResult,APIMonitorResults apiMonitorResults,BaseRepository
	 * baseRepository) { List<Resources>
	 * resourcesList=apiMonitorResults.getResources(); if(resourcesList==null){
	 * resourcesList=new ArrayList<>(); } Resources resources=new Resources();
	 * Request req=new Request(); Response res=new Response();
	 * runPrerequestScript(request, runResult);
	 * 
	 * HttpHeaders headers = request.getHeaders(var); if
	 * (request.dataMode.equals("urlencoded")) { headers.set("Content-Type",
	 * "application/x-www-form-urlencoded"); } resources.setName(request.name);
	 * HttpEntity<String> entity = new HttpEntity<String>(request.getData(var),
	 * headers); ResponseEntity<String> httpResponse = null; RestTemplate
	 * restTemplate = setupRestTemplate(request); String url =
	 * request.getUrl(var); URI uri; try { uri = new URI(url); } catch
	 * (URISyntaxException e) { if (haltOnError) throw new
	 * HaltTestFolderException(); else return false; } List<String>
	 * ignoreList=new ArrayList<String>(); ignoreList.add("Accept-Charset");
	 * List<Headers> headersList=new ArrayList<>(); Set<Entry<String,
	 * List<String>>> entries=entity.getHeaders().entrySet(); for(Entry<String,
	 * List<String>> ent:entries){ Headers headers2=new Headers();
	 * headers2.setName(ent.getKey()); if(ent.getValue()!=null
	 * &&ent.getValue().size()>0){ headers2.setValue(ent.getValue().get(0)); }
	 * headersList.add(headers2); } req.setHeaders(headersList);
	 * req.setVerb(request.method); req.setUri(uri.getPath());
	 * req.setPayload((request.rawModeData==null)?null:request.rawModeData.
	 * toString()); resources.setRequest(req); long startMillis =
	 * System.currentTimeMillis(); try{ httpResponse =
	 * restTemplate.exchange(uri, HttpMethod.valueOf(request.method), entity,
	 * String.class); }catch(Exception e){ resources.setStatus("Error"); long
	 * elapsedTime = System.currentTimeMillis() - startMillis;
	 * res.setResponseTime(elapsedTime); resources.setResponse(res);
	 * resourcesList.add(resources);
	 * apiMonitorResults.setResources(resourcesList);
	 * baseRepository.save(apiMonitorResults); } if(httpResponse!=null){ long
	 * elapsedTime = System.currentTimeMillis() - startMillis;
	 * res.setResponseTime(elapsedTime); res.setPayload(httpResponse.getBody());
	 * List<Headers> headersList1=new ArrayList<>(); Set<Entry<String,
	 * List<String>>> entries1 = httpResponse.getHeaders().entrySet();
	 * for(Entry<String, List<String>> ent:entries1){
	 * if(!ignoreList.contains(ent.getKey())){ Headers headers2=new Headers();
	 * headers2.setName(ent.getKey()); if(ent.getValue()!=null
	 * &&ent.getValue().size()>0){ headers2.setValue(ent.getValue().get(0)); }
	 * headersList1.add(headers2); } } res.setHeaders(headersList1);
	 * resources.setResponse(res); resourcesList.add(resources);
	 * apiMonitorResults.setResources(resourcesList);
	 * 
	 * logger.info(" [" + (System.currentTimeMillis() - startMillis) + "ms]");
	 * logger.info("httpResponse : " + httpResponse); if
	 * (httpResponse.getStatusCode().series() != Series.SERVER_ERROR &&
	 * httpResponse.getStatusCode().series() != Series.CLIENT_ERROR &&
	 * httpResponse.getStatusCode().series() != Series.REDIRECTION) {
	 * resources.setStatus("Success"); baseRepository.save(apiMonitorResults);
	 * return this.evaluateTests(request, httpResponse, runResult, null); } else
	 * { resources.setStatus("Error"); baseRepository.save(apiMonitorResults);
	 * return false; } }else{ long elapsedTime = System.currentTimeMillis() -
	 * startMillis; res.setResponseTime(elapsedTime);
	 * resources.setResponse(res); resources.setStatus("Error");
	 * baseRepository.save(apiMonitorResults); return false; }
	 * 
	 * }
	 */
	/**
	 * @param request
	 * @param httpResponse
	 * 
	 * @return true if all tests pass, false otherwise
	 */
	public boolean evaluateTests(PostmanRequest request, ResponseEntity<String> httpResponse,
			PostmanRunResult runResult, Map postmanResult) {
		if (request.tests == null || request.tests.isEmpty()) {
			return true;
		}
		Map tests = new HashMap();
		Context cx = Context.enter();
		String testName = "---------------------> POSTMAN test";
		boolean isSuccessful = false;
		try {
			Scriptable scope = cx.initStandardObjects();
			PostmanJsVariables jsVar = new PostmanJsVariables(cx, scope, this.var.getEnv());
			jsVar.prepare(httpResponse);

			// Evaluate the test script
			cx.evaluateString(scope, request.tests, testName, 1, null);
			// The results are in the jsVar.tests variable

			// Extract any generated environment variables during the js run.
			jsVar.extractEnvironmentVariables();
			isSuccessful = true;
			Set<Map.Entry<Object, Object>> jsVarSet = jsVar.tests.entrySet();
			for (Map.Entry e : jsVarSet) {
				runResult.totalTest++;

				String strVal = e.getValue().toString();
				if ("false".equalsIgnoreCase(strVal)) {
					runResult.failedTest++;
					runResult.failedTestName.add(request.name + "." + e.getKey().toString());
					isSuccessful = false;
				}
				tests.put(e.getKey(), e.getValue());
				logger.info(testName + ": " + request.name + " : " + e.getKey() + " - " + e.getValue());
			}
		} catch (Throwable t) {
			isSuccessful = false;
			logger.info("=====FAILED TO EVALUATE TEST AGAINST SERVER RESPONSE======");
			logger.info("##### ERROR CAUSED BY : " + t.getMessage() + " ##### ");
			logger.info("========TEST========");
			logger.info(request.tests);
			logger.info("========TEST========");
			logger.info("========RESPONSE========");
			logger.info(httpResponse.getStatusCode() + "");
			logger.info(httpResponse.getBody());
			logger.info("========RESPONSE========");
			logger.info("=====FAILED TO EVALUATE TEST AGAINST SERVER RESPONSE======");
			// t.printStackTrace();
		} finally {
			Context.exit();
			if (postmanResult != null)
				postmanResult.put("Tests", tests);
		}
		return isSuccessful;
	}

	public boolean runPrerequestScript(PostmanRequest request, PostmanRunResult runResult) {
		if (request.preRequestScript == null || request.preRequestScript.isEmpty()) {
			return true;
		}
		Context cx = Context.enter();
		String testName = "---------------------> POSTMAN test: ";
		boolean isSuccessful = false;
		try {
			Scriptable scope = cx.initStandardObjects();
			PostmanJsVariables jsVar = new PostmanJsVariables(cx, scope, this.var.getEnv());
			jsVar.prepare(null);

			// Evaluate the test script
			cx.evaluateString(scope, request.preRequestScript, testName, 1, null);
			// The results are in the jsVar.tests ???? variable

			// Extract any generated environment variables during the js run.
			jsVar.extractEnvironmentVariables();
			isSuccessful = true;
		} finally {
			Context.exit();
		}
		return isSuccessful;
	}
}
