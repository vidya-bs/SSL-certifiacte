package com.itorix.apiwiz.devstudio.businessImpl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.itorix.apiwiz.common.model.projectmanagement.Project;
import com.itorix.apiwiz.common.model.projectmanagement.ProjectFile;
import com.itorix.apiwiz.common.model.projectmanagement.ProjectMetaData;
import com.itorix.apiwiz.common.model.projectmanagement.Proxies;
import com.itorix.apiwiz.common.model.proxystudio.Category;
import com.itorix.apiwiz.common.model.proxystudio.CodeGenHistory;
import com.itorix.apiwiz.common.model.proxystudio.Flow;
import com.itorix.apiwiz.common.model.proxystudio.Folder;
import com.itorix.apiwiz.common.model.proxystudio.Policy;
import com.itorix.apiwiz.common.model.proxystudio.ProxyConfig;
import com.itorix.apiwiz.devstudio.dao.MongoConnection;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

@Component
public class ProxyGenerator {

	@Autowired
	MongoConnection mongoConnection;

	private String proxyName;
	private String dstPolicies = "";
	private String dstProxies = "";
	private String dstResourcesXSL = "";
	private String dstResourcesJSC = "";
	private String dstResourcesXsd = "";
	private String dstResourcesJava = "";
	private String dstResourcesWsdl = "";
	private String dstRootFolder = "";
	private String dstTargets = "";
	private List<String> proxyNames = new ArrayList<String>();

	// private final List<String> allowedTemplateExt =
	// Arrays.asList("ba","oa","2w");

	public void generateProxyCode(Folder proxyFolders, CodeGenHistory cg, String dir, Project project)
			throws IOException, TemplateException {
		proxyName = cg.getProxy().getName();
		dstRootFolder = dir;
		String templateDir = "/opt/itorix/templates/API";
		List<String> basPath = Arrays.asList(cg.getProxy().getBasePath().split(","));
		createDestinationFolderStructure(dstRootFolder);
		Map<String, Object> data = populateProxyData(cg, project.getProxyByName(proxyName));
		processPolicies(templateDir + "/Common/policies", data);
		processProxyEndpoint(templateDir + "/Proxy/proxies", data, basPath);
		processProxy(templateDir + "/Proxy", data);

		// if(project.getProxyByName(proxyName).getWsdlFiles()!=null)
		// copyWSDLFiles(project.getName(), project.getProxyByName(proxyName));
		// if(project.getProxyByName(proxyName).getXsdFiles()!=null)
		// copyXSDFiles(project.getName(), project.getProxyByName(proxyName));

		processTargetEndpoint(templateDir + "/Target/targets", data);
	}

