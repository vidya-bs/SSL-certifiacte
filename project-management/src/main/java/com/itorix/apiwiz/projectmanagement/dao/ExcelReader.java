package com.itorix.apiwiz.projectmanagement.dao;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.model.projectmanagement.Category;
import com.itorix.apiwiz.common.model.projectmanagement.Policy;
import com.itorix.apiwiz.common.model.projectmanagement.Project;
import com.itorix.apiwiz.common.model.projectmanagement.ProjectFile;
import com.itorix.apiwiz.common.model.projectmanagement.ProjectMetaData;
import com.itorix.apiwiz.common.model.projectmanagement.Proxies;
import com.itorix.apiwiz.common.model.projectmanagement.ServiceRegistry;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import net.sf.json.JSONArray;

public class ExcelReader {

	public List<Map<String, String>> readExcelData(String fileName) throws IOException, InvalidFormatException {
		Workbook workbook = WorkbookFactory.create(new File(fileName));
		Sheet sheet = workbook.getSheetAt(1);
		DataFormatter dataFormatter = new DataFormatter();
		List<Map<String, String>> dataElements = new ArrayList<>();
		for (Row row : sheet)
			if (row.getRowNum() > 0) {
				Map<String, String> data = new HashMap<>();
				for (Cell cell : row) {
					try {
						int colIndex = cell.getColumnIndex();
						String key = workbook.getSheetAt(1).getRow(0).getCell(colIndex).getStringCellValue();
						key = key.replaceAll("\\n", "").replaceAll("\\s+", "_");
						String cellValue = dataFormatter.formatCellValue(cell);
						String validKey = getValidKey(key);
						if (validKey != null) {
							data.put(validKey, cellValue);
						}
					} catch (Exception e) {

					}
				}

				dataElements.add(data);
			}
		return dataElements;
	}

	private String getValidKey(String key) {
		key = Character.getNumericValue(key.charAt(key.length() - 1)) == -1 ? key.substring(0, key.length() - 1) : key;
		final List<String> validKeys = new ArrayList<String>();
		validKeys.add("Service_Name");
		validKeys.add("Active");
		validKeys.add("DP_URI");
		validKeys.add("Apigee_VirtualHosts");
		validKeys.add("Security_Auth-N");
		validKeys.add("Security_Auth-Z");
		validKeys.add("req_transformation");
		validKeys.add("Threat_Flowname");
		validKeys.add("Routing");
		validKeys.add("Schema_Validation_Request");
		validKeys.add("Schema_Validation_Response");
		validKeys.add("Schema_Validation_Error");
		validKeys.add("Schema_Validation_MTOM");
		validKeys.add("gal_original_destination");
		validKeys.add("log_everthing");
		validKeys.add("log_custom_fields_request_name");
		validKeys.add("log_custom_fields_request_value");
		validKeys.add("log_custom_fields_response_name");
		validKeys.add("log_custom_fields_response_value");
		validKeys.add("Routing_xpath_region");
		validKeys.add("Routing_xpath_mrn");
		validKeys.add("Routing_xpath_mrntype");
		validKeys.add("Routing_constants_region");
		validKeys.add("Routing_constants_envlbl");
		validKeys.add("Routing_constants_urn");
		validKeys.add("Routing_xpath_encounterID");
		validKeys.add("Routing_xpath_encounterIDType");
		validKeys.add("Interface_Type");
		validKeys.add("req_transformation");
		validKeys.add("Threat_Flowname");
		validKeys.add("Routing_Flowname");
		validKeys.add("Logging_Flowname");
		validKeys.add("log_everything");
		for (String textKey : validKeys)
			if (textKey.contains(key))
				return textKey;
		return null;
	}

	public Project populateProject(Map<String, String> data) throws ItorixException {
		Project project;
		try {
			project = new Project();
			project.setName(data.get("Service_Name"));
			project.setStatus(data.get("Active").equalsIgnoreCase("yes") ? "Active" : "inActive");
			project.setInProd(data.get("Active").equalsIgnoreCase("yes") ? "true" : "false");
			project.setProxies(populateProxy(data));

		} catch (Exception e) {
			project = null;
		}
		return project;
	}

