package com.itorix.apiwiz.devstudio.businessImpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.itorix.apiwiz.common.model.proxystudio.Flow;
import com.itorix.apiwiz.common.model.proxystudio.Flows;
import com.itorix.apiwiz.common.model.proxystudio.Proxy;
import com.itorix.apiwiz.common.model.proxystudio.Target;
import com.itorix.apiwiz.devstudio.business.LoadSwagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.ParseOptions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.sf.json.JSONSerializer;


public class LoadSwaggerImpl implements LoadSwagger{


	@Override
	public String loadProxySwaggerDetails(String  content, String oas)	throws JsonProcessingException, JSONException {
		if(oas != null && oas.equals("3.0")){
			return loadProxySwagger3Details(content);
		} else{
			return loadProxySwagger2Details(content);
		}
	}

	@Override
	public String  loadTargetSwaggerDetails(String  content, String oas)	throws JsonProcessingException, JSONException {
		if(oas != null && oas.equals("3.0")){
			return loadTargetSwagger3Details(content);
		} else{
			return loadTargetSwagger2Details(content);
		}
	}

	public String loadProxySwagger3Details(String  content)	throws JsonProcessingException, JSONException {
		OpenAPI api = getOpenAPI(content);
		String basePath = null;
		String name = null;
		String version = null;
		Proxy proxy = new Proxy();
		if(api.getInfo()!= null){
			name = api.getInfo().getTitle();
			version = api.getInfo().getVersion();
		}
		if(api.getServers() != null)
			basePath= api.getServers().get(0).getUrl();
		proxy.setBasePath(basePath);
		proxy.setName(name);
		proxy.setDescription(name);
		Flows flows = new Flows();
		proxy.setFlows(flows);
		proxy.setVersion(getVersion(version));
		List<Flow> flowList = new ArrayList<Flow>();
		Paths paths = api.getPaths();
		Set<Map.Entry<String, PathItem>> pathSet = paths.entrySet();
		for (Map.Entry<String, PathItem> pathItem :pathSet){
			PathItem item = pathItem.getValue();
			String path = pathItem.getKey();
			List<Flow> pathFlows = getFlows(item, path);
			if(pathFlows != null){
				for(Flow flow : pathFlows){
					flowList.add(flow);
				}
			}
		}
		Flow flowsArray[] = new Flow[flowList.size()];              
		for(int i =0;i<flowList.size();i++){
			flowsArray[i] = flowList.get(i);
		}
		flows.setFlow(flowsArray);
		ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(proxy).toString();
	}



	private List<Flow> getFlows(PathItem item, String path ){
		List<Flow> flowList = new ArrayList<Flow>();
		Operation get = item.getGet();
		Operation put = item.getPut();
		Operation post = item.getPost();
		Operation delete = item.getDelete();
		Operation patch = item.getPatch();
		if(get != null){
			Flow flow = new Flow();
			get.getOperationId();
			flow.setName(get.getOperationId());
			flow.setPath(path);
			flow.setVerb("GET");
			flow.setDescription(get.getSummary());
			if(!flow.getPath().isEmpty() && !flow.getVerb().isEmpty())
			flowList.add(flow);
		}
		if(put != null){
			Flow flow = new Flow();
			put.getOperationId();
			flow.setName(put.getOperationId());
			flow.setPath(path);
			flow.setVerb("PUT");
			flow.setDescription(put.getSummary());
			if(!flow.getPath().isEmpty() && !flow.getVerb().isEmpty())
			flowList.add(flow);
		}
		if(post != null){
			Flow flow = new Flow();
			post.getOperationId();
			flow.setName(post.getOperationId());
			flow.setPath(path);
			flow.setVerb("POST");
			flow.setDescription(post.getSummary());
			if(!flow.getPath().isEmpty() && !flow.getVerb().isEmpty())
			flowList.add(flow);
		}
		if(delete != null){
			Flow flow = new Flow();
			delete.getOperationId();
			flow.setName(delete.getOperationId());
			flow.setPath(path);
			flow.setVerb("DELETE");
			flow.setDescription(delete.getSummary());
			if(!flow.getPath().isEmpty() && !flow.getVerb().isEmpty())
			flowList.add(flow);
		}
		if(patch != null){
			Flow flow = new Flow();
			patch.getOperationId();
			flow.setName(patch.getOperationId());
			flow.setPath(path);
			flow.setVerb("PATCH");
			flow.setDescription(delete.getSummary());
			if(!flow.getPath().isEmpty() && !flow.getVerb().isEmpty())
			flowList.add(flow);
		}
		return flowList;
	}

	private  OpenAPI getOpenAPI(String swagger){
		ParseOptions options = new ParseOptions();
		options.setResolve(true);
		options.setResolveCombinators(false);
		options.setResolveFully(true);
		return new OpenAPIV3Parser().readContents(swagger, null, options).getOpenAPI();
	}

