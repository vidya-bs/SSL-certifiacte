package com.itorix.apiwiz.devstudio.businessImpl;

import com.itorix.apiwiz.common.model.proxystudio.CodeGenHistory;
import com.itorix.apiwiz.common.model.proxystudio.Flow;
import com.itorix.apiwiz.common.model.proxystudio.Flows;
import com.itorix.apiwiz.common.model.proxystudio.Folder;
import com.itorix.apiwiz.common.model.proxystudio.Proxy;
import com.itorix.apiwiz.common.model.proxystudio.ProxyConfig;
import com.itorix.apiwiz.common.model.proxystudio.Target;
import com.itorix.apiwiz.devstudio.dao.MongoConnection;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("rawtypes")
public class ApigeeProxyGeneration {

	@Autowired
	CommonsGen commonsGen;

	@Autowired
	MongoConnection mongoConnection;

	private String proxyName;
	private String basePath;
	private String proxyDescription;
	private List<String> targetNames;
	private String dstPolicies = "";
	private String dstProxies = "";
	private String dstResourcesXSL = "";
	private String dstResourcesJSC = "";
	private String dstResourcesXsd = "";
	private String dstResourcesJava = "";
	private String dstRootFolder = "";

	public void generateCommonCode(String connectorId,Folder commonsFolder) throws IOException, TemplateException {
		Folder templates = commonsFolder.getFile("policies");
		for (Folder tmplFile : templates.getFiles()) {
			String content = mongoConnection.getFile(connectorId,tmplFile.getName());
			writeFile(content, dstPolicies + File.separatorChar + tmplFile.getName());
		}
		processResources(connectorId,commonsFolder.getFile("resources"));
	}

	private void processResources(String connectorId,Folder templates) throws IOException, TemplateException {
		try {
			for (Folder tmplFile : templates.getFiles()) {
				if (tmplFile.isFolder()) {
					String filePath = "";
					if (tmplFile.getName().equals(ProxyConfig.FLDR_XSD))
						filePath = dstResourcesXsd;
					else if (tmplFile.getName().equals(ProxyConfig.FLDR_JAVA))
						filePath = dstResourcesJava;
					else if (tmplFile.getName().equals(ProxyConfig.FLDR_JSC))
						filePath = dstResourcesJSC;
					else if (tmplFile.getName().equals(ProxyConfig.FLDR_XSL))
						filePath = dstResourcesXSL;
					if (!filePath.equals(""))
						for (Folder resourceFile : tmplFile.getFiles()) {
							String content = mongoConnection.getFile(connectorId,resourceFile.getName());
							writeFile(content, filePath + File.separatorChar + resourceFile.getName());
						}
				}
			}
		} catch (Exception e) {

		}
	}

	public void generateProxyCode(Folder proxyFolder, Folder commonFolder, CodeGenHistory cg, String dir)
			throws IOException, TemplateException {
		targetNames = new ArrayList<>();
		Proxy proxy = cg.getProxy();
		proxyName = proxy.getName().split("_")[0];
		dstRootFolder = dir; // + "API" + File.separatorChar + "Proxies" +
		// File.separatorChar + proxyName +
		// File.separatorChar + proxy.getVersion();
		createDestinationFolderStructure(dstRootFolder);
		if (cg.getTarget() != null) {
			for (Target target : cg.getTarget())
				targetNames.add(target.getName());
		}
		basePath = proxy.getBasePath();
		proxyDescription = proxy.getDescription();
		List<Folder> files = proxyFolder.getFiles();
		for (Folder file : files)
			if (!file.isFolder())
				processProxyTemplate(dstRootFolder, cg.getConnectorId()+"-"+file.getName());
		Folder proxyFile = proxyFolder.getFile("proxies");
		files = proxyFile.getFiles();
		for (Folder file : files)
			if (!file.isFolder())
				processProxyEndpointTemplate(cg, file.getName());
		if (cg.getProxy().getFlows() != null) {
			processPolicyTemplates(proxy.getFlows(), proxyFolder.getFile("policies"), cg);
		}
		generateCommonCode(cg.getConnectorId(), commonFolder);
	}