	private List<Proxies> populateProxy(Map<String, String> data) throws ItorixException {
		List<Proxies> proxies = new ArrayList<Proxies>();
		Proxies proxy = new Proxies();
		proxy.setName(data.get("Service_Name").toLowerCase().replaceAll(" ", "_"));
		proxy.setExternal(false);
		if (data.get("DP_URI") != null) {
			List<String> paths = new ArrayList<String>(Arrays.asList(data.get("DP_URI").split("\n")));
			proxy.setBasePath(paths);
		}
		if (data.get("Apigee_VirtualHosts") != null) {
			Set<String> apigeeVirtualHosts = new HashSet<String>(
					Arrays.asList(data.get("Apigee_VirtualHosts").split(";")));
			proxy.setApigeeVirtualHosts(apigeeVirtualHosts);
		}
		proxy.setProjectMetaData(populateMetadata(data));
		proxies.add(proxy);
		return proxies;
	}

	private List<ProjectMetaData> populateMetadata(Map<String, String> data) {
		List<ProjectMetaData> projectMetaData = new ArrayList<ProjectMetaData>();
		if (data.containsKey("gal_original_destination")) {
			ProjectMetaData gal_original_destination = new ProjectMetaData();
			gal_original_destination.setName("kp.metadata.gal_original_destination");
			gal_original_destination.setValue(data.get("gal_original_destination"));
			projectMetaData.add(gal_original_destination);
		}
		if (data.containsKey("log_everything")) {
			ProjectMetaData log_everything = new ProjectMetaData();
			log_everything.setName("kp.metadata.log_everything");
			log_everything.setValue(data.get("log_everything"));
			projectMetaData.add(log_everything);
		}
		if (data.containsKey("log_custom_fields_request_name") && data.containsKey("log_custom_fields_request_value")) {
			ProjectMetaData customFieldsRequest = new ProjectMetaData();
			customFieldsRequest.setName("kp.metadata.custom-fields-request");
			customFieldsRequest.setValue(populatecustomFields(data.get("log_custom_fields_request_name"),
					data.get("log_custom_fields_request_value")));
			projectMetaData.add(customFieldsRequest);
		}
		if (data.containsKey("log_custom_fields_response_name")
				&& data.containsKey("log_custom_fields_response_value")) {
			ProjectMetaData customFieldsResponse = new ProjectMetaData();
			customFieldsResponse.setName("kp.metadata.custom-fields-response");
			customFieldsResponse.setValue(populatecustomFields(data.get("log_custom_fields_response_name"),
					data.get("log_custom_fields_response_value")));
			projectMetaData.add(customFieldsResponse);
		}
		if (data.containsKey("Routing_xpath_region")) {
			ProjectMetaData Routingxpathregion = new ProjectMetaData();
			Routingxpathregion.setName("kp.metadata.routing.xpath.region");
			Routingxpathregion.setValue(data.get("Routing_xpath_region"));
			projectMetaData.add(Routingxpathregion);
		}
		if (data.containsKey("Routing_xpath_mrn")) {
			ProjectMetaData Routingxpathmrn = new ProjectMetaData();
			Routingxpathmrn.setName("kp.metadata.routing.xpath.mrn");
			Routingxpathmrn.setValue(data.get("Routing_xpath_mrn"));
			projectMetaData.add(Routingxpathmrn);
		}
		if (data.containsKey("Routing_xpath_mrntype")) {
			ProjectMetaData Routingxpathmrntype = new ProjectMetaData();
			Routingxpathmrntype.setName("kp.metadata.routing.xpath.mrntype");
			Routingxpathmrntype.setValue(data.get("Routing_xpath_mrntype"));
			projectMetaData.add(Routingxpathmrntype);
		}
		if (data.containsKey("Routing_constants_region")) {
			ProjectMetaData Routingconstantsregion = new ProjectMetaData();
			Routingconstantsregion.setName("kp.metadata.routing.constants.region");
			Routingconstantsregion.setValue(data.get("Routing_constants_region"));
			projectMetaData.add(Routingconstantsregion);
		}
		if (data.containsKey("Routing_constants_envlbl")) {
			ProjectMetaData Routingconstantsenvlbl = new ProjectMetaData();
			Routingconstantsenvlbl.setName("kp.metadata.routing.constants.envlbl");
			Routingconstantsenvlbl.setValue(data.get("Routing_constants_envlbl"));
			projectMetaData.add(Routingconstantsenvlbl);
		}
		if (data.containsKey("Routing_constants_urn"))
			projectMetaData
					.add(getMetadataElement("kp.metadata.routing.constants.urn", data.get("Routing_constants_urn")));
		if (data.containsKey("Routing_xpath_encounterID"))
			projectMetaData.add(
					getMetadataElement("kp.metadata.routing.xpath.encounterid", data.get("Routing_xpath_encounterID")));
		if (data.containsKey("Routing_xpath_encounterIDType"))
			projectMetaData.add(getMetadataElement("kp.metadata.routing.xpath.encounteridtype",
					data.get("Routing_xpath_encounterIDType")));
		if (data.containsKey("Interface_Type"))
			projectMetaData.add(getMetadataElement("kp.metadata.service-type", data.get("Interface_Type")));
		return projectMetaData;
	}

