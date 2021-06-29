package com.itorix.apiwiz.cicd.dao;

import java.io.IOException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.mail.MessagingException;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import com.itorix.apiwiz.cicd.beans.Metadata;
import com.itorix.apiwiz.cicd.beans.Package;
import com.itorix.apiwiz.cicd.beans.PackageMetadata;
import com.itorix.apiwiz.cicd.beans.PackagePipelineData;
import com.itorix.apiwiz.cicd.beans.PackageProjectData;
import com.itorix.apiwiz.cicd.beans.PackageProxy;
import com.itorix.apiwiz.cicd.beans.PackageReviewComents;
import com.itorix.apiwiz.cicd.beans.Pipeline;
import com.itorix.apiwiz.cicd.beans.PipelineGroups;
import com.itorix.apiwiz.cicd.beans.Proxy;
import com.itorix.apiwiz.cicd.beans.Stage;
import com.itorix.apiwiz.cicd.gocd.integrations.CiCdIntegrationAPI;
import com.itorix.apiwiz.common.model.SearchItem;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.model.proxystudio.ProxyArtifacts;
import com.itorix.apiwiz.common.model.proxystudio.ProxyData;
import com.itorix.apiwiz.common.model.proxystudio.apigeeassociations.Deployments;
import com.itorix.apiwiz.common.model.proxystudio.apigeeassociations.Product;
import com.itorix.apiwiz.common.model.proxystudio.apigeeassociations.ProxyApigeeDetails;
import com.itorix.apiwiz.common.properties.ApplicationProperties;
import com.itorix.apiwiz.common.util.mail.EmailTemplate;
import com.itorix.apiwiz.common.util.mail.MailUtil;
import com.itorix.apiwiz.identitymanagement.dao.BaseRepository;
import com.itorix.apiwiz.identitymanagement.dao.IdentityManagementDao;
import com.itorix.apiwiz.identitymanagement.model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

@Component
public class PackageDao {
	private static final Logger logger = LoggerFactory.getLogger(PackageDao.class);

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	CiCdIntegrationAPI cicdIntegrationApi;

	@Autowired
	ApplicationProperties applicationProperties;

	@Autowired
	private MailUtil mailUtil;

	@Autowired
	private IdentityManagementDao commonServices;

	@Autowired
	private BaseRepository baseRepository;

	public boolean validatePackage(String name){
		Query query = new Query(Criteria.where("packageName").is(name));
		List<Package> packages = mongoTemplate.find(query, Package.class);
		if(packages != null && packages.size() > 0)
			return false;
		else
			return true;
	}


	public List<String> getPackageNames(){
		Query query = new Query();
		query.fields().include("packageName");
		List<Package> packages = mongoTemplate.find(query, Package.class);
		List<String> names = new ArrayList<String>();
		if(packages!= null)
			for(Package packageNames : packages)
				names.add(packageNames.getPackageName());
		return names;
	}


	public Package savePackage(Package packageRequest, User user) throws ItorixException {
		try{
			if(packageRequest != null){
				packageRequest.setMetadata(manageMetadata(packageRequest.getMetadata(), user));
				packageRequest = mongoTemplate.save(packageRequest);
				return packageRequest;
			}
			return packageRequest;
		}
		catch(Exception ex){
			throw new ItorixException(ex.getMessage(),"Configuration-1000", ex );
		}
	}

	public boolean editPackage(Package packageRequest, User user) throws ItorixException {
		try{
			if(packageRequest != null){
				packageRequest.setMetadata(manageMetadata(packageRequest.getMetadata(), user));
				Query query = new Query(Criteria.where("packageId").is(packageRequest.getPackageId()));
				Package dbPackage = mongoTemplate.findOne(query, Package.class);
				packageRequest.setId(dbPackage.getId());
				mongoTemplate.save(packageRequest);

				//Document dbDoc = new Document(); 
				//mongoTemplate.getConverter().write(packageRequest, dbDoc);
				//Update update = Update.fromDocument(dbDoc,"_id");
				//UpdateResult result = mongoTemplate.updateFirst(query, update, Package.class);
				//Document dbDoc = new Document();
				//mongoTemplate.getConverter().write(packageRequest, dbDoc);
				//Update update = Update.fromDBObject(dbDoc, "_id");
				//UpdateResult result = mongoTemplate.updateFirst(query, update, Package.class);
				return true;
			}
			return false;
		}
		catch(Exception ex){
			throw new ItorixException(ex.getMessage(),"Package-1000", ex );
		}
	}