	private void processProxyTemplate(String destRootFolder, String fileName) throws IOException, TemplateException {
		Template template = getTemplate(fileName);
		String dstFileName = destRootFolder + File.separatorChar + proxyName + "Proxy.xml";
		Writer file = new FileWriter(dstFileName);
		Map<String, Object> data = new HashMap<String, Object>();
		Map<String, Object> proxy = new HashMap<String, Object>();
		data.put("proxy", proxy);
		proxy.put(ProxyConfig.STR_NAME, proxyName);
		proxy.put("description", proxyDescription);
		proxy.put("targetEndpointList", targetNames);
		data.put("xslResources", getFileList(dstResourcesXSL, ProxyConfig.XSLT_FILE_EXT, false));
		data.put("policies", getFileList(dstPolicies, ProxyConfig.XML_FILE_EXT, true));
		data.put("jscResources", getFileList(dstResourcesJSC, ProxyConfig.JS_FILE_EXT, false));
		template.process(data, file);
		file.flush();
		file.close();
	}

	private void processProxyEndpointTemplate(CodeGenHistory cg, String fileName)
			throws IOException, TemplateException {
		// Template template =
		// getTemplateFromFile("/opt/itorix/temp/ProxyGen/proxies/ProxyEndpoint.xml.ftl");
		Template template = getTemplate(cg.getConnectorId()+"-"+"ProxyEndpoint.xml.ftl");
		String dstProxiesFileName =
				dstProxies + File.separatorChar + proxyName + ProxyConfig.XML_FILE_EXT;

		Map<String, Object> data = new HashMap<String, Object>();
		Map<String, Object> proxy = new HashMap<String, Object>();
		data.put("basePath", basePath);
		List<String> virtualHostList = new ArrayList<String>();
		//virtualHostList.add("secure");
		//virtualHostList.add("default");
		data.put("proxy", proxy);
		proxy.put("virtualHostList", virtualHostList);
		proxy.put("name", proxyName);
		proxy.put("description", proxyDescription);
		proxy.put("targetEndpointList", processRouteRuleTemplate(cg.getTarget()));
		List<Object> apiDetails = new ArrayList<Object>();
		data.put("apis", apiDetails);
		if (cg.getProxy().getFlows() != null) {
			Flow[] flows = cg.getProxy().getFlows().getFlow();
			for (Flow flow : flows) {
				Map<String, Object> mapApi = new HashMap<String, Object>();
				apiDetails.add(mapApi);
				mapApi.put("pathSuffix", flow.getPath());
				mapApi.put("verb", flow.getVerb().toUpperCase());
				mapApi.put("name", flow.getName());
				mapApi.put("description", flow.getDescription());
				if (flow.getPolicyTemplates() != null) {
					Map flowMap = commonsGen.createMap(flow.getPolicyTemplates());
					mapApi.put("flowPolicyTemplate", flowMap.get("policyTemplate"));
					mapApi.put("flowPolicyName", flowMap.get("policyName"));
				}

				if(null != flow.getMetadata()){
					Map<String, String> metadata = new HashMap<String, String>();
					for( com.itorix.apiwiz.common.model.proxystudio.ProxyMetadata proxyMetadata : flow.getMetadata()){
						metadata.put(proxyMetadata.getName().replaceAll("-", "_"), proxyMetadata.getValue());
					}
					mapApi.put("metadata", metadata);
				}
			}
		}
		if (null != cg.getProxyMetadata() && !cg.getProxyMetadata().isEmpty()) {
			data.put("proxyMetadata", cg.getProxyMetadata());
		}
		Writer file = new FileWriter(dstProxiesFileName);
		if (cg.getPolicyTemplates() != null) {
			Map commonMap = commonsGen.createMap(cg);
			data.put("policyTemplate", commonMap.get("policyTemplate"));
			data.put("policyName", commonMap.get("policyName"));
		}
		template.process(data, file);
		file.flush();
		file.close();
	}

