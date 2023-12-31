package com.itorix.apiwiz.datapower.dao;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.datapower.model.proxy.ApigeeConfig;
import com.itorix.apiwiz.datapower.model.proxy.Metadata;
import com.itorix.apiwiz.datapower.model.proxy.Policies;
import com.itorix.apiwiz.datapower.model.proxy.PolicyCategory;
import com.itorix.apiwiz.datapower.model.proxy.Proxy;
import com.itorix.apiwiz.identitymanagement.dao.IdentityManagementDao;
import com.itorix.apiwiz.identitymanagement.model.User;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Slf4j
@Component
public class ExcelReader {

	@Autowired
	private IdentityManagementDao identityManagementDao;

	@Autowired
	private MongoTemplate mongoTemplate;

	
	@Autowired
	private ProxyDAO proxyDAO;


	public Map<String, String> readDataFromExcel(String path, String jsessionid)
			throws InvalidFormatException, IOException, ItorixException {
		Map<String, String> portfolioData = new HashMap<>();
		try {
			List<Map<String, String>> data = readExcelData(path);
			List<Proxy> proxies = populatePortfolio(data);

			for (Proxy proxy : proxies) {
				Query query = new Query().addCriteria(Criteria.where("name").is(proxy.getName()));
				if (mongoTemplate.count(query, Proxy.class) != 0) {
					log.debug("Performing operations for portfolioDao");
					Proxy dbProxy = mongoTemplate.findOne(query, Proxy.class);
					portfolioData.put(proxy.getName(), "updated");

					proxyDAO.updateProxy(proxy);
				} else {
					User user = identityManagementDao.getUserDetailsFromSessionID(jsessionid);
					proxy.setMts(System.currentTimeMillis());
					proxy.setModifiedBy(user.getFirstName() + " " + user.getLastName());
					proxy.setCts(System.currentTimeMillis());
					proxy.setCreatedBy(user.getFirstName() + " " + user.getLastName());
					mongoTemplate.save(proxy);
					portfolioData.put(proxy.getName(), "created");
				}
			}
		} catch (Exception e) {
			throw new ItorixException("Error Reading Excel", "General-1000");
		}
		return portfolioData;
	}

	private List<Map<String, String>> readExcelData(String fileName) throws IOException, InvalidFormatException {
		Workbook workbook = WorkbookFactory.create(new File(fileName));
		Sheet sheet = workbook.getSheetAt(0);
		DataFormatter dataFormatter = new DataFormatter();
		List<Map<String, String>> dataElements = new ArrayList<>();
		for (Row row : sheet)
			if (row.getRowNum() > 0) {
				Map<String, String> data = new HashMap<>();
				for (Cell cell : row) {
					try {
						int colIndex = cell.getColumnIndex();
						String key = workbook.getSheetAt(0).getRow(0).getCell(colIndex).getStringCellValue();
						key = key.replaceAll("\\n", "").replaceAll("\\s+", "_");
						String cellValue = dataFormatter.formatCellValue(cell);
						String validKey = key.trim().toLowerCase();
						if (validKey != null) {
							data.put(validKey, cellValue);
						}
					} catch (Exception e) {
						log.info("Exception reading row " + row.getRowNum() + "");
					}
				}
				dataElements.add(data);
			}
		return dataElements;
	}

	private List<Proxy> populatePortfolio(List<Map<String, String>> dataElements) {
		List<Proxy> proxies = new ArrayList<>();
		for (Map<String, String> dataElement : dataElements) {
			//			String portfolioName = dataElement.get("portfolio.name");
			//			String projectName = dataElement.get("project.name");
			String proxyName = dataElement.get("proxy.name");

			/**
			Portfolio portfolio = null;
			try {
				portfolio = portfolios.stream().filter(p -> p.getName().equals(portfolioName)).findFirst().get();
			} catch (Exception e) {
			}
			if (ObjectUtils.isEmpty(portfolio)) {
				portfolio = createPortfolio(dataElement);
				portfolios.add(portfolio);
			}
			Projects project = null;
			if (ObjectUtils.isEmpty(portfolio.getProjects())) {
				log.debug("Setting projects to projectList");
				List<Projects> projectList = new ArrayList<>();
				project = createProject(dataElement);
				projectList.add(project);
				portfolio.setProjects(projectList);
			} else {
				try {
					project = portfolio.getProjects().stream().filter(p -> p.getName().equals(projectName)).findFirst()
							.get();
				} catch (Exception e) {
				}
				if (ObjectUtils.isEmpty(project)) {
					project = createProject(dataElement);
					portfolio.getProjects().add(project);
				}
			}
			 **/


			Proxy proxy = null;
			try {
				proxy = proxies.stream().filter(p -> p.getName().equals(proxyName)).findFirst().get();
			} catch (Exception e) {
			}
			if (proxy != null) {
				createProxy(dataElement, proxy);
			} else {
				proxy = new Proxy();
				proxy.setId(new ObjectId().toString());
				proxies.add(createProxy(dataElement, proxy));
			}

		}
		return proxies;
	}