	@SuppressWarnings("unchecked")
	public String loadProxySwagger2Details(String  content)	throws JsonProcessingException, JSONException {
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
		if(null != jsonObject.get("paths")){
			net.sf.json.JSONObject paths = (net.sf.json.JSONObject) JSONSerializer.toJSON(jsonObject.get("paths").toString());
			Set<Map.Entry<String, JSONObject>> set = paths.entrySet();
			Iterator<Map.Entry<String, JSONObject>> iterator = set.iterator();
			String path = null;
			while (iterator.hasNext()) {
				Map.Entry<String, JSONObject> entry = iterator.next();
				path = entry.getKey();
				net.sf.json.JSONObject operation = (net.sf.json.JSONObject) JSONSerializer.toJSON(entry.getValue());
				for(Object obj : operation.keySet()){
					String methodType = (String)obj; 
					Object operationJsonValue = operation.get(methodType);
					if(!operationJsonValue.equals(null))
					{
						Flow flow = new Flow();
						flowList.add(flow);
						try{
							net.sf.json.JSONObject js = (net.sf.json.JSONObject)operationJsonValue;
							try{
								flow.setName((js.get("operationId").toString()).replaceAll("\\s",""));
							}catch(Exception e){}
							flow.setPath(path);
							flow.setVerb(methodType.toUpperCase());
							String summary =null;
							try{
								summary = js.get("summary").toString();
							}catch(Exception e){}
							if(summary!=null)
								flow.setDescription(summary);
						}catch(Exception e){}
					}
				}
			}
		}
		Flow flowsArray[] = new Flow[flowList.size()];              
		for(int i =0;i<flowList.size();i++){
			flowsArray[i] = flowList.get(i);
		}
		flows.setFlow(flowsArray);
		ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(proxy).toString();
	}

	public String loadTargetSwagger3Details(String  content)	throws JsonProcessingException, JSONException {
		OpenAPI api = getOpenAPI(content);
		String basePath = null;
		String name = null;
		Target proxy = new Target();
		if(api.getInfo()!= null){
			name = api.getInfo().getTitle();
		}
		if(api.getServers() != null)
			basePath= api.getServers().get(0).getUrl();

		proxy.setBasePath(basePath);
		proxy.setName(name);
		proxy.setDescription(name);
		Flows flows = new Flows();
		proxy.setFlows(flows);
		List<Flow> flowList = new ArrayList<Flow>();
		Paths paths = api.getPaths();
		Set<Map.Entry<String, PathItem>> pathSet = paths.entrySet();
		for (Map.Entry<String, PathItem> pathItem :pathSet){
			PathItem item = pathItem.getValue();
			String path = pathItem.getKey();
			List<Flow> pathFlows = getFlows(item, path);
			if(pathFlows != null){
				for(Flow flow : pathFlows){
					flowList.add(flow);
				}
			}
		}
		Flow flowsArray[] = new Flow[flowList.size()];              
		for(int i =0;i<flowList.size();i++){
			flowsArray[i] = flowList.get(i);
		}
		flows.setFlow(flowsArray);
		ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(proxy).toString();
	}


	@SuppressWarnings("unchecked")
	public String loadTargetSwagger2Details(String  content)	throws JsonProcessingException, JSONException {
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
		if(null != jsonObject.get("paths")){
			net.sf.json.JSONObject paths = (net.sf.json.JSONObject) JSONSerializer.toJSON(jsonObject.get("paths").toString());
			Set<Map.Entry<String, JSONObject>> set = paths.entrySet();
			Iterator<Map.Entry<String, JSONObject>> iterator = set.iterator();
			String path = null;
			while (iterator.hasNext()) {
				Map.Entry<String, JSONObject> entry = iterator.next();
				path = entry.getKey();
				net.sf.json.JSONObject operation = (net.sf.json.JSONObject) JSONSerializer.toJSON(entry.getValue());
				for(Object obj : operation.keySet()){
					String methodType = (String)obj; 
					Object operationJsonValue = operation.get(methodType);
					if(!operationJsonValue.equals(null))
					{
						try{
							Flow flow = new Flow();
							flowList.add(flow);
							net.sf.json.JSONObject js = (net.sf.json.JSONObject)operationJsonValue;
							try{
								flow.setName((js.get("operationId").toString()).replaceAll("\\s",""));
							}catch(Exception e){}
							flow.setPath(path);
							flow.setVerb(methodType.toUpperCase());
							String description =null;
							try{
								description = js.get("summary").toString();
							}catch (Exception e){
								try{
									description = js.get("operationId").toString().replaceAll("\\s","");
								}catch(Exception ex){}
							}
							flow.setDescription(description);
						}catch (Exception e){}	
					}
				}
			}
		}
		Flow flowsArray[] = new Flow[flowList.size()];              
		for(int i =0;i<flowList.size();i++){
			flowsArray[i] = flowList.get(i);
		}
		flows.setFlow(flowsArray);
		ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(proxy).toString();
	}

	private String getVersion(String version){
		String proxyVersion = "v1";
		System.out.println(version.length());
		if(version.length()>=2 && version.length()<=5)
		{
			Pattern regexPattern = Pattern.compile("[a-zA-z]\\d\\d?");
			Matcher matcher = regexPattern.matcher(version);
			if(matcher.find()){
				proxyVersion = version;
			}
			else{
				regexPattern = Pattern.compile("\\d\\d?");
				matcher = regexPattern.matcher(version);
				if(matcher.find()){
					proxyVersion = "v" +  version;
				}
			}
		}
		else if(version.length()<=2){
			Pattern regexPattern = Pattern.compile("[a-zA-z]\\d?");
			Matcher matcher = regexPattern.matcher(version);
			if(matcher.find()){
				proxyVersion = version;
			}
			else {
				regexPattern = Pattern.compile("\\d\\d?");
				matcher = regexPattern.matcher(version);
				if(matcher.find()){
					proxyVersion = "v"+version;
				}
			}
		}
		return proxyVersion;
	}

}
