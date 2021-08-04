package com.itorix.apiwiz.devstudio.businessImpl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.itorix.apiwiz.common.model.proxystudio.Category;
import com.itorix.apiwiz.common.model.proxystudio.CodeGenHistory;
import com.itorix.apiwiz.common.model.proxystudio.Folder;
import com.itorix.apiwiz.common.model.proxystudio.Policy;
import com.itorix.apiwiz.common.model.proxystudio.ProxyConfig;
import com.itorix.apiwiz.common.properties.ApplicationProperties;
import com.itorix.apiwiz.devstudio.dao.MongoConnection;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

@Component
public class CommonsGen {
	@Autowired
	ApplicationProperties applicationProperties;

	@Autowired
	private MongoConnection mongoConnection;

	private String dstApiFragflows = "";
	private String dstPolicies = "";
	private String dstResourcesXSL = "";
	private String dstResourcesJSC = "";
	private String dstResourcesXsd = "";
	private String dstResourcesJava = "";
	private String dstRootFolder = "";

	public void generateCommonsCode(Folder commonsFolder, CodeGenHistory cg, String dir)
			throws IOException, TemplateException {
		dstRootFolder = dir + "API" + File.separatorChar + "Common" + File.separatorChar + cg.getProxy().getVersion();
		createDestinationFolderStructure(dstRootFolder);
		processPolicyTemplates(createMap(cg), commonsFolder.getFile("flowfragments"));
		processPolicies(commonsFolder.getFile("policies"));
		processResources(commonsFolder.getFile("resources"));
	}

	private void processPolicyTemplates(Map apiMap, Folder templates) throws IOException, TemplateException {
		for (Folder tmplFile : templates.getFiles()) {
			if (tmplFile.getName().endsWith("ftl")) {
				Template template = getTemplate(tmplFile.getName());
				Writer out = new StringWriter();
				template.process(apiMap, out);
				String filePath = dstApiFragflows + File.separatorChar + tmplFile.getName().replaceAll(".ftl", "");
				writeFile(out.toString(), filePath);
			} else {
				String content = mongoConnection.getFile(tmplFile.getName());
				writeFile(content, dstApiFragflows + File.separatorChar + tmplFile.getName());
			}
		}
	}

	private void processPolicies(Folder templates) throws IOException, TemplateException {
		for (Folder tmplFile : templates.getFiles()) {
			String content = mongoConnection.getFile(tmplFile.getName());
			writeFile(content, dstPolicies + File.separatorChar + tmplFile.getName());
		}
	}