	public boolean approvePackage(Package packageRequest, User user) throws ItorixException {
		try{
			if(packageRequest != null){
				List<PackageMetadata> packageMetadataList = new ArrayList<PackageMetadata>();
				for(Proxy proxy : packageRequest.getPipelines()){
					PackageMetadata packageMetadata = new PackageMetadata();
					packageMetadata.setPipelineName(proxy.getPipelineName());
					packageMetadata.setProxy(proxy.getProxy().getName());
					try{
						cicdIntegrationApi.triggerStage("", proxy.getPipelineName(), proxy.getStage(), proxy.getBuildNumber());
						packageMetadata.setStatus("ACCEPTED");
					}catch(Exception ex){
						packageMetadata.setStatus("ERROR");
					}
					packageMetadataList.add(packageMetadata);
				}
				packageRequest.setMetadata(manageMetadata(packageRequest.getMetadata(), user));
				packageRequest.setApprovedBy(packageRequest.getMetadata().getModifiedBy());
				packageRequest.setApprovedOn(packageRequest.getMetadata().getMts().toString());
				packageRequest.setPackageMetadata(packageMetadataList);
				Query query = new Query(Criteria.where("packageId").is(packageRequest.getPackageId()));
				Document dbDoc = new Document(); 
				mongoTemplate.getConverter().write(packageRequest, dbDoc);
				Update update = Update.fromDocument(dbDoc,"_id");
				UpdateResult result = mongoTemplate.updateFirst(query, update, Package.class);

				//				DBObject dbDoc = new BasicDBObject();
				//				mongoTemplate.getConverter().write(packageRequest, dbDoc);
				//				Update update = Update.fromDBObject(dbDoc, "_id");
				//				WriteResult result = mongoTemplate.updateFirst(query, update, Package.class);
				if( result.isModifiedCountAvailable())
					return true;
			}
			return false;
		}
		catch(Exception ex){
			throw new ItorixException(ex.getMessage(),"Package-1000", ex );
		}
	}

	public boolean rejectPackage(Package packageRequest, User user) throws ItorixException {
		try{
			if(packageRequest != null){
				packageRequest.setMetadata(manageMetadata(packageRequest.getMetadata(), user));
				packageRequest.setApprovedBy(packageRequest.getMetadata().getModifiedBy());
				packageRequest.setApprovedOn(packageRequest.getMetadata().getMts().toString());
				Query query = new Query(Criteria.where("packageId").is(packageRequest.getPackageId()));
				Document dbDoc = new Document(); 
				mongoTemplate.getConverter().write(packageRequest, dbDoc);
				Update update = Update.fromDocument(dbDoc,"_id");
				UpdateResult result = mongoTemplate.updateFirst(query, update, Package.class);

				//				DBObject dbDoc = new BasicDBObject();
				//				mongoTemplate.getConverter().write(packageRequest, dbDoc);
				//				Update update = Update.fromDBObject(dbDoc, "_id");
				//				WriteResult result = mongoTemplate.updateFirst(query, update, Package.class);
				if( result.isModifiedCountAvailable())
					return true;
			}
			return false;
		}
		catch(Exception ex){
			throw new ItorixException(ex.getMessage(),"Package-1000", ex );
		}
	}

	public boolean reviewPackage(Package packageRequest, User user) throws ItorixException {
		try{
			if(packageRequest != null){
				try{
					sendEmailTo(packageRequest);
				}catch(Exception e){}
				packageRequest.setMetadata(manageMetadata(packageRequest.getMetadata(), user));
				Query query = new Query(Criteria.where("packageId").is(packageRequest.getPackageId()));
				Document dbDoc = new Document(); 
				mongoTemplate.getConverter().write(packageRequest, dbDoc);
				Update update = Update.fromDocument(dbDoc,"_id");
				UpdateResult result = mongoTemplate.updateFirst(query, update, Package.class);

				//				DBObject dbDoc = new BasicDBObject();
				//				mongoTemplate.getConverter().write(packageRequest, dbDoc);
				//				Update update = Update.fromDBObject(dbDoc, "_id");
				//				WriteResult result = mongoTemplate.updateFirst(query, update, Package.class);
				if( result.isModifiedCountAvailable())
					return true;
			}
			return false;
		}
		catch(Exception ex){
			throw new ItorixException(ex.getMessage(),"Package-1000", ex );
		}
	}