	private ProjectMetaData getMetadataElement(String name, String value) {
		ProjectMetaData projectMetaData = new ProjectMetaData();
		projectMetaData.setName(name);
		projectMetaData.setValue(value);
		return projectMetaData;
	}

	private String populatecustomFields(String key, String value) {
		String[] keys = key.split(";");
		String[] values = value.split(";");
		Map<String, List<ProjectMetaData>> data = new HashMap<String, List<ProjectMetaData>>();
		List<ProjectMetaData> customFields = new ArrayList<ProjectMetaData>();
		try {
			for (int i = 0; i < keys.length; i++) {
				ProjectMetaData customField = new ProjectMetaData();
				customField.setName(keys[i].replaceAll("\"", ""));
				customField.setValue(values[i]);
				customFields.add(customField);
			}
			ObjectMapper mapper = new ObjectMapper();
			data.put("custom-field", customFields);
			return mapper.writeValueAsString(data);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public Project readProxyData(Project project, String file) {
		try {
			DocumentContext context = JsonPath.parse(FileUtils.readFileToString(new File(file + "service.json")));
			project.setOwnerEmail(getAttributeValue(context, "$.metadata.ownerEmail"));
			project.setOrganization(getAttributeValue(context, "$.metadata.organization"));
			project.setTeamOwner(getAttributeValue(context, "$.metadata.teamOwner"));
			project.getProxies().get(0).setName(getAttributeValue(context, "$.metadata.proxyName"));
			project.getProxies().get(0).setVersion(getAttributeValue(context, "$.metadata.version"));
			project.getProxies().get(0).setInterfaceType(getAttributeValue(context, "$.metadata.interfaceType"));
			String serviceRegistry = getAttributeValue(context, "$.serviceRegistry");
			List<ServiceRegistry> serviceRegistries = new ObjectMapper().readValue(serviceRegistry, List.class);
			project.getProxies().get(0).setServiceRegistries(serviceRegistries);
			project.getProxies().add(0, populateAttachments(project.getProxies().get(0),
					file + File.separatorChar + "attachments" + File.separatorChar));
			project.getProxies().remove(1);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return project;
	}

	private Proxies populateAttachments(Proxies proxy, String path) {
		File[] fileList = new File(path).listFiles();
		List<ProjectFile> wsdlFiles = null;
		List<ProjectFile> xsdFiles = null;
		List<ProjectFile> attachments = null;
		for (File file : fileList) {
			String ext = FilenameUtils.getExtension(file.getName()).toUpperCase();
			switch (ext) {
				case "WSDL" :
					if (wsdlFiles == null)
						wsdlFiles = new ArrayList<ProjectFile>();
					ProjectFile wsdlFile = new ProjectFile();
					wsdlFile.setFileName(file.getName());
					wsdlFiles.add(wsdlFile);
					break;
				case "XSD" :
					if (xsdFiles == null)
						xsdFiles = new ArrayList<ProjectFile>();
					ProjectFile xsdFile = new ProjectFile();
					xsdFile.setFileName(file.getName());
					xsdFiles.add(xsdFile);
					break;
				default :
					if (attachments == null)
						attachments = new ArrayList<ProjectFile>();
					ProjectFile attachmentFile = new ProjectFile();
					attachmentFile.setFileName(file.getName());
					attachments.add(attachmentFile);
			}
		}
		proxy.setWsdlFiles(wsdlFiles);
		proxy.setXsdFiles(xsdFiles);
		proxy.setAttachments(attachments);
		return proxy;
	}

	private String getAttributeValue(DocumentContext context, String path) {
		String value = null;
		try {
			if (context.read(path) instanceof JSONArray) {
				JSONArray array = context.read(path);
				if (array.size() == 1)
					value = array.get(0).toString();
				else
					throw new Exception("Invalid JSON Path specified");
			} else
				value = context.read(path).toString();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return value;
	}

	public List<Category> populatePolicyTemplates(Map<String, String> data, List<Category> policyTemplates)
			throws ItorixException {
		for (Category category : policyTemplates) {
			switch (category.getType()) {
				case "trafficmanagement" :
					for (Policy policy : category.getPolicies()) {
						switch (policy.getName()) {
							case "spikearrest" :
								policy.setEnabled(true);
								break;
						}
					}
					break;
				case "security" :
					for (Policy policy : category.getPolicies()) {
						switch (policy.getName()) {
							case "Authentication" :
								if (data.containsKey("Security_Auth-N")
										&& data.get("Security_Auth-N").equalsIgnoreCase("TRUE"))
									policy.setEnabled(true);
								break;
							case "Authorization" :
								if (data.containsKey("Security_Auth-Z")
										&& data.get("Security_Auth-Z").equalsIgnoreCase("TRUE"))
									policy.setEnabled(true);
								break;
						}
					}
					break;
				case "mediation" :
					for (Policy policy : category.getPolicies()) {
						switch (policy.getName()) {
							case "XFM_Req_Scrub_WSSE_Header_v1" :
								if (data.containsKey("req_transformation")
										&& data.get("req_transformation").equals("XFM_Req_Scrub_WSSE_Header_v1"))
									policy.setEnabled(true);
								break;
							case "XFM_Req_Scrub_ESBAuth_Header_v1" :
								if (data.containsKey("req_transformation")
										&& data.get("req_transformation").equals("XFM_Req_Scrub_ESBAuth_Header_v1"))
									policy.setEnabled(true);
								break;
							case "XFM_Req_Scrub_BasicAuth_Header_v1" :
								if (data.containsKey("req_transformation")
										&& data.get("req_transformation").equals("XFM_Req_Scrub_BasicAuth_Header_v1"))
									policy.setEnabled(true);
								break;
							case "XFM_Req_Scrub_MetaData_Header_v1" :
								if (data.containsKey("req_transformation")
										&& data.get("req_transformation").equals("XFM_Req_Scrub_MetaData_Header_v1"))
									policy.setEnabled(true);
								break;
						}
					}
					break;
				case "threatprotection" :
					for (Policy policy : category.getPolicies()) {
						switch (policy.getName()) {
							case "THR_Threat_SOAP_v1" :
								if (data.containsKey("Threat_Flowname")
										&& data.get("Threat_Flowname").equals("THR_Threat_SOAP_v1"))
									policy.setEnabled(true);
								break;
							case "THR_REST_XML_v1" :
								if (data.containsKey("Threat_Flowname")
										&& data.get("Threat_Flowname").equals("THR_REST_XML_v1"))
									policy.setEnabled(true);
								break;
							case "THR_REST_JSON_v1" :
								if (data.containsKey("Threat_Flowname")
										&& data.get("Threat_Flowname").equals("THR_REST_JSON_v1"))
									policy.setEnabled(true);
								break;
						}
					}
					break;
				case "routing" :
					for (Policy policy : category.getPolicies()) {
						switch (policy.getName()) {
							case "RTE_KPHC_Endpoint_Lookup_v1" :
								if (data.containsKey("Routing_Flowname")
										&& data.get("Routing_Flowname").equalsIgnoreCase("RTE_KPHC_Endpoint_Lookup_v1"))
									policy.setEnabled(true);
								break;
							case "RTE_Endpoint_Lookup_v1" :
								if (data.containsKey("Routing_Flowname")
										&& data.get("Routing_Flowname").equalsIgnoreCase("RTE_Endpoint_Lookup_v1"))
									policy.setEnabled(true);
								break;
						}
					}
					break;
				case "MessageValidation" :
					for (Policy policy : category.getPolicies()) {
						switch (policy.getName()) {
							case "Schema_Validation_Request" :
								if (data.containsKey("Schema_Validation_Request")
										&& data.get("Schema_Validation_Request").equalsIgnoreCase("TRUE"))
									policy.setEnabled(true);
								break;
							case "Schema_Validation_Response" :
								if (data.containsKey("Schema_Validation_Response")
										&& data.get("Schema_Validation_Response").equalsIgnoreCase("TRUE"))
									policy.setEnabled(true);
								break;
							case "Schema_Validation_Error" :
								if (data.containsKey("Schema_Validation_Error")
										&& data.get("Schema_Validation_Error").equalsIgnoreCase("TRUE"))
									policy.setEnabled(true);
								break;
							case "Schema_Validation_MTOM" :
								if (data.containsKey("Schema_Validation_MTOM")
										&& data.get("Schema_Validation_MTOM").equalsIgnoreCase("TRUE"))
									policy.setEnabled(true);
								break;
						}
					}
					break;
				case "logging" :
					for (Policy policy : category.getPolicies()) {
						switch (policy.getName()) {
							case "LOG_Req_Res_Err_v1" :
								if (data.containsKey("Logging_Flowname")
										&& data.get("Logging_Flowname").equalsIgnoreCase("LOG_Req_Res_Err_v1"))
									policy.setEnabled(true);
								break;
						}
					}
					break;
				default :
					break;
			}
		}
		return policyTemplates;
	}
}