	/*
	private Portfolio createPortfolio(Map<String, String> dataElement) {
		Portfolio portfolio = new Portfolio();
		portfolio.setName(dataElement.get("portfolio.name"));
		portfolio.setSummary(dataElement.get("portfolio.summary"));
		portfolio.setDescription(dataElement.get("portfolio.description"));
		portfolio.setOwner(dataElement.get("portfolio.owner"));
		portfolio.setOwnerEmail(dataElement.get("portfolio.owneremail"));
		return portfolio;
	}

	private Projects createProject(Map<String, String> dataElement) {
		Projects project = new Projects();
		project.setId(new ObjectId().toString());
		project.setName(dataElement.get("project.name"));
		project.setSummary(dataElement.get("project.summary"));
		project.setDescription(dataElement.get("project.description"));
		project.setOwner(dataElement.get("project.owner"));
		project.setOwner_email(dataElement.get("project.owneremail"));
		return project;
	}
	 */
	private Proxy createProxy(Map<String, String> dataElement, Proxy proxy) {
		proxy.setName(dataElement.get("proxy.name"));
		proxy.setSummary(dataElement.get("proxy.summary"));
		proxy.setProxyVersion(dataElement.get("proxy.version"));
		proxy.setOwner(dataElement.get("proxy.owner"));
		String[] tokens;
		try {
			tokens = dataElement.get("proxy.basepaths").trim().split(",");
		} catch (Exception e) {
			tokens = new String[1];
			tokens[0] = dataElement.get("proxy.basepaths");
		}
		proxy.setBasePaths(Arrays.asList(tokens));
		proxy.setApigeeConfig(createApigeeConfig(dataElement));
		return proxy;
	}

	private ApigeeConfig createApigeeConfig(Map<String, String> dataElement) {
		ApigeeConfig apigeeConfig = new ApigeeConfig();
		apigeeConfig.setApigeeVirtualHosts(Arrays.asList(dataElement.get("proxy.virtualhosts").trim().split(",")));
		apigeeConfig.setPolicyCategory(createPolicyCategory(dataElement));
		apigeeConfig.setMetadata(createMetaData(dataElement));
		return apigeeConfig;
	}

	private List<PolicyCategory> createPolicyCategory(Map<String, String> dataElement) {
		List<String> dataCategory = Arrays.asList(dataElement.get("proxy.policydetails").trim().split(","));
		List<PolicyCategory> policies = new ArrayList<>();
		for (String category : dataCategory) {
			String[] tokens = category.split(";");
			String categoryName = tokens[0];
			String policyName = tokens[1];
			PolicyCategory policyCategory = null;
			try {
				policyCategory = policies.stream().filter(p -> p.getType().equalsIgnoreCase(categoryName)).findFirst()
						.get();
			} catch (Exception e) {
			}
			if (null == policyCategory) {
				policyCategory = new PolicyCategory();
				policyCategory.setType(categoryName);
				List<Policies> proxyPolicies = new ArrayList<>();
				Policies policy = new Policies();
				policy.setName(policyName);
				policy.setEnabled(Boolean.TRUE);
				proxyPolicies.add(policy);
				policies.add(policyCategory);
				policyCategory.setPolicies(proxyPolicies);
			} else {
				Policies policy = null;
				try {
					policy = policyCategory.getPolicies().stream().filter(p -> p.getName().equalsIgnoreCase(policyName))
							.findFirst().get();
				} catch (Exception e) {
				}
				if (policy == null) {
					policy = new Policies();
					policy.setName(policyName);
					policy.setEnabled(Boolean.TRUE);
					policyCategory.getPolicies().add(policy);
				}
			}
		}
		return policies;
	}

	private List<Metadata> createMetaData(Map<String, String> dataElement) {
		List<Metadata> metadataList = new ArrayList<>();
		List<String> dataMetadata = Arrays.asList(dataElement.get("proxy.metadata").trim().split(","));
		for (String metaDataItem : dataMetadata) {
			Metadata metadata = new Metadata();
			String[] metadataTokens = metaDataItem.split(";");
			metadata.setName(metadataTokens[0]);
			metadata.setValue(metadataTokens[1]);
			metadataList.add(metadata);
		}
		return metadataList;
	}

}