	private Metadata manageMetadata(Metadata existing, User user) {
		Metadata metadata = null;
		String username = (user != null && user.getFirstName() != null) ? user.getFirstName() + " " + user.getLastName() : "";
		String email =(user != null && user.getEmail() != null) ? user.getEmail() : "";

		if (existing == null || existing.getCreatedBy() == null) {
			metadata = new Metadata(username, email ,Instant.now().toEpochMilli(), username, email, Instant.now().toEpochMilli());
		} else {
			metadata = new Metadata(existing.getCreatedBy(), existing.getCreatedUserEmail(), existing.getCts(), username, email, Instant.now().toEpochMilli());
		}
		return metadata;
	}


	public boolean deletePackage(Package packageRequest, User user) throws ItorixException {
		return deletePackage(packageRequest.getPackageId(), user);
	}

	public boolean deletePackage(String packageRequest, User user) throws ItorixException {
		try{
			Query query = new Query(Criteria.where("packageId").is(packageRequest));
			DeleteResult result = mongoTemplate.remove(query, Package.class);
			if( result.getDeletedCount()>0)
				return result.wasAcknowledged();
			else 
				throw new ItorixException("No Record exists","Package-1004" );
		}
		catch(ItorixException ex){
			throw ex;
		}
		catch(Exception ex){
			throw new ItorixException(ex.getMessage(),"Package-1000", ex );
		}
	}


	public Object getPackages(String packageId) throws ItorixException{
		try{
			if(packageId != null){
				Query query = new Query(Criteria.where("packageId").is(packageId));
				return  mongoTemplate.findOne(query, Package.class);
			}
			else{
				List<Package> packages = mongoTemplate.findAll(Package.class); 
				for (Package packageElement : packages){
					packageElement.setPipelines(null);
					packageElement.setPackageMetadata(null);
				}
				return packages;
			}
		}
		catch(Exception ex){
			throw new ItorixException(ex.getMessage(),"", ex );
		}
	}

	public Object getPackages(int offset, int pageSize) throws ItorixException{
		try{
			Query query = new Query().with(Sort.by(Direction.DESC, "_id"))
					.skip(offset > 0 ? ((offset - 1) * pageSize) : 0).limit(pageSize);
			PackageOverviewResponse response = new PackageOverviewResponse();
			List<Package> packages = mongoTemplate.find(query,Package.class); 
			if(packages != null){
				for (Package packageElement : packages){
					packageElement.setPipelines(null);
				}
				Long counter = mongoTemplate.count(new Query(), Package.class);
				com.itorix.apiwiz.identitymanagement.model.Pagination pagination = new com.itorix.apiwiz.identitymanagement.model.Pagination();
				pagination.setOffset(offset);
				pagination.setTotal(counter);
				pagination.setPageSize(pageSize);
				response.setPagination(pagination);
				response.setData(packages);
			}
			return response;
		}
		catch(Exception ex){
			throw new ItorixException(ex.getMessage(),"", ex );
		}
	}


	public PackageProjectData getPackageProjectData(String projectName) throws ItorixException {
		logger.debug("inside getPackageProjectData(): Start");
		projectName = projectName.replaceAll(" " , "-").replaceAll("\\.", "");
		PipelineGroups pipelineGroups = null;
		if (projectName != null) {
			Query query = new Query(Criteria.where("_id").is(projectName));
			List<PipelineGroups> projects = mongoTemplate.find(query, PipelineGroups.class);
			logger.debug("projects : "+ getStringValue(projects));
			if (projects != null && projects.size() > 0) {
				pipelineGroups = projects.get(0);
			}
			logger.debug("inside getPackageProjectData(): Exit");
			return populatePackageProjectData(pipelineGroups);
		}
		return null;
	}