	private void processPolicyTemplates(Flows apiList, Folder templates, CodeGenHistory cg)
			throws TemplateException, IOException {
		List<Folder> tmplfiles = templates.getFiles();
		for (Folder tmplFile : tmplfiles) {

			Template template = getTemplate(cg.getConnectorId()+"-"+tmplFile.getName());
			for (Flow flow : apiList.getFlow()) {
				String apiName = flow.getName();
				String fileName = removeFileExtension(tmplFile.getName(), true);
				String dstPoliciesFile = dstPolicies + File.separatorChar
						+ tmplFile.getName().replaceAll(fileName, fileName + "-" + apiName );

				final Map<String, Object> apiDtls = new HashMap<String, Object>();
				Map<String, Object> apiMap = new HashMap<String, Object>();
				apiDtls.put(ProxyConfig.STR_API, apiMap);
				apiMap.put("name", flow.getName());
				apiMap.put("description", flow.getDescription());
				apiMap.put("verb", flow.getVerb());
				apiMap.put("pathSuffix", flow.getPath());
				apiDtls.put("targetService", "");
				apiDtls.put("resourceTargetBasePath", flow.getTargetBasepath() != null ? flow.getTargetBasepath() : "");
				apiDtls.put("targetName", flow.getTargetName() != null ? flow.getTargetName() : "");
				apiDtls.put("targetOperation", flow.getTargetOperation() != null ? flow.getTargetOperation() : "");
				if (cg.getPolicyTemplates() != null) {
					Map commonMap = commonsGen.createMap(cg);
					apiDtls.put("policyTemplate", commonMap.get("policyTemplate"));
					apiDtls.put("policyName", commonMap.get("policyName"));
				}
				if (flow.getPolicyTemplates() != null) {
					Map flowMap = commonsGen.createMap(flow.getPolicyTemplates());
					apiDtls.put("flowPolicyTemplate", flowMap.get("policyTemplate"));
					apiDtls.put("flowPolicyName", flowMap.get("policyName"));
				}
				if(null != flow.getMetadata()){
					Map<String, String> metadata = new HashMap<String, String>();
					for( com.itorix.apiwiz.common.model.proxystudio.ProxyMetadata proxyMetadata : flow.getMetadata()){
						metadata.put(proxyMetadata.getName().replaceAll("-", "_"), proxyMetadata.getValue());
					}
					apiDtls.put("metadata", metadata);
				}
				if(null != cg.getProxyMetadata()){
					apiDtls.put("proxyMetadata", cg.getProxyMetadata());
				}
				boolean canProcess = true;
				if(fileName.contains("x-gw-cache-resource")){
					canProcess = false;
					if(null != flow.getMetadata()){
						for( com.itorix.apiwiz.common.model.proxystudio.ProxyMetadata proxyMetadata : flow.getMetadata()){
							if(proxyMetadata.getName().equals("x-gw-cache-resource")){
								if(StringUtils.isNotEmpty(proxyMetadata.getValue())){
									canProcess = true;
									break;
								}
							}
							if(proxyMetadata.getName().equals("x-gw-cache-key")){
								String string = proxyMetadata.getValue();
								List<String> cacheKeys = new ArrayList<String>(Arrays.asList(string.split(" , ")));
								apiDtls.put("cacheKeys", cacheKeys);
							}
							if(proxyMetadata.getName().equals("x-gw-cache-timeout-unit")){
								if(proxyMetadata.getValue().equals("days")) {
									for( com.itorix.apiwiz.common.model.proxystudio.ProxyMetadata proxyMetadata1 : flow.getMetadata()){
										if(proxyMetadata1.getName().equals("x-gw-cache-timeout")) {
											String count = proxyMetadata1.getValue();
											long timeunit = Integer.valueOf(count) * 86400;
											apiDtls.put("cacheTimeout", timeunit);
										}
									}
								} else {
									for( com.itorix.apiwiz.common.model.proxystudio.ProxyMetadata proxyMetadata1 : flow.getMetadata()){
										if(proxyMetadata1.getName().equals("x-gw-cache-timeout")) {
											String count = proxyMetadata1.getValue();
											apiDtls.put("cacheTimeout", count);
										}
									}
								}
							}
						}
					}
					dstPoliciesFile = dstPoliciesFile.replace(ProxyConfig.FTL_FILE_EXT, "").replace("-x-gw-cache-resource", "")
							.replace(ProxyConfig.STR_ALL, "");
				}
				if(fileName.contains("cf-Assign-Metadata-Variables")){
					canProcess = false;
					if(null!=cg.getProxyMetadata() && !cg.getProxyMetadata().isEmpty()){
						canProcess =true;
					}
					dstPoliciesFile = dstPoliciesFile.replace(ProxyConfig.FTL_FILE_EXT, "")
							.replace(ProxyConfig.STR_ALL, "");
				}else{
					dstPoliciesFile = dstPoliciesFile.replace(ProxyConfig.FTL_FILE_EXT, "").replace(ProxyConfig.STR_GET, "")
							.replace(ProxyConfig.STR_ALL, "");
				}
				if(canProcess){
					Writer reqFile = new FileWriter(dstPoliciesFile);
					template.process(apiDtls, reqFile);
					reqFile.flush();
					reqFile.close();
				}
			}
		}
	}

