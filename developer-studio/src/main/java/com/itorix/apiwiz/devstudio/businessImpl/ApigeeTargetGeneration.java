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

import com.itorix.apiwiz.common.model.proxystudio.CodeGenHistory;
import com.itorix.apiwiz.common.model.proxystudio.Flow;
import com.itorix.apiwiz.common.model.proxystudio.Flows;
import com.itorix.apiwiz.common.model.proxystudio.Folder;
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
public class ApigeeTargetGeneration {
	@Autowired
	ApplicationProperties applicationProperties;

	@Autowired
	MongoConnection mongoConnection;
	
	@Autowired
	private CommonsGen commonsGen;

	private String basePath;
	private String targetDescription;
	private String targetName;
	private String dstPolicies = "";
	private String dstResourcesXsl = "";
	private String dstResourcesJsc = "";
	private String dstResourcesJava = "";
	private String dstResourcesXsd = "";
	private String dstTarget = "";
	private String dstRootFolder = "";

	public void generateTargetCode(Folder targetFolder, CodeGenHistory cg, String dir)
			throws IOException, TemplateException {
		List<Target> targets = cg.getTarget();
		dstRootFolder = dir;
		createDestinationFolderStructure(dstRootFolder);
		for (Target target : targets) {
			targetName = target.getName();
			basePath = target.getBasePath();
			targetDescription = target.getDescription();
			// dstRootFolder = dir ;//+ "API" + File.separatorChar + "Targets" +
			// File.separatorChar +
			// targetName + File.separatorChar + cg.getProxy().getVersion();
			processTargetEndpointTemplate(target.getFlows(), targetFolder.getFile("targets"), cg);
			processPolicyTemplates(target.getFlows(), targetFolder.getFile("policies"));
		}
	}

	private void processTargetEndpointTemplate(Flows flows, Folder target, CodeGenHistory cg)
			throws IOException, TemplateException {
		List<Folder> files = target.getFiles();
		for (Folder tmplFile : files) {
			if (!tmplFile.isFolder()) {
				// Template template =
				// getTemplateFromFile("/opt/itorix/temp/ProxyGen/targets/TargetEndpoint.xml.ftl");
				Template template = getTemplate(cg.getConnectorId()+"-"+"TargetEndpoint.xml.ftl");
				String tgtfilePrefix = targetName;
				String dstFileName = dstRootFolder + File.separatorChar + "targets" + File.separatorChar + tgtfilePrefix
						+ ProxyConfig.XML_FILE_EXT;
				Writer file = new FileWriter(dstFileName);
				Map<String, Object> data = new HashMap<String, Object>();

				List<Object> operations = new ArrayList<>();
				if (flows != null && flows.getFlow() != null) {
					for (Flow flow : flows.getFlow()) {
						Map<String, Object> mapApi = new HashMap<String, Object>();
						operations.add(mapApi);
						mapApi.put("name", flow.getName());
						mapApi.put("disablePathSuffix", flow.getDisablePathSuffix());
						if (flow.getPolicyTemplates() != null) {
							Map flowMap = commonsGen.createMap(flow.getPolicyTemplates());
							mapApi.put("flowPolicyTemplate", flowMap.get("policyTemplate"));
							mapApi.put("flowPolicyName", flowMap.get("policyName"));
						}
						//operations.add(flow.getName());
					}
				}
				data.put("targetOperations", operations);
				data.put("targetName", targetName);
				data.put("targetServiceName", "");
				data.put("targetPath", basePath);
				data.put("targetDescription", targetDescription);
				if (cg.getPolicyTemplates() != null) {
					CommonsGen commons = new CommonsGen();
					Map commonMap = commons.createMap(cg);
					data.put("policyTemplate", commonMap.get("policyTemplate"));
					data.put("policyName", commonMap.get("policyName"));
				}
				template.process(data, file);
				file.flush();
				file.close();
			}
		}
	}

	private void processPolicyTemplates(Flows flows, Folder policies) throws IOException, TemplateException {
		String STR_TARGET = "TARGET_";
		if (policies != null) {
			List<Folder> fileList = policies.getFiles();
			if (fileList != null) {
				for (Folder fragFile : fileList) {
					Template template = getTemplate(fragFile.getName());
					String dstFile;
					if (fragFile.getName().contains("ftl"))
						if (fragFile.getName().contains(STR_TARGET)) {
							final Map<String, Object> targetDetails = new HashMap<String, Object>();
							dstFile = dstPolicies + File.separatorChar + targetName + "_" + fragFile.getName();
							dstFile = dstFile.replace(STR_TARGET, ProxyConfig.EMPTY_STRING)
									.replace(ProxyConfig.FTL_FILE_EXT, ProxyConfig.EMPTY_STRING);
							if (((new File(dstFile)).exists() == false)) {
								targetDetails.put("targetName", targetName);
								targetDetails.put("targetServiceName", "");
								Writer file = new FileWriter(dstFile);
								template.process(targetDetails, file);
								file.flush();
								file.close();
							}
						} else {
							for (int i = 0; i < flows.getFlow().length; i++) {
								final Map<String, Object> operationDetails = new HashMap<String, Object>();
								dstFile = dstPolicies + File.separatorChar + targetName + "_"
										+ flows.getFlow()[i].getName() + "_" + fragFile.getName();
								dstFile = dstFile.replace(STR_TARGET, ProxyConfig.EMPTY_STRING)
										.replace(ProxyConfig.FTL_FILE_EXT, ProxyConfig.EMPTY_STRING);
								operationDetails.put("SOAPAction", flows.getFlow()[i].getName());
								operationDetails.put("targetName", targetName);
								operationDetails.put("targetServiceName", targetName);
								Writer file = new FileWriter(dstFile);
								template.process(operationDetails, file);
								file.flush();
								file.close();
							}
						}
					else {
						String content = mongoConnection.getFile(fragFile.getName());
						writeFile(content, dstPolicies + File.separatorChar + fragFile.getName());
					}
				}
			}
		}
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

	public Template getTemplate(String file) throws IOException {
		System.out.println(file);
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
	private Template getTemplateFromFile(String file) throws IOException {
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

	private void createDestinationFolderStructure(String apiProxyRootFolder) {
		dstPolicies = apiProxyRootFolder + File.separatorChar + ProxyConfig.FLDR_POLICIES;
		// File dir = new File(dstPolicies);
		// dir.mkdirs();
		dstResourcesXsl = apiProxyRootFolder + File.separatorChar + ProxyConfig.FLDR_RESOURCES + File.separatorChar
				+ ProxyConfig.FLDR_XSL;
		// dir = new File(dstResourcesXsl);
		// dir.mkdirs();
		dstResourcesJsc = apiProxyRootFolder + File.separatorChar + ProxyConfig.FLDR_RESOURCES + File.separatorChar
				+ ProxyConfig.FLDR_JSC;
		// dir = new File(dstResourcesJsc);
		// dir.mkdirs();
		dstResourcesJava = apiProxyRootFolder + File.separatorChar + ProxyConfig.FLDR_RESOURCES + File.separatorChar
				+ ProxyConfig.FLDR_JAVA;
		// dir = new File(dstResourcesJava);
		// dir.mkdirs();
		dstResourcesXsd = apiProxyRootFolder + File.separatorChar + ProxyConfig.FLDR_RESOURCES + File.separatorChar
				+ ProxyConfig.FLDR_WSDL;
		File dir = new File(dstResourcesXsd);
		dir.mkdirs();
		dstTarget = apiProxyRootFolder + File.separatorChar + "targets";
		dir = new File(dstTarget);
		dir.mkdirs();
	}
}