	public PackageProjectData getPackageProxyData(String proxy) throws ItorixException {
		logger.debug("inside getPackageProjectData(): Start");
		//projectName = projectName.replaceAll(" " , "-").replaceAll("\\.", "");
		PipelineGroups pipelineGroups = null;
		if (proxy != null) {
			Query query = new Query(Criteria.where("proxyName").is(proxy));
			List<Pipeline> projects = mongoTemplate.find(query, Pipeline.class);
			logger.debug("Pipelines for "+ proxy + " proxy  : "+ getStringValue(projects));
			//			if (projects != null && projects.size() > 0) {
			//				pipelineGroups = projects.get(0);
			//			}
			PackageProjectData data = new PackageProjectData();
			data.setProxyName(proxy);
			List<PackagePipelineData> pipelines = new ArrayList<PackagePipelineData>();
			for(Pipeline pipeline : projects){
				pipelines.add(populatePackageProjectData(pipeline));
			}
			data.setPipelines(pipelines);
			logger.debug("inside getPackageProjectData(): Exit");
			return data;
		}
		return null;
	}

	private PackageProjectData populatePackageProjectData(PipelineGroups pipelineGroups){
		logger.debug("inside populatePackageProjectData(): Start");
		PackageProjectData data = new PackageProjectData();
		data.setProjectName(pipelineGroups.getProjectName());
		List<PackagePipelineData> pipelineData = new ArrayList<PackagePipelineData>();
		for(Pipeline pipeline: pipelineGroups.getPipelines()){
			PackagePipelineData pipelineDataElement = new PackagePipelineData();
			pipelineDataElement.setName(pipeline.getName());
			String projectName = pipeline.getProjectName()!=null? pipeline.getProjectName() : pipelineGroups.getProjectName();
			//pipelineDataElement.setProxy(getPackageProxyData(pipeline.getProxyName(),projectName));
			List<String> stageList = new ArrayList<String>();
			for(Stage stage:pipeline.getStages())
				stageList.add(stage.getName());
			pipelineDataElement.setStages(stageList);
			pipelineDataElement.setBuildNumbers(populateHistoryData(pipelineGroups.getProjectName(), pipelineDataElement.getName()));
			if(pipelineDataElement.getBuildNumbers()!=null)
				pipelineData.add(pipelineDataElement);
		}
		data.setPipelines(pipelineData);
		logger.debug("inside populatePackageProjectData() data: " + getStringValue(data));
		logger.debug("inside populatePackageProjectData(): Exit");
		return data;
	}


	private PackagePipelineData populatePackageProjectData(Pipeline pipeline){
		logger.debug("inside populatePackageProjectData(): Start");
		PackagePipelineData data = new PackagePipelineData();
		//for(Pipeline pipeline: pipelineGroups.getPipelines()){
		data.setName(pipeline.getName());
		//String projectName = pipeline.getProjectName()!=null? pipeline.getProjectName() : pipelineGroups.getProjectName();
		data.setProxy(getProxyData(pipeline.getProxyName()));
		List<String> stageList = new ArrayList<String>();
		for(Stage stage:pipeline.getStages())
			stageList.add(stage.getName());
		data.setStages(stageList);
		data.setBuildNumbers(populateHistoryData(" ", data.getName()));
		//		if(pipelineDataElement.getBuildNumbers()!=null)
		//			pipelineData.add(pipelineDataElement);
		//}
		//data.setPipelines(pipelineData);
		logger.debug("inside populatePackageProjectData() data: " + getStringValue(data));
		logger.debug("inside populatePackageProjectData(): Exit");
		return data;
	}

	private String getPipelineProxy(String pipelineName) {
		Pipeline pipeline = null;
		if (pipelineName != null) {
			Query query = new Query(Criteria.where("_id").is(pipelineName));
			List<Pipeline> pipelines = mongoTemplate.find(query, Pipeline.class);
			if (pipelines != null && pipelines.size() > 0) {
				pipeline = pipelines.get(0);
				pipeline.getProxyName();
			}
		}
		return null;
	}