	public static String removeFileExtension(String filename, boolean removeAllExtensions) {
		if (filename == null || filename.isEmpty()) {
			return filename;
		}

		String extPattern = "(?<!^)[.]" + (removeAllExtensions ? ".*" : "[^.]*$");
		return filename.replaceAll(extPattern, "");
	}

	private List<String> processRouteRuleTemplate(List<Target> targets) throws IOException, TemplateException {
		// Map<String,Object> proxyCfgDtls = new HashMap<String,Object>();
		// Map<String,Object> proxy = new HashMap<String,Object>();
		List<String> targetEndPointList = new ArrayList<String>();
		// proxyCfgDtls.put("proxy", proxy);
		// proxy.put("targetEndpointList", targetEndPointList);
		if (targets != null) {
			for (Target target : targets)
				targetEndPointList.add(target.getName());
		} else {
			targetEndPointList.add("default");
		}
		// Template template = getTemplate("routerules.flowfrag.ftl");
		// String dstFile = dstApiFragflows + File.separatorChar + proxyName
		// +"_routerules.flowfrag";
		// Writer wFile = new FileWriter(dstFile);
		// template.process(proxyCfgDtls, wFile);
		// wFile.flush();
		// wFile.close();
		return targetEndPointList;
	}

	@SuppressWarnings("deprecation")
	public Template getTemplate(String file) throws IOException {
		String reader = mongoConnection.getFile(file);
		Configuration conf = new Configuration();
		StringTemplateLoader tloader = new StringTemplateLoader();
		conf.setTemplateLoader(tloader);
		tloader.putTemplate(file, reader);
		conf.setObjectWrapper(new DefaultObjectWrapper());
		Template template = conf.getTemplate(file);
		return template;
	}

	@SuppressWarnings("deprecation")
	public Template getTemplateFromFile(String file) throws IOException {
		File templateFile = new File(file);
		String fileName = templateFile.getName();
		String reader = FileUtils.readFileToString(templateFile);
		Configuration conf = new Configuration();
		StringTemplateLoader tloader = new StringTemplateLoader();
		conf.setTemplateLoader(tloader);
		tloader.putTemplate(fileName, reader);
		conf.setObjectWrapper(new DefaultObjectWrapper());
		Template template = conf.getTemplate(fileName);
		return template;
	}

	private List<String> getFileList(String rootFolder, String ext, boolean removeFileExt) {
		List<String> fileList = new ArrayList<String>();
		File file = new File(rootFolder);
		String fileNames[] = file.list();
		for (String fileName : fileNames) {
			if (fileName.endsWith(ext)) {
				if (removeFileExt) {
					fileName = fileName.replace(ext, "");
				}
				fileList.add(fileName);
			}
		}
		return fileList;
	}

	private void createDestinationFolderStructure(String proxyRootFolder) {

		dstPolicies = proxyRootFolder + File.separatorChar + ProxyConfig.FLDR_POLICIES;
		File dir = new File(dstPolicies);
		dir.mkdirs();

		dstProxies = proxyRootFolder + File.separatorChar + "proxies";
		dir = new File(dstProxies);
		dir.mkdirs();

		dstResourcesXSL = proxyRootFolder + File.separatorChar + ProxyConfig.FLDR_RESOURCES + File.separatorChar
				+ ProxyConfig.FLDR_XSL;
		dir = new File(dstResourcesXSL);
		dir.mkdirs();

		dstResourcesJSC = proxyRootFolder + File.separatorChar + ProxyConfig.FLDR_RESOURCES + File.separatorChar
				+ ProxyConfig.FLDR_JSC;
		dir = new File(dstResourcesJSC);
		dir.mkdirs();

		dstResourcesJava = proxyRootFolder + File.separatorChar + ProxyConfig.FLDR_RESOURCES + File.separatorChar
				+ ProxyConfig.FLDR_JAVA;
		dir = new File(dstResourcesJava);
		dir.mkdirs();

		dstResourcesXsd = proxyRootFolder + File.separatorChar + ProxyConfig.FLDR_RESOURCES + File.separatorChar
				+ ProxyConfig.FLDR_XSD;
		dir = new File(dstResourcesXsd);
		dir.mkdirs();
	}

	private boolean writeFile(String content, String name) throws IOException {
		File file = new File(name);
		FileOutputStream fop = new FileOutputStream(file);
		if (!file.exists()) {
			file.createNewFile();
		}
		byte[] contentInBytes = content.getBytes();
		fop.write(contentInBytes);
		fop.flush();
		fop.close();
		return true;
	}
}
