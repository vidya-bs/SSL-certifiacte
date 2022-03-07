package com.itorix.apiwiz.devstudio.businessImpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itorix.apiwiz.common.model.proxystudio.*;
import com.itorix.apiwiz.devstudio.business.LoadSwagger;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.ParseOptions;
import net.sf.json.JSONSerializer;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoadSwaggerImpl implements LoadSwagger {

	@Override
	public String loadProxySwaggerDetails(String content, String oas) throws JsonProcessingException, JSONException {
		if (oas != null && oas.equals("3.0")) {
			return loadProxySwagger3Details(content);
		} else {
			return loadProxySwagger2Details(content);
		}
	}

	@Override
	public String loadTargetSwaggerDetails(String content, String oas) throws JsonProcessingException, JSONException {
		if (oas != null && oas.equals("3.0")) {
			return loadTargetSwagger3Details(content);
		} else {
			return loadTargetSwagger2Details(content);
		}
	}

	public String loadProxySwagger3Details(String content) throws JsonProcessingException, JSONException {
		OpenAPI api = getOpenAPI(content);

		String basePath = null;
		String name = null;
		String version = null;
		Proxy proxy = new Proxy();
		if (api.getInfo() != null) {
			name = api.getInfo().getTitle();
			version = api.getInfo().getVersion();
		}
		if (api.getServers() != null)
			basePath = api.getServers().get(0).getUrl();
		proxy.setBasePath(basePath);
		proxy.setName(name);
		proxy.setDescription(name);
		Flows flows = new Flows();
		proxy.setFlows(flows);
		proxy.setVersion(getVersion(version));
		List<Flow> flowList = new ArrayList<Flow>();

		net.sf.json.JSONObject jsonSwaggerObject = (net.sf.json.JSONObject) JSONSerializer.toJSON(content);
		net.sf.json.JSONObject jsonPaths = null;
		if (null != jsonSwaggerObject.get("paths")) {
			jsonPaths = (net.sf.json.JSONObject) JSONSerializer
					.toJSON(jsonSwaggerObject.get("paths").toString());
			Set<Map.Entry<String, JSONObject>> set = jsonPaths.entrySet();
			Iterator<Map.Entry<String, JSONObject>> iterator = set.iterator();
			String path = null;
			while (iterator.hasNext()) {
				Map.Entry<String, JSONObject> entry = iterator.next();
				path = entry.getKey();
				net.sf.json.JSONObject operation = (net.sf.json.JSONObject) JSONSerializer.toJSON(entry.getValue());
				for (Object obj : operation.keySet()) {
					String methodType = (String) obj;
					if(isValidMethod(methodType)){
						Object operationJsonValue = operation.get(methodType);
						net.sf.json.JSONObject js = (net.sf.json.JSONObject) operationJsonValue;
						try {
							String op = js.get("operationId").toString().replaceAll("\\s", "");
							Flow flow = new Flow();
							try {
								flow.setName((js.get("operationId").toString()).replaceAll("\\s", ""));
							} catch (Exception e) {
							}
							flow.setPath(path);
							flow.setVerb(methodType.toUpperCase());
							String summary = null;
							try {
								summary = js.get("summary").toString();
							} catch (Exception e) {
							}
							if (summary != null)
								flow.setDescription(summary);
							try {
								String targetBasepath = js.get("x-targetBasepath").toString();
								flow.setTargetBasepath(targetBasepath);
							} catch (Exception ex) {
							}

							try {
								net.sf.json.JSONArray proxyMetadataList = (net.sf.json.JSONArray) js
										.get("x-gw-metadata");
								if (null != proxyMetadataList) {
									List<ProxyMetadata> metadata = new ArrayList<>();
									for (Object key : proxyMetadataList) {
										try {
											ProxyMetadata metadataItem = new ProxyMetadata();
											net.sf.json.JSONObject jsonItem = (net.sf.json.JSONObject) key;
											Set<String> keySet = jsonItem.keySet();
											String keyStr = keySet.stream().findFirst().get();
											net.sf.json.JSONArray element = (net.sf.json.JSONArray) jsonItem
													.get(keyStr);
											if(StringUtils.isNotEmpty(element.stream().findFirst().get().toString())){
												metadataItem.setName(keyStr);
												metadataItem.setValue(element.stream().findFirst().get().toString());
												metadata.add(metadataItem);
											}
										} catch (Exception ex) {
										}
									}
									flow.setMetadata(metadata);
								}
							} catch (Exception ex) {
							}
							flowList.add(flow);
						} catch (Exception e) {
						}
					}
				}
			}
		}



		//		Paths paths = api.getPaths();
		//		Set<Map.Entry<String, PathItem>> pathSet = paths.entrySet();
		//		for (Map.Entry<String, PathItem> pathItem : pathSet) {
		//			PathItem item = pathItem.getValue();
		//			String path = pathItem.getKey();
		//			List<Flow> pathFlows = getFlows(item, path);
		//			if (pathFlows != null) {
		//				for (Flow flow : pathFlows) {
		//					flowList.add(flow);
		//				}
		//			}
		//		}
		Flow flowsArray[] = new Flow[flowList.size()];
		for (int i = 0; i < flowList.size(); i++) {
			flowsArray[i] = flowList.get(i);
		}
		flows.setFlow(flowsArray);
		ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(proxy).toString();
	}


	private List<Flow> getFlows(PathItem item, String path) {
		List<Flow> flowList = new ArrayList<Flow>();
		Operation get = item.getGet();
		Operation put = item.getPut();
		Operation post = item.getPost();
		Operation delete = item.getDelete();
		Operation patch = item.getPatch();
		if (get != null) {
			Flow flow = new Flow();
			get.getOperationId();
			flow.setName(get.getOperationId());
			flow.setPath(path);
			flow.setVerb("GET");
			flow.setDescription(get.getSummary());
			if (!flow.getPath().isEmpty() && !flow.getVerb().isEmpty())
				flowList.add(flow);
		}
		if (put != null) {
			Flow flow = new Flow();
			put.getOperationId();
			flow.setName(put.getOperationId());
			flow.setPath(path);
			flow.setVerb("PUT");
			flow.setDescription(put.getSummary());
			if (!flow.getPath().isEmpty() && !flow.getVerb().isEmpty())
				flowList.add(flow);
		}
		if (post != null) {
			Flow flow = new Flow();
			post.getOperationId();
			flow.setName(post.getOperationId());
			flow.setPath(path);
			flow.setVerb("POST");
			flow.setDescription(post.getSummary());
			if (!flow.getPath().isEmpty() && !flow.getVerb().isEmpty())
				flowList.add(flow);
		}
		if (delete != null) {
			Flow flow = new Flow();
			delete.getOperationId();
			flow.setName(delete.getOperationId());
			flow.setPath(path);
			flow.setVerb("DELETE");
			flow.setDescription(delete.getSummary());
			if (!flow.getPath().isEmpty() && !flow.getVerb().isEmpty())
				flowList.add(flow);
		}
		if (patch != null) {
			Flow flow = new Flow();
			patch.getOperationId();
			flow.setName(patch.getOperationId());
			flow.setPath(path);
			flow.setVerb("PATCH");
			flow.setDescription(patch.getSummary());
			if (!flow.getPath().isEmpty() && !flow.getVerb().isEmpty())
				flowList.add(flow);
		}
		return flowList;
	}


	private OpenAPI getOpenAPI(String swagger) {
		ParseOptions options = new ParseOptions();
		options.setResolve(true);
		options.setResolveCombinators(false);
		options.setResolveFully(true);
		return new OpenAPIV3Parser().readContents(swagger, null, options).getOpenAPI();
	}

	@SuppressWarnings("unchecked")
	public String loadProxySwagger2Details(String content) throws JsonProcessingException, JSONException {
		net.sf.json.JSONObject jsonObject = (net.sf.json.JSONObject) JSONSerializer.toJSON(content);
		String basePath = (String) jsonObject.get("basePath");
		JSONObject nameJson = new JSONObject(jsonObject.get("info").toString());
		String name = (String) nameJson.get("title");
		String version = (String) nameJson.get("version");

		Proxy proxy = new Proxy();
		proxy.setBasePath(basePath);
		proxy.setName(name);
		proxy.setDescription(name);
		Flows flows = new Flows();
		proxy.setFlows(flows);
		proxy.setVersion(getVersion(version));
		List<Flow> flowList = new ArrayList<Flow>();
		if (null != jsonObject.get("paths")) {
			net.sf.json.JSONObject paths = (net.sf.json.JSONObject) JSONSerializer
					.toJSON(jsonObject.get("paths").toString());
			Set<Map.Entry<String, JSONObject>> set = paths.entrySet();
			Iterator<Map.Entry<String, JSONObject>> iterator = set.iterator();
			String path = null;
			while (iterator.hasNext()) {
				Map.Entry<String, JSONObject> entry = iterator.next();
				path = entry.getKey();
				net.sf.json.JSONObject operation = (net.sf.json.JSONObject) JSONSerializer.toJSON(entry.getValue());
				for (Object obj : operation.keySet()) {
					String methodType = (String) obj;
					Object operationJsonValue = operation.get(methodType);
					if (!operationJsonValue.equals(null)) {
						Flow flow = new Flow();
						// flowList.add(flow);
						try {
							net.sf.json.JSONObject js = (net.sf.json.JSONObject) operationJsonValue;
							try {
								flow.setName((js.get("operationId").toString()).replaceAll("\\s", ""));
							} catch (Exception e) {
							}
							flow.setPath(path);
							flow.setVerb(methodType.toUpperCase());
							String summary = null;
							try {
								summary = js.get("summary").toString();
							} catch (Exception e) {
							}
							if (summary != null)
								flow.setDescription(summary);
							try {
								String targetBasepath = js.get("x-targetBasepath").toString();
								flow.setTargetBasepath(targetBasepath);
							} catch (Exception ex) {
							}

							try {
								net.sf.json.JSONArray proxyMetadataList = (net.sf.json.JSONArray) js
										.get("x-gw-metadata");
								if (null != proxyMetadataList) {
									List<ProxyMetadata> metadata = new ArrayList<>();
									for (Object key : proxyMetadataList) {
										try {
											ProxyMetadata metadataItem = new ProxyMetadata();
											net.sf.json.JSONObject jsonItem = (net.sf.json.JSONObject) key;
											Set<String> keySet = jsonItem.keySet();
											String keyStr = keySet.stream().findFirst().get();
											net.sf.json.JSONArray element = (net.sf.json.JSONArray) jsonItem
													.get(keyStr);
											metadataItem.setName(keyStr);
											metadataItem.setValue(element.stream().findFirst().get().toString());
											metadata.add(metadataItem);
										} catch (Exception ex) {
										}
									}
									flow.setMetadata(metadata);
								}
							} catch (Exception ex) {
							}

						} catch (Exception e) {
						}

						if (flow.getPath() != null && flow.getVerb() != null)
							if (!flow.getPath().isEmpty() && !flow.getVerb().isEmpty())
								flowList.add(flow);
					}
				}
			}
		}
		Flow flowsArray[] = new Flow[flowList.size()];
		for (int i = 0; i < flowList.size(); i++) {
			flowsArray[i] = flowList.get(i);
		}
		flows.setFlow(flowsArray);
		ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(proxy).toString();
	}

	public String loadTargetSwagger3Details(String content) throws JsonProcessingException, JSONException {
		OpenAPI api = getOpenAPI(content);
		String basePath = null;
		String name = null;
		Target proxy = new Target();
		if (api.getInfo() != null) {
			name = api.getInfo().getTitle();
		}
		if (api.getServers() != null)
			basePath = api.getServers().get(0).getUrl();

		proxy.setBasePath(basePath);
		proxy.setName(name);
		proxy.setDescription(name);
		Flows flows = new Flows();
		proxy.setFlows(flows);
		List<Flow> flowList = new ArrayList<Flow>();
		Paths paths = api.getPaths();
		Set<Map.Entry<String, PathItem>> pathSet = paths.entrySet();
		for (Map.Entry<String, PathItem> pathItem : pathSet) {
			PathItem item = pathItem.getValue();
			String path = pathItem.getKey();
			List<Flow> pathFlows = getFlows(item, path);
			if (pathFlows != null) {
				for (Flow flow : pathFlows) {
					flowList.add(flow);
				}
			}
		}
		Flow flowsArray[] = new Flow[flowList.size()];
		for (int i = 0; i < flowList.size(); i++) {
			flowsArray[i] = flowList.get(i);
		}
		flows.setFlow(flowsArray);
		ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(proxy).toString();
	}

	@SuppressWarnings("unchecked")
	public String loadTargetSwagger2Details(String content) throws JsonProcessingException, JSONException {
		net.sf.json.JSONObject jsonObject = (net.sf.json.JSONObject) JSONSerializer.toJSON(content);
		String basePath = (String) jsonObject.get("basePath");
		JSONObject nameJson = new JSONObject(jsonObject.get("info").toString());
		String name = (String) nameJson.get("title");
		Target proxy = new Target();
		proxy.setBasePath(basePath);
		proxy.setName(name);
		proxy.setDescription(name);
		Flows flows = new Flows();
		proxy.setFlows(flows);
		List<Flow> flowList = new ArrayList<Flow>();
		if (null != jsonObject.get("paths")) {
			net.sf.json.JSONObject paths = (net.sf.json.JSONObject) JSONSerializer
					.toJSON(jsonObject.get("paths").toString());
			Set<Map.Entry<String, JSONObject>> set = paths.entrySet();
			Iterator<Map.Entry<String, JSONObject>> iterator = set.iterator();
			String path = null;
			while (iterator.hasNext()) {
				Map.Entry<String, JSONObject> entry = iterator.next();
				path = entry.getKey();
				net.sf.json.JSONObject operation = (net.sf.json.JSONObject) JSONSerializer.toJSON(entry.getValue());
				for (Object obj : operation.keySet()) {
					String methodType = (String) obj;
					Object operationJsonValue = operation.get(methodType);
					if (!operationJsonValue.equals(null)) {
						Flow flow = new Flow();
						try {
							net.sf.json.JSONObject js = (net.sf.json.JSONObject) operationJsonValue;
							try {
								flow.setName((js.get("operationId").toString()).replaceAll("\\s", ""));
							} catch (Exception e) {
							}
							flow.setPath(path);
							flow.setVerb(methodType.toUpperCase());
							String description = null;
							try {
								description = js.get("summary").toString();
							} catch (Exception e) {
								try {
									description = js.get("operationId").toString().replaceAll("\\s", "");
								} catch (Exception ex) {
								}
							}
							flow.setDescription(description);
							try {
								String targetBasepath = js.get("x-targetBasepath").toString();
								flow.setTargetBasepath(targetBasepath);
							} catch (Exception ex) {
							}
						} catch (Exception e) {
						}
						if (flow.getPath() != null && flow.getVerb() != null)
							if (!flow.getPath().isEmpty() && !flow.getVerb().isEmpty())
								flowList.add(flow);
					}
				}
			}
		}
		Flow flowsArray[] = new Flow[flowList.size()];
		for (int i = 0; i < flowList.size(); i++) {
			flowsArray[i] = flowList.get(i);
		}
		flows.setFlow(flowsArray);
		ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(proxy).toString();
	}

	private String getVersion(String version) {
		String proxyVersion = "v1";
		if (version.length() >= 2 && version.length() <= 5) {
			Pattern regexPattern = Pattern.compile("[a-zA-z]\\d\\d?");
			Matcher matcher = regexPattern.matcher(version);
			if (matcher.find()) {
				proxyVersion = version;
			} else {
				regexPattern = Pattern.compile("\\d\\d?");
				matcher = regexPattern.matcher(version);
				if (matcher.find()) {
					proxyVersion = "v" + version;
				}
			}
		} else if (version.length() <= 2) {
			Pattern regexPattern = Pattern.compile("[a-zA-z]\\d?");
			Matcher matcher = regexPattern.matcher(version);
			if (matcher.find()) {
				proxyVersion = version;
			} else {
				regexPattern = Pattern.compile("\\d\\d?");
				matcher = regexPattern.matcher(version);
				if (matcher.find()) {
					proxyVersion = "v" + version;
				}
			}
		}
		return proxyVersion;
	}

	private boolean isValidMethod(String method){
		switch(method.toUpperCase()){
		case "POST" : {
			return true;
		}
		case "PUT" : {
			return true;
		}
		case "GET" : {
			return true;
		}
		case "DELETE" : {
			return true;
		}
		case "PATCH" : {
			return true;
		}
		}
		return false;
	}
}