	private PackageProxy getProxyData(String proxyName){
		PackageProxy packageProxy = new PackageProxy();
		packageProxy.setName(proxyName);
		Query query = new Query(Criteria.where("proxyName").is(proxyName));
		List<ProxyData> proxies = mongoTemplate.find(query, ProxyData.class);

		if (proxies != null && proxies.size() > 0) {
			ProxyData proxyData = proxies.get(0);
			ProxyApigeeDetails proxyApigeeDetails = proxyData.getProxyApigeeDetails();
			try{
			if(proxyApigeeDetails != null){
				List<Deployments> deployments = proxyApigeeDetails.getDeployments();
				Set<String> devapps = new HashSet<>();
				Set<String> productList= null;
				Deployments deployment = deployments.get(0);
					if(deployment.getProxies().get(0).getProducts() != null){
						List<Product> products = deployment.getProxies().get(0).getProducts();
						productList = products.stream().map(o -> o.getName()).collect(Collectors.toSet());
						for(Product product : products){
							devapps.addAll(product.getDevApps().stream().map(o -> o.getName()).collect(Collectors.toSet()));
						}
					}
				packageProxy.setProducts(productList);
				packageProxy.setDevapps(devapps);
			}
			}catch(Exception e){
				e.printStackTrace();
			}
			ProxyArtifacts proxyArtifacts = proxyData.getProxyArtifacts();
			if(proxyArtifacts != null){
				packageProxy.setCaches(proxyArtifacts.getCaches());
				packageProxy.setKvm(proxyArtifacts.getKvms());
				packageProxy.setTargetServers(proxyArtifacts.getTargetServers());
			}
		}
		return packageProxy;
	}