	private void processPolicies(String policiesDir, Map<String, Object> data) {
		List<File> files = (List<File>) FileUtils.listFiles(new File(policiesDir), TrueFileFilter.INSTANCE,
				TrueFileFilter.INSTANCE);
		for (File file : files) {
			String extn = FilenameUtils.getExtension(file.getName());
			String dstPolicyFileName = dstPolicies + File.separatorChar
					+ file.getName().replace(ProxyConfig.FTL_FILE_EXT, "");
			if (extn.equalsIgnoreCase("ftl")) {
				try {
					Template template = getTemplateFromFile(file.getAbsolutePath());
					Writer dstFile = new FileWriter(dstPolicyFileName);
					template.process(data, dstFile);
					dstFile.flush();
					dstFile.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (TemplateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				try {
					FileUtils.copyFile(file, new File(dstPolicyFileName));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void processProxyEndpoint(String proxyEndPntDir, Map<String, Object> data, List<String> basePath) {
		int emptyCounter = 0;
		List<File> files = (List<File>) FileUtils.listFiles(new File(proxyEndPntDir), TrueFileFilter.INSTANCE,
				TrueFileFilter.INSTANCE);
		for (File file : files) {
			String extn = FilenameUtils.getExtension(file.getName());

			if (extn.equalsIgnoreCase("ftl")) {
				for (String path : basePath) {
					String ext = "_default";
					if (emptyCounter > 0)
						ext = "_default" + Integer.toString(emptyCounter);
					if (!ext.equals("") || emptyCounter == 0) {
						Template template;
						try {
							data.put("basePath", path);
							Map<String, Object> proxyMap = (Map<String, Object>) data.get("proxy");
							proxyMap.put("name", proxyName + ext);
							data.put("proxy", proxyMap);
							template = getTemplateFromFile(file.getAbsolutePath());
							String endPointFileName = proxyName + (ext.equals("") ? ext : "" + ext) + "Proxy"
									+ ProxyConfig.ENDPOINT_XML_SUFFIX;
							proxyNames.add(FilenameUtils.getBaseName(endPointFileName));
							String dstFileName = dstProxies + File.separatorChar + endPointFileName;
							Writer dstFile = new FileWriter(dstFileName);
							template.process(data, dstFile);
							dstFile.flush();
							dstFile.close();
							emptyCounter = ext.equals("") || ext.contains("default") ? emptyCounter + 1 : emptyCounter;
						} catch (IOException e) {
							e.printStackTrace();
						} catch (TemplateException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}

	private void processProxy(String proxyDir, Map<String, Object> data) {
		data.put("proxyList", proxyNames);

		Map<String, Object> proxyMap = (Map<String, Object>) data.get("proxy");
		proxyMap.put("name", proxyName);
		data.put("proxy", proxyMap);

		File file = new File(proxyDir + File.separatorChar + "Proxy.xml.ftl");
		String extn = FilenameUtils.getExtension(file.getName());
		if (extn.equalsIgnoreCase("ftl")) {
			Template template;
			try {
				template = getTemplateFromFile(file.getAbsolutePath());
				String dstFileName = dstRootFolder + File.separatorChar + proxyName + "Proxy.xml";
				Writer dstFile = new FileWriter(dstFileName);
				template.process(data, dstFile);
				dstFile.flush();
				dstFile.close();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (TemplateException e) {
				e.printStackTrace();
			}
		}
	}

	private void processTargetEndpoint(String targetEndPntDir, Map<String, Object> data) {
		List<File> files = (List<File>) FileUtils.listFiles(new File(targetEndPntDir), TrueFileFilter.INSTANCE,
				TrueFileFilter.INSTANCE);
		for (File file : files) {
			String extn = FilenameUtils.getExtension(file.getName());
			if (extn.equalsIgnoreCase("ftl")) {
				Template template;
				try {
					template = getTemplateFromFile(file.getAbsolutePath());
					String dstFileName = dstTargets + File.separatorChar + "default" + "Target"
							+ ProxyConfig.ENDPOINT_XML_SUFFIX;
					Writer dstFile = new FileWriter(dstFileName);
					template.process(data, dstFile);
					dstFile.flush();
					dstFile.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (TemplateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	private void copyWSDLFiles(String projectName, Proxies proxies) {
		for (ProjectFile file : proxies.getWsdlFiles()) {
			mongoConnection.getResourceFile(projectName, proxyName, "WSDL", file.getFileName(),
					dstResourcesWsdl + File.separatorChar);
		}
	}

	private void copyXSDFiles(String projectName, Proxies proxies) {
		for (ProjectFile file : proxies.getXsdFiles()) {
			mongoConnection.getResourceFile(projectName, proxyName, "XSD", file.getFileName(),
					dstResourcesXsd + File.separatorChar);
		}
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private Map<String, Object> populateProxyData(CodeGenHistory cg, Proxies proxies) {
		Map<String, Object> data = new HashMap<String, Object>();
		Map<String, Object> proxy = new HashMap<String, Object>();
		Map<String, Object> target = new HashMap<String, Object>();
		data.put("basePath", cg.getProxy().getBasePath());
		List<String> virtualHostList = new ArrayList<String>();
		if (proxies.getApigeeVirtualHosts() != null)
			for (String vHost : proxies.getApigeeVirtualHosts())
				virtualHostList.add(vHost);
		data.put("proxy", proxy);
		proxy.put("virtualHostList", virtualHostList);
		proxy.put("name", proxyName);
		proxy.put("description", cg.getProxy().getDescription());

		data.put("target", target);
		target.put("name", "default");
		target.put("description", "default target endpoint");

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
			}
		}
		if (cg.getPolicyTemplates() != null) {
			Map commonMap = createMap(cg.getPolicyTemplates());
			data.put("policyTemplate", commonMap.get("policyTemplate"));
			data.put("policyName", commonMap.get("policyName"));
		}
		List<Object> projectMetadataList = new ArrayList<Object>();
		data.put("projectMetadataList", projectMetadataList);
		if (proxies.getProjectMetaData() != null) {
			for (ProjectMetaData metadata : proxies.getProjectMetaData()) {
				Map<String, Object> projectMetadata = new HashMap<String, Object>();
				projectMetadataList.add(projectMetadata);
				projectMetadata.put("name", metadata.getName());
				projectMetadata.put("value", metadata.getValue() != null ? metadata.getValue() : " ");
			}
		}
		if (proxies.getWsdlFiles() != null)
			data.put("wsdlList", getFileList(proxies.getWsdlFiles()));
		if (proxies.getXsdFiles() != null)
			data.put("xsdList", getFileList(proxies.getXsdFiles()));
		return data;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private List<String> getFileList(List<ProjectFile> files) {
		List<String> fileList = new ArrayList();
		for (ProjectFile file : files) {
			fileList.add(file.getFileName());
		}
		return fileList;
	}

	private List<Object> createMetadataMap(List<ProjectMetaData> list) {
		List<Object> projectMetadataList = new ArrayList<Object>();
		for (ProjectMetaData metadata : list) {
			Map<String, Object> projectMetadata = new HashMap<String, Object>();
			projectMetadataList.add(projectMetadata);
			projectMetadata.put("name", metadata.getName());
			projectMetadata.put("value", metadata.getValue());
		}
		return projectMetadataList;
	}

	private Map<String, Object> createMap(List<Category> policyTemplates) {
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

	private void createDestinationFolderStructure(String proxyRootFolder) {

		dstPolicies = proxyRootFolder + File.separatorChar + ProxyConfig.FLDR_POLICIES;
		File dir = new File(dstPolicies);
		dir.mkdirs();

		dstProxies = proxyRootFolder + File.separatorChar + "proxies";
		dir = new File(dstProxies);
		dir.mkdirs();

		dstTargets = proxyRootFolder + File.separatorChar + "targets";
		dir = new File(dstTargets);
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

		dstResourcesWsdl = proxyRootFolder + File.separatorChar + ProxyConfig.FLDR_RESOURCES + File.separatorChar
				+ ProxyConfig.FLDR_WSDL;
		dir = new File(dstResourcesWsdl);
		dir.mkdirs();
	}
}
