package com.itorix.apiwiz.devstudio.businessImpl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import com.itorix.apiwiz.common.model.proxystudio.CodeGenHistory;
import com.itorix.apiwiz.common.model.proxystudio.Flow;
import com.itorix.apiwiz.common.model.proxystudio.Flows;
import com.itorix.apiwiz.common.model.proxystudio.Folder;
import com.itorix.apiwiz.common.model.proxystudio.Proxy;
import com.itorix.apiwiz.common.model.proxystudio.ProxyConfig;
import com.itorix.apiwiz.common.model.proxystudio.Target;
import com.itorix.apiwiz.common.properties.ApplicationProperties;
import com.itorix.apiwiz.devstudio.dao.MongoConnection;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

@Component
@SuppressWarnings("rawtypes")
public class ProxyGen {
	@Autowired
	CommonsGen commonsGen;

	@Autowired
	ApplicationProperties applicationProperties;

	@Autowired
	MongoConnection mongoConnection;

	private String proxyName;
	private String basePath;
	private String proxyDescription;
	private List<String> targetNames = new ArrayList<String>();
	private String dstApiFragflows = "";
	private String dstPolicies = "";
	private String dstProxies = "";
	private String dstResourcesXSL = "";
	private String dstResourcesJSC = "";
	private String dstResourcesXsd = "";
	private String dstResourcesJava = "";
	private String dstRootFolder = "";

	public void generateCommonCode(Folder commonsFolder, CodeGenHistory cg, String dir)
			throws IOException, TemplateException {
		commonsGen.generateCommonsCode(commonsFolder, cg, dir);
	}