	private  List<String> populateHistoryData(String projectName , String pipelineName){
		try {
			List<String> buildNumbers = new ArrayList<String>();
			int total = 1;
			int pageSize = 10;
			int counter =  total/pageSize;
			for(int i=0 ; i <= counter ; i++){
				String offset = String.valueOf(i*10);
				String history = cicdIntegrationApi.getPipelineHistory( projectName.replaceAll(" " , "-").replace(".", ""),pipelineName, offset);
				ObjectMapper mapper = new ObjectMapper();
				JsonNode jsonHistory = mapper.readTree(history);
				JsonNode pagination = jsonHistory.get("pagination");
				total = pagination.get("total").asInt();
				pageSize = pagination.get("page_size").asInt();
				counter =  total/pageSize;
				JsonNode pipelines = jsonHistory.get("pipelines");
				if (pipelines.isArray()) 
					for (final JsonNode pipeline : pipelines) 
						buildNumbers.add(pipeline.get("counter").asText());
			}
			return buildNumbers;
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private void sendEmailTo(Package packageRequest) throws MessagingException {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		String formatedDate = dateFormat.format(date);
		EmailTemplate emailTemplate = new EmailTemplate();
		ArrayList<String> toMailId = new ArrayList<String>();
		String body = null;
		if (packageRequest.getState().equalsIgnoreCase("REVIEW")) {
			List<String> allUsers = commonServices.getAllUsersWithRoleDevOPS();
			toMailId.addAll(allUsers);
			if(packageRequest.getMetadata().getModifiedUserEmail()!=null && packageRequest.getMetadata().getModifiedUserEmail()!="")
				toMailId.add(packageRequest.getMetadata().getModifiedUserEmail());
			body = MessageFormat.format(applicationProperties.getServiceRequestReviewBody(), packageRequest.getDescription(),
					formatedDate, packageRequest.getMetadata().getCreatedBy(), 
					(packageRequest.getComments() != null && packageRequest.getComments() != null) ? packageRequest.getComments() : "");
			emailTemplate.setToMailId(toMailId);
			emailTemplate.setBody(body);
			//emailTemplate.setSubject(MessageFormat.format(applicationProperties.getPackageRequestSubject(),packageRequest.getDescription()));
			mailUtil.sendEmail(emailTemplate);
		}
	}

	public void createOrUpdateReviewComment(PackageReviewComents packageReviewComents)throws Exception {
		if(packageReviewComents.getId() != null){
			PackageReviewComents sc = baseRepository.findById(packageReviewComents.getId(), PackageReviewComents.class);
			if (sc != null) {
				sc.setComment(packageReviewComents.getComment());
				baseRepository.save(sc);
			} else {
				baseRepository.save(packageReviewComents);
			}
		}
		else{
			baseRepository.save(packageReviewComents);
		}
	}

	public ObjectNode getReviewComment(PackageReviewComents packageReviewComents) throws MessagingException,ItorixException {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode rootNode = mapper.createObjectNode();
		ArrayNode arrayNode = mapper.createArrayNode();
		List<PackageReviewComents> list = getReviewComments(packageReviewComents);
		for (PackageReviewComents sc : list) {
			if(sc.getCommentId()==null){
				ObjectNode objectNode = mapper.createObjectNode();
				objectNode.put("id", sc.getId());
				objectNode.put("packageId", sc.getPackageId());
				objectNode.put("comment", sc.getComment());
				User u1 = commonServices.getUserById(sc.getCreatedBy());
				User modifieduser = commonServices.getUserById(sc.getModifiedBy());
				if (u1 != null) {
					objectNode.put("createdBy", sc.getCreatedUserName());
					objectNode.put("createdUserEmail", u1.getEmail());
					objectNode.put("modifiedBy",sc.getModifiedUserName());
					objectNode.put("modifiedUserEmail", modifieduser.getEmail());
					objectNode.put("cts", sc.getCts());
					objectNode.put("mts", sc.getMts());
				}
				objectNode.put("cts", sc.getCts());
				PackageReviewComents sc1 = sc;
				sc1.setCommentId(sc.getId());
				List<PackageReviewComents> list2 = getReviewCommentsByID(sc1);
				ArrayNode replayNode = mapper.createArrayNode();
				for (PackageReviewComents sc2 : list2) {
					ObjectNode objectNode1 = mapper.createObjectNode();
					objectNode1.put("id", sc2.getId());
					objectNode1.put("packageId", sc2.getPackageId());
					objectNode1.put("comment", sc2.getComment());
					User u2 = commonServices.getUserById(sc2.getCreatedBy());
					User modifieduser2 = commonServices.getUserById(sc.getModifiedBy());
					if (u2 != null) {
						objectNode1.put("createdBy", sc2.getCreatedUserName());
						objectNode1.put("createdUserEmail", u2.getEmail());
						objectNode1.put("modifiedBy",sc2.getModifiedUserName());
						objectNode1.put("modifiedUserEmail", modifieduser2.getEmail());
						objectNode1.put("cts", u2.getCts());
						objectNode1.put("mts", u2.getMts());
					}
					objectNode1.put("cts", sc2.getCts());
					replayNode.add(objectNode1);
				}
				objectNode.set("tags", replayNode);
				arrayNode.add(objectNode);
			}
		}
		rootNode.set("reviews", arrayNode);
		return rootNode;
	}

	private List<PackageReviewComents> getReviewComments(PackageReviewComents packageReviewComents) {
		Query query = new Query(Criteria.where("packageId").is(packageReviewComents.getPackageId()));
		List<PackageReviewComents> list =  mongoTemplate.find(query, PackageReviewComents.class);
		return list;
	}

	private List<PackageReviewComents> getReviewCommentsByID(PackageReviewComents packageReviewComents) {
		Query query = new Query(Criteria.where("commentId").is(packageReviewComents.getCommentId()));
		List<PackageReviewComents> list =  mongoTemplate.find(query, PackageReviewComents.class);
		return list;
	}

	private String getStringValue(Object obj){
		try {
			ObjectMapper mapper = new ObjectMapper();
			return mapper.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			return "parse exception";
		}
	}

	public Object searchPackage(String name, int limit) throws ItorixException
	{
		BasicQuery query = new BasicQuery("{\"packageName\": {$regex : '" + name + "', $options: 'i'}}");
		query.limit(limit > 0 ? limit : 10);
		List<Package> packages = mongoTemplate.find(query, Package.class);
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode response = mapper.createObjectNode();
		ArrayNode responseFields = mapper.createArrayNode();
		for (Package vo : packages) {
			SearchItem searchItem = new SearchItem();
			searchItem.setId(vo.getPackageId());
			searchItem.setName(vo.getPackageName());
			responseFields.addPOJO(searchItem);
		}
		response.set("packages", responseFields);
		return response;	
	}

}