	private void processResources(Folder templates) throws IOException, TemplateException {
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
						String content = mongoConnection.getFile(resourceFile.getName());
						writeFile(content, filePath + File.separatorChar + resourceFile.getName());
					}
			}
		}
	}

	protected Map<String, Object> createMap(CodeGenHistory cg) {
		List<Category> policyTemplates = cg.getPolicyTemplates();
		Map<String, Object> apiDtls = null;
		if (policyTemplates != null) {
			apiDtls = new HashMap<String, Object>();
			Map<String, Object> templateMap = new HashMap<String, Object>();
			Map<String, Object> policyMap = new HashMap<String, Object>();
			apiDtls.put("policyTemplate", templateMap);
			apiDtls.put("policyName", policyMap);
			for (Category category : policyTemplates) {
				String categoryEnabled = "false";
				String categoryType = category.getType();
				for (Policy policy : category.getPolicies()) {
					if (policy.isEnabled()) {
						policyMap.put(policy.getName(), "true");
						categoryEnabled = "true";
					} else
						policyMap.put(policy.getName(), "false");
				}
				templateMap.put(categoryType, categoryEnabled);
			}
		}
		return apiDtls;
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

	private Template getTemplate(String file) throws IOException {
		System.out.println("fileName : " + file);
		String reader = mongoConnection.getFile(file);
		System.out.println(reader);
		Configuration conf = new Configuration();
		StringTemplateLoader tloader = new StringTemplateLoader();
		conf.setTemplateLoader(tloader);
		tloader.putTemplate(file, reader);
		conf.setObjectWrapper(new DefaultObjectWrapper());
		Template template = conf.getTemplate(file);
		return template;
	}

	private void createDestinationFolderStructure(String proxyRootFolder) {
		dstApiFragflows = proxyRootFolder + File.separatorChar + "flowfragments";
		File dir = new File(dstApiFragflows);
		dir.mkdirs();

		dstPolicies = proxyRootFolder + File.separatorChar + ProxyConfig.FLDR_POLICIES;
		dir = new File(dstPolicies);
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
	 * 
	 * @Autowired private MongoConnection mongoConnection;
	 * 
	 * private String dstApiFragflows = ""; private String dstPolicies = "";
	 * private String dstResourcesXSL = ""; private String dstResourcesJSC = "";
	 * private String dstResourcesXsd = ""; private String dstResourcesJava =
	 * ""; private String dstRootFolder = "";
	 * 
	 * public void generateCommonsCode(Folder commonsFolder, CodeGenHistory cg ,
	 * String dir) throws IOException, TemplateException{ dstRootFolder = dir+
	 * "API" + File.separatorChar + "Common" + File.separatorChar +
	 * cg.getProxy().getVersion();
	 * createDestinationFolderStructure(dstRootFolder);
	 * processPolicyTemplates(createMap(cg.getPolicyTemplates()),commonsFolder.
	 * getFile("flowfragments"));
	 * processPolicies(commonsFolder.getFile("policies"));
	 * processResources(commonsFolder.getFile("resources")); }
	 * 
	 * @SuppressWarnings("rawtypes") private void processPolicyTemplates(Map
	 * apiMap, Folder templates) throws IOException, TemplateException{ for
	 * (Folder tmplFile : templates.getFiles()) {
	 * if(tmplFile.getName().endsWith("ftl")){ Template template =
	 * getTemplate(tmplFile.getName()); Writer out = new StringWriter();
	 * template.process(apiMap, out); String filePath = dstApiFragflows +
	 * File.separatorChar + tmplFile.getName().replaceAll(".ftl", "");
	 * writeFile(out.toString(),filePath); } else{ String content =
	 * mongoConnection.getFile(tmplFile.getName()); writeFile(content,
	 * dstApiFragflows+ File.separatorChar +tmplFile.getName()); } } }
	 * 
	 * private void processPolicies(Folder templates) throws IOException,
	 * TemplateException{ for (Folder tmplFile : templates.getFiles()) { String
	 * content = mongoConnection.getFile(tmplFile.getName()); writeFile(content,
	 * dstPolicies+ File.separatorChar +tmplFile.getName()); } }
	 * 
	 * private void processResources(Folder templates) throws IOException,
	 * TemplateException{ for (Folder tmplFile : templates.getFiles()) {
	 * if(tmplFile.isFolder()){ String filePath = "";
	 * if(tmplFile.getName().equals(ProxyConfig.FLDR_XSD)) filePath =
	 * dstResourcesXsd; else
	 * if(tmplFile.getName().equals(ProxyConfig.FLDR_JAVA)) filePath =
	 * dstResourcesJava; else
	 * if(tmplFile.getName().equals(ProxyConfig.FLDR_JSC)) filePath =
	 * dstResourcesJSC; else if(tmplFile.getName().equals(ProxyConfig.FLDR_XSL))
	 * filePath = dstResourcesXSL; if(!filePath.equals("")) for (Folder
	 * resourceFile : tmplFile.getFiles()) { String content =
	 * mongoConnection.getFile(resourceFile.getName()); writeFile(content,
	 * filePath + File.separatorChar +resourceFile.getName()); } } } }
	 * 
	 * protected Map<String, Object> createMap(List<Category> policyTemplates){
	 * Map<String, Object> apiDtls = null; if(policyTemplates!=null){ apiDtls =
	 * new HashMap<String, Object>(); Map<String, Object> templateMap = new
	 * HashMap<String, Object>(); Map<String, Object> policyMap = new
	 * HashMap<String, Object>(); apiDtls.put("policyTemplate", templateMap);
	 * apiDtls.put("policyName", policyMap); for(Category category:
	 * policyTemplates){ String categoryEnabled = "false"; String categoryType =
	 * category.getType(); for(Policy policy: category.getPolicies()){
	 * if(policy.isEnabled()){ policyMap.put(policy.getName(), "true");
	 * categoryEnabled = "true"; } else policyMap.put(policy.getName(),
	 * "false"); } templateMap.put(categoryType, categoryEnabled); } } return
	 * apiDtls; }
	 * 
	 * private boolean writeFile(String content , String name) throws
	 * IOException{ File file = new File(name); FileOutputStream fop = new
	 * FileOutputStream(file); if (!file.exists()) { file.createNewFile(); }
	 * byte[] contentInBytes = content.getBytes(); fop.write(contentInBytes);
	 * fop.flush(); fop.close(); return true; }
	 * 
	 * @SuppressWarnings("deprecation") private Template getTemplate(String
	 * file) throws IOException{ // System.out.println("fileName : "+ file);
	 * String reader = mongoConnection.getFile(file); //
	 * System.out.println(reader); Configuration conf= new Configuration();
	 * StringTemplateLoader tloader = new StringTemplateLoader();
	 * conf.setTemplateLoader(tloader); tloader.putTemplate(file, reader);
	 * conf.setObjectWrapper(new DefaultObjectWrapper()); Template template =
	 * conf.getTemplate(file); return template; }
	 * 
	 * private void createDestinationFolderStructure(String proxyRootFolder) {
	 * dstApiFragflows = proxyRootFolder + File.separatorChar + "flowfragments";
	 * File dir = new File(dstApiFragflows); dir.mkdirs();
	 * 
	 * dstPolicies = proxyRootFolder + File.separatorChar +
	 * ProxyConfig.FLDR_POLICIES; dir = new File(dstPolicies); dir.mkdirs();
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