	public void generateProxyCode(Folder proxyFolder, CodeGenHistory cg, String dir)
			throws IOException, TemplateException {
		Proxy proxy = cg.getProxy();
		if (cg.getTarget() != null) {
			for (Target target : cg.getTarget())
				targetNames.add(target.getName());
		}
		proxyName = proxy.getName().split("_")[0];
		basePath = proxy.getBasePath();

		Flows proxyList = proxy.getFlows();
		proxyDescription = proxy.getDescription();
		dstRootFolder = dir + "API" + File.separatorChar + "Proxies" + File.separatorChar + proxyName
				+ File.separatorChar + proxy.getVersion();
		createDestinationFolderStructure(dstRootFolder);
		List<Folder> files = proxyFolder.getFiles();
		for (Folder file : files)
			if (!file.isFolder())
				processProxyTemplate(dstRootFolder, file.getName());
		Folder proxyFile = proxyFolder.getFile("proxies");
		files = proxyFile.getFiles();
		for (Folder file : files)
			if (!file.isFolder())
				processProxyEndpointTemplate(cg, file.getName());
		if (!ObjectUtils.isEmpty(proxyList.getFlow()))
			processAPIFlowTemplates(proxyList, cg);
		processRouteRuleTemplate(cg.getTarget());
		if (!ObjectUtils.isEmpty(proxy.getFlows()))
			processPolicyTemplates(proxy.getFlows(), proxyFolder.getFile("policies"), cg);
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
		Template template = getTemplate(fileName);
		String dstProxiesFileName = dstProxies + File.separatorChar + proxyName + "Proxy"
				+ ProxyConfig.ENDPOINT_XML_SUFFIX;
		Map<String, Object> data = new HashMap<String, Object>();
		Map<String, Object> proxy = new HashMap<String, Object>();
		data.put("basePath", basePath);
		List<String> virtualHostList = new ArrayList<String>();
		virtualHostList.add("secure");
		virtualHostList.add("default");
		data.put("proxy", proxy);
		proxy.put("virtualHostList", virtualHostList);
		proxy.put("name", proxyName);
		proxy.put("description", proxyDescription);
		List<Object> apiDetails = new ArrayList<Object>();
		data.put("apis", apiDetails);
		Flow[] flows = cg.getProxy().getFlows().getFlow();
		for (Flow flow : flows) {
			Map<String, Object> mapApi = new HashMap<String, Object>();
			apiDetails.add(mapApi);
			mapApi.put("pathSuffix", flow.getPath());
			mapApi.put("verb", flow.getVerb().toUpperCase());
			mapApi.put("name", flow.getName());
			mapApi.put("description", flow.getDescription());
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

	private void processAPIFlowTemplates(Flows apiList, CodeGenHistory cg) throws IOException, TemplateException {
		Template reqTemplate = getTemplate("request.flowfrag.ftl");
		Template respTemplate = getTemplate("response.flowfrag.ftl");
		for (Flow flow : apiList.getFlow()) {
			String apiName = flow.getName();
			final Map<String, Object> apiDtls = new HashMap<String, Object>();
			String dstReqAPIFlow = dstApiFragflows + File.separatorChar + apiName + "_request.flowfrag";
			Map<String, Object> apiMap = new HashMap<String, Object>();
			apiDtls.put(ProxyConfig.STR_API, apiMap);
			apiMap.put("name", flow.getName());
			apiMap.put("description", flow.getDescription());
			apiMap.put("verb", flow.getVerb());
			apiMap.put("pathSuffix", flow.getPath());
			if (cg.getPolicyTemplates() != null) {
				Map commonMap = commonsGen.createMap(cg);
				apiDtls.put("policyTemplate", commonMap.get("policyTemplate"));
				apiDtls.put("policyName", commonMap.get("policyName"));
			}
			Writer reqFile = new FileWriter(dstReqAPIFlow);
			reqTemplate.process(apiDtls, reqFile);
			reqFile.flush();
			reqFile.close();
			String dstRespAPIFlow = dstApiFragflows + File.separatorChar + apiName + "_response.flowfrag";
			Writer respFile = new FileWriter(dstRespAPIFlow);
			respTemplate.process(apiDtls, respFile);
			respFile.flush();
			respFile.close();
		}
	}

	private void processPolicyTemplates(Flows apiList, Folder templates, CodeGenHistory cg)
			throws TemplateException, IOException {
		List<Folder> tmplfiles = templates.getFiles();
		for (Folder tmplFile : tmplfiles) {

			Template template = getTemplate(tmplFile.getName());
			for (Flow flow : apiList.getFlow()) {
				String apiName = flow.getName();
				String verb = flow.getVerb();
				if (verb.equalsIgnoreCase("GET")) {
					if ((tmplFile.getName().contains(ProxyConfig.STR_GET) == false)
							&& (tmplFile.getName().contains(ProxyConfig.STR_ALL) == false)) {
						continue;
					}
				} else {
					if (tmplFile.getName().contains(ProxyConfig.STR_GET)) {
						continue;
					}
				}
				String dstPoliciesFile = dstPolicies + File.separatorChar + apiName + ProxyConfig.STR_UNDERSCORE
						+ tmplFile.getName();

				dstPoliciesFile = dstPoliciesFile.replace(ProxyConfig.FTL_FILE_EXT, "").replace(ProxyConfig.STR_GET, "")
						.replace(ProxyConfig.STR_ALL, "");
				final Map<String, Object> apiDtls = new HashMap<String, Object>();
				Map<String, Object> apiMap = new HashMap<String, Object>();
				apiDtls.put(ProxyConfig.STR_API, apiMap);
				apiMap.put("name", flow.getName());
				apiMap.put("description", flow.getDescription());
				apiMap.put("verb", flow.getVerb());
				apiMap.put("pathSuffix", flow.getPath());
				apiDtls.put("targetService", "");
				apiDtls.put("targetName", flow.getTargetName() != null ? flow.getTargetName() : "");
				apiDtls.put("targetOperation", flow.getTargetOperation() != null ? flow.getTargetOperation() : "");
				apiDtls.put("resourceTargetBasePath", flow.getTargetBasepath() != null ? flow.getTargetBasepath() : "");
				if (cg.getPolicyTemplates() != null) {
					Map commonMap = commonsGen.createMap(cg);
					apiDtls.put("policyTemplate", commonMap.get("policyTemplate"));
					apiDtls.put("policyName", commonMap.get("policyName"));
				}
				Writer reqFile = new FileWriter(dstPoliciesFile);
				template.process(apiDtls, reqFile);
				reqFile.flush();
				reqFile.close();
			}
		}
	}

	private void processRouteRuleTemplate(List<Target> targets) throws IOException, TemplateException {
		Map<String, Object> proxyCfgDtls = new HashMap<String, Object>();
		Map<String, Object> proxy = new HashMap<String, Object>();
		List<String> targetEndPointList = new ArrayList<String>();
		proxyCfgDtls.put("proxy", proxy);
		proxy.put("targetEndpointList", targetEndPointList);
		if (targets != null) {
			for (Target target : targets)
				targetEndPointList.add(target.getName());
		} else {
			targetEndPointList.add("default");
		}
		Template template = getTemplate("routerules.flowfrag.ftl");
		String dstFile = dstApiFragflows + File.separatorChar + proxyName + "_routerules.flowfrag";
		Writer wFile = new FileWriter(dstFile);
		template.process(proxyCfgDtls, wFile);
		wFile.flush();
		wFile.close();
	}

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
		dstApiFragflows = proxyRootFolder + File.separatorChar + "flowfragments";
		File dir = new File(dstApiFragflows);
		dir.mkdirs();

		dstPolicies = proxyRootFolder + File.separatorChar + ProxyConfig.FLDR_POLICIES;
		dir = new File(dstPolicies);
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

	/*
	 * @Autowired private CommonsGen commonsGen;
	 * 
	 * 
	 * @Autowired private MongoConnection mongoConnection;
	 * 
	 * 
	 * 
	 * private String proxyName; private String basePath; private String
	 * proxyDescription; private List<String> targetNames= new
	 * ArrayList<String>(); private String dstApiFragflows = ""; private String
	 * dstPolicies = ""; private String dstProxies = ""; private String
	 * dstResourcesXSL = ""; private String dstResourcesJSC = ""; private String
	 * dstResourcesXsd = ""; private String dstResourcesJava = ""; private
	 * String dstRootFolder = "";
	 * 
	 * 
	 * public void generateCommonCode(Folder commonsFolder, CodeGenHistory cg ,
	 * String dir) throws IOException, TemplateException{
	 * commonsGen.generateCommonsCode(commonsFolder, cg, dir); }
	 * 
	 * public void generateProxyCode(Folder proxyFolder, CodeGenHistory cg ,
	 * String dir) throws IOException, TemplateException{ Proxy proxy =
	 * cg.getProxy(); if(cg.getTarget()!=null) { for(Target target:
	 * cg.getTarget()) targetNames.add(target.getName()); } proxyName =
	 * proxy.getName().split("_")[0]; basePath = proxy.getBasePath();
	 * 
	 * Flows proxyList = proxy.getFlows(); proxyDescription =
	 * proxy.getDescription(); dstRootFolder = dir+ "API" + File.separatorChar +
	 * "Proxies" + File.separatorChar + proxyName + File.separatorChar +
	 * proxy.getVersion(); createDestinationFolderStructure(dstRootFolder);
	 * List<Folder> files = proxyFolder.getFiles(); for(Folder file: files)
	 * if(!file.isFolder()) processProxyTemplate(dstRootFolder,file.getName());
	 * Folder proxyFile = proxyFolder.getFile("proxies"); files =
	 * proxyFile.getFiles(); for(Folder file: files) if(!file.isFolder())
	 * processProxyEndpointTemplate(cg, file.getName());
	 * processAPIFlowTemplates(proxyList , cg);
	 * processRouteRuleTemplate(cg.getTarget());
	 * processPolicyTemplates(proxy.getFlows(),
	 * proxyFolder.getFile("policies"),cg);
	 * 
	 * }
	 * 
	 * private void processProxyTemplate(String destRootFolder, String fileName)
	 * throws IOException, TemplateException {
	 * 
	 * Template template = getTemplate(fileName); String dstFileName =
	 * destRootFolder + File.separatorChar+ proxyName + "Proxy.xml"; Writer file
	 * = new FileWriter(dstFileName); Map<String, Object> data = new
	 * HashMap<String, Object>(); Map<String, Object> proxy = new
	 * HashMap<String, Object>(); data.put("proxy", proxy);
	 * proxy.put(ProxyConfig.STR_NAME, proxyName); proxy.put("description",
	 * proxyDescription); proxy.put("targetEndpointList", targetNames);
	 * data.put("xslResources", getFileList(dstResourcesXSL,
	 * ProxyConfig.XSLT_FILE_EXT, false)); data.put("policies",
	 * getFileList(dstPolicies, ProxyConfig.XML_FILE_EXT, true));
	 * data.put("jscResources", getFileList(dstResourcesJSC,
	 * ProxyConfig.JS_FILE_EXT, false)); template.process(data, file);
	 * file.flush(); file.close(); }
	 * 
	 * 
	 * private void processProxyEndpointTemplate(CodeGenHistory cg, String
	 * fileName) throws IOException,TemplateException { Template template =
	 * getTemplate(fileName); String dstProxiesFileName = dstProxies +
	 * File.separatorChar + proxyName + "Proxy" +
	 * ProxyConfig.ENDPOINT_XML_SUFFIX; Map<String, Object> data = new
	 * HashMap<String, Object>(); Map<String, Object> proxy = new
	 * HashMap<String, Object>(); data.put("basePath" , basePath); List<String>
	 * virtualHostList = new ArrayList<String>(); virtualHostList.add("secure");
	 * virtualHostList.add("default"); data.put("proxy" , proxy);
	 * proxy.put("virtualHostList" , virtualHostList); proxy.put("name" ,
	 * proxyName); proxy.put("description" , proxyDescription); List<Object>
	 * apiDetails = new ArrayList<Object>(); data.put("apis", apiDetails);
	 * Flow[] flows = cg.getProxy().getFlows().getFlow(); for(Flow flow: flows){
	 * Map<String, Object> mapApi = new HashMap<String, Object>();
	 * apiDetails.add(mapApi); mapApi.put("pathSuffix", flow.getPath());
	 * mapApi.put("verb", flow.getVerb().toUpperCase()); mapApi.put("name",
	 * flow.getName()); mapApi.put("description", flow.getDescription()); }
	 * Writer file = new FileWriter(dstProxiesFileName);
	 * if(cg.getPolicyTemplates()!=null){ Map commonMap =
	 * commonsGen.createMap(cg.getPolicyTemplates()); data.put("policyTemplate",
	 * commonMap.get("policyTemplate")); data.put("policyName",
	 * commonMap.get("policyName")); } template.process(data, file);
	 * file.flush(); file.close(); }
	 * 
	 * private void processAPIFlowTemplates(Flows apiList,CodeGenHistory cg
	 * )throws IOException, TemplateException { Template reqTemplate =
	 * getTemplate("request.flowfrag.ftl"); Template respTemplate =
	 * getTemplate("response.flowfrag.ftl"); for (Flow flow : apiList.getFlow())
	 * { String apiName = flow.getName(); final Map<String, Object> apiDtls =
	 * new HashMap<String, Object>(); String dstReqAPIFlow = dstApiFragflows +
	 * File.separatorChar + apiName + "_request.flowfrag"; Map<String, Object>
	 * apiMap = new HashMap<String, Object>(); apiDtls.put(ProxyConfig.STR_API,
	 * apiMap); apiMap.put("name", flow.getName()); apiMap.put("description",
	 * flow.getDescription()); apiMap.put("verb", flow.getVerb());
	 * apiMap.put("pathSuffix", flow.getPath());
	 * if(cg.getPolicyTemplates()!=null){ Map commonMap =
	 * commonsGen.createMap(cg.getPolicyTemplates());
	 * apiDtls.put("policyTemplate", commonMap.get("policyTemplate"));
	 * apiDtls.put("policyName", commonMap.get("policyName")); } Writer reqFile
	 * = new FileWriter(dstReqAPIFlow); reqTemplate.process(apiDtls, reqFile);
	 * reqFile.flush(); reqFile.close(); String dstRespAPIFlow = dstApiFragflows
	 * + File.separatorChar + apiName + "_response.flowfrag"; Writer respFile =
	 * new FileWriter(dstRespAPIFlow); respTemplate.process(apiDtls, respFile);
	 * respFile.flush(); respFile.close(); } }
	 * 
	 * private void processPolicyTemplates(Flows apiList, Folder templates,
	 * CodeGenHistory cg ) throws TemplateException, IOException { List<Folder>
	 * tmplfiles = templates.getFiles(); for (Folder tmplFile : tmplfiles) {
	 * 
	 * Template template = getTemplate(tmplFile.getName()); for (Flow flow :
	 * apiList.getFlow()) { String apiName = flow.getName(); String verb =
	 * flow.getVerb(); if (verb.equalsIgnoreCase("GET")) { if
	 * ((tmplFile.getName().contains(ProxyConfig.STR_GET) == false) &&
	 * (tmplFile.getName().contains(ProxyConfig.STR_ALL) == false)) { continue;
	 * } } else { if (tmplFile.getName().contains(ProxyConfig.STR_GET)) {
	 * continue; } } String dstPoliciesFile = dstPolicies + File.separatorChar +
	 * apiName + ProxyConfig.STR_UNDERSCORE + tmplFile.getName();
	 * 
	 * dstPoliciesFile = dstPoliciesFile.replace(ProxyConfig.FTL_FILE_EXT,
	 * "").replace(ProxyConfig.STR_GET, "").replace(ProxyConfig.STR_ALL, "");
	 * final Map<String, Object> apiDtls = new HashMap<String, Object>();
	 * Map<String, Object> apiMap = new HashMap<String, Object>();
	 * apiDtls.put(ProxyConfig.STR_API, apiMap); apiMap.put("name",
	 * flow.getName()); apiMap.put("description", flow.getDescription());
	 * apiMap.put("verb", flow.getVerb()); apiMap.put("pathSuffix",
	 * flow.getPath()); apiDtls.put("targetService","");
	 * apiDtls.put("targetName",flow.getTargetName()!=
	 * null?flow.getTargetName():"");
	 * apiDtls.put("targetOperation",flow.getTargetOperation()!=
	 * null?flow.getTargetOperation():""); if(cg.getPolicyTemplates()!=null){
	 * Map commonMap = commonsGen.createMap(cg.getPolicyTemplates());
	 * apiDtls.put("policyTemplate", commonMap.get("policyTemplate"));
	 * apiDtls.put("policyName", commonMap.get("policyName")); } Writer reqFile
	 * = new FileWriter(dstPoliciesFile); template.process(apiDtls, reqFile);
	 * reqFile.flush(); reqFile.close(); }
	 * 
	 * } }
	 * 
	 * 
	 * private void processRouteRuleTemplate(List<Target> targets) throws
	 * IOException, TemplateException { Map<String,Object> proxyCfgDtls = new
	 * HashMap<String,Object>(); Map<String,Object> proxy = new
	 * HashMap<String,Object>(); List<String> targetEndPointList = new
	 * ArrayList<String>(); proxyCfgDtls.put("proxy", proxy);
	 * proxy.put("targetEndpointList", targetEndPointList); if(targets!=null){
	 * for(Target target: targets) targetEndPointList.add(target.getName()); }
	 * else{ targetEndPointList.add("default"); } Template template =
	 * getTemplate("routerules.flowfrag.ftl"); String dstFile = dstApiFragflows
	 * + File.separatorChar + proxyName +"_routerules.flowfrag"; Writer wFile =
	 * new FileWriter(dstFile); template.process(proxyCfgDtls, wFile);
	 * wFile.flush(); wFile.close(); }
	 * 
	 * @SuppressWarnings("deprecation") public Template getTemplate(String file)
	 * throws IOException{ String reader = mongoConnection.getFile(file);
	 * Configuration conf= new Configuration(); StringTemplateLoader tloader =
	 * new StringTemplateLoader(); conf.setTemplateLoader(tloader);
	 * tloader.putTemplate(file, reader); conf.setObjectWrapper(new
	 * DefaultObjectWrapper()); Template template = conf.getTemplate(file);
	 * return template; }
	 * 
	 * private List<String> getFileList(String rootFolder, String ext, boolean
	 * removeFileExt) { List<String> fileList = new ArrayList<String>(); File
	 * file = new File(rootFolder); String fileNames[] = file.list(); for
	 * (String fileName : fileNames) { if (fileName.endsWith(ext)) { if
	 * (removeFileExt) { fileName = fileName.replace(ext, ""); }
	 * fileList.add(fileName); } } return fileList; }
	 * 
	 * private void createDestinationFolderStructure(String proxyRootFolder) {
	 * dstApiFragflows = proxyRootFolder + File.separatorChar + "flowfragments";
	 * File dir = new File(dstApiFragflows); dir.mkdirs();
	 * 
	 * dstPolicies = proxyRootFolder + File.separatorChar +
	 * ProxyConfig.FLDR_POLICIES; dir = new File(dstPolicies); dir.mkdirs();
	 * 
	 * dstProxies = proxyRootFolder + File.separatorChar + "proxies"; dir = new
	 * File(dstProxies); dir.mkdirs();
	 * 
	 * dstResourcesXSL = proxyRootFolder + File.separatorChar +
	 * ProxyConfig.FLDR_RESOURCES + File.separatorChar + ProxyConfig.FLDR_XSL;
	 * dir = new File(dstResourcesXSL); dir.mkdirs();
	 * 
	 * dstResourcesJSC = proxyRootFolder + File.separatorChar +
	 * ProxyConfig.FLDR_RESOURCES + File.separatorChar + ProxyConfig.FLDR_JSC;
	 * dir = new File(dstResourcesJSC); dir.mkdirs();
	 * 
	 * dstResourcesJava = proxyRootFolder + File.separatorChar +
	 * ProxyConfig.FLDR_RESOURCES + File.separatorChar + ProxyConfig.FLDR_JAVA;
	 * dir = new File(dstResourcesJava); dir.mkdirs();
	 * 
	 * dstResourcesXsd = proxyRootFolder + File.separatorChar +
	 * ProxyConfig.FLDR_RESOURCES + File.separatorChar + ProxyConfig.FLDR_XSD;
	 * dir = new File(dstResourcesXsd); dir.mkdirs(); }
	 */
}
