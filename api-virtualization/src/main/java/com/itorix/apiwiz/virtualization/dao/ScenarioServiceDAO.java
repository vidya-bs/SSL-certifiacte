package com.itorix.apiwiz.virtualization.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.identitymanagement.dao.IdentityManagementDao;
import com.itorix.apiwiz.identitymanagement.model.Pagination;
import com.itorix.apiwiz.identitymanagement.model.User;
import com.itorix.apiwiz.virtualization.model.ExpectationDTO;
import com.itorix.apiwiz.virtualization.model.ExpectationResponse;
import com.itorix.apiwiz.virtualization.model.ExpectationVO;
import com.itorix.apiwiz.virtualization.model.GroupHistoryResponse;
import com.itorix.apiwiz.virtualization.model.MockDTO;
import com.itorix.apiwiz.virtualization.model.expectation.Expectation;
import com.itorix.apiwiz.virtualization.model.logging.MockLog;
import com.itorix.hyggee.mockserver.client.serialization.ObjectMapperFactory;
import com.mongodb.client.result.DeleteResult;


@Component("scenarioServiceDAO")
public class ScenarioServiceDAO {
	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private IdentityManagementDao identityManagementDao;

	private ObjectMapper objectMapper = ObjectMapperFactory.createObjectMapper();

	public boolean saveScenario(ExpectationDTO expectationDTO)  {
		try {
			ExpectationVO expectationVO = new ExpectationVO();
			expectationVO.setName(expectationDTO.getScenarioName());
			expectationVO.setGroupId(expectationDTO.getGroupName());
			expectationVO.setPath(expectationDTO.getHttpRequest().getPath().toString());
			expectationVO.setDTO(objectMapper.writeValueAsString(expectationDTO));
			mongoTemplate.save(expectationVO);
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}


	public ExpectationVO saveScenarioDTO(MockDTO mockDTO)  {
		try {
			ExpectationVO expectationVO = null;
			Query query = new Query(Criteria.where("name").is(mockDTO.getName()));
			expectationVO = mongoTemplate.findOne(query, ExpectationVO.class);
			if(expectationVO == null){
				expectationVO = new ExpectationVO();
				expectationVO.setName(mockDTO.getName());
				expectationVO.setDescription(mockDTO.getDescription());
				expectationVO.setSummary(mockDTO.getSummary());
				expectationVO.setGroupId(mockDTO.getGroupId());
				expectationVO.setPath(mockDTO.getExpectation().getHttpRequest().getPath().toString());
				expectationVO.setDTO(objectMapper.writeValueAsString(mockDTO.getExpectation()));
				expectationVO.setExpectationMetadata(mockDTO.getExpectationMetadata());
				mongoTemplate.save(expectationVO);

				return expectationVO;
			}
			else{
				throw new ItorixException("Record exists", "General-1000");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public boolean updateScenario(MockDTO mockDTO, String id)  {
		try {
			if(id!= null && id !="") {
				Query query = new Query(Criteria.where("_id").is(id));
				ExpectationVO expectationVO = mongoTemplate.findOne(query, ExpectationVO.class);
				expectationVO.setName(mockDTO.getName());
				expectationVO.setGroupId(mockDTO.getGroupId());
				expectationVO.setPath(mockDTO.getExpectation().getHttpRequest().getPath().toString());
				expectationVO.setDTO(objectMapper.writeValueAsString(mockDTO.getExpectation()));
				expectationVO.setExpectationMetadata(mockDTO.getExpectationMetadata());
				mongoTemplate.save(expectationVO);
				return true;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}

	public List<Expectation> getExpectationDTOs()  {
		List <Expectation> expectationDTOs = new ArrayList<Expectation>();
		try {
			expectationDTOs = mongoTemplate.findAll(Expectation.class);
			return expectationDTOs;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public Expectation getExpectationDTOs(String id)  {
		try {
			Query query = new Query(Criteria.where("_id").is(id));
			Expectation expectation = mongoTemplate.findOne(query, Expectation.class);
			return expectation;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}


	public Expectation getExpectationsByGroup(String group)  {
		try {
			Query query = new Query(Criteria.where("group").is(group));
			Expectation expectation = mongoTemplate.findOne(query, Expectation.class);
			return expectation;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public List<Expectation> getExpectationByGroup(String group)  {
		try {
			Query query = new Query(Criteria.where("groupId").is(group));
			List<Expectation> expectation = mongoTemplate.find(query, Expectation.class);
			return expectation;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public boolean deletetExpectation(String scenarioId)  {
		try {
			Query query = new Query(Criteria.where("_id").is(scenarioId));
			DeleteResult result = mongoTemplate.remove(query,ExpectationVO.class);
			return  result.wasAcknowledged();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}

	public List<MockLog> getLogEntries(String expectationId)  {
		try {
			if(expectationId !=null){
				Query query = new Query(Criteria.where("expectationId").is(expectationId));
				List <MockLog> logentries = mongoTemplate.find(query, MockLog.class);
				for(int i=0 ;i<logentries.size();i++) {
					logentries.get(i).setHttpRequest(null);
					logentries.get(i).setHttpResponse(null);
				}
				return logentries;
			}
			else{
				List<MockLog> logentries = mongoTemplate.findAll(MockLog.class);
				for(int i=0 ;i<logentries.size();i++) {
					logentries.get(i).setHttpRequest(null);
					logentries.get(i).setHttpResponse(null);
				}

				return logentries;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public GroupHistoryResponse getLogEntries(String expectationName, String path, String match, int offset, int pageSize)  {
		try {
			Query query ;
			if(expectationName !=null)
				if(match != null){
					if(match.equalsIgnoreCase("true"))
						query = new Query(Criteria.where("expectationName").is(expectationName).and("wasMatched").is(true)).with(Sort.by(Direction.DESC, "_id")).skip(offset > 0 ? ((offset - 1) * pageSize) : 0).limit(pageSize);
					else
						query = new Query(Criteria.where("expectationName").is(expectationName).and("wasMatched").is(false)).with(Sort.by(Direction.DESC, "_id")).skip(offset > 0 ? ((offset - 1) * pageSize) : 0).limit(pageSize);
				}else{
					query = new Query(Criteria.where("expectationName").is(expectationName)).with(Sort.by(Direction.DESC, "_id")).skip(offset > 0 ? ((offset - 1) * pageSize) : 0).limit(pageSize);
				}
			else if(path != null )
				if(match != null){
					query = new Query(Criteria.where("path").is(path).and("match").is(match)).with(Sort.by(Direction.DESC, "_id")).skip(offset > 0 ? ((offset - 1) * pageSize) : 0).limit(pageSize);
				}else{
					query = new Query(Criteria.where("path").is(path)).with(Sort.by(Direction.DESC, "_id")).skip(offset > 0 ? ((offset - 1) * pageSize) : 0).limit(pageSize);
				}
			else
				query = new Query().with(Sort.by(Direction.DESC, "_id")).skip(offset > 0 ? ((offset - 1) * pageSize) : 0).limit(pageSize);

			List <MockLog> logentries = mongoTemplate.find(query, MockLog.class);
			for(int i=0 ;i<logentries.size();i++) {
				logentries.get(i).setHttpRequest(null);
				logentries.get(i).setHttpResponse(null);
			}
			if(logentries.size()>0){
				GroupHistoryResponse response = new GroupHistoryResponse();
				response.setData( logentries);
				Pagination pagination = new Pagination();
				long total = mongoTemplate.count(query,MockLog.class);
				pagination.setOffset(offset);
				pagination.setTotal(total);
				pagination.setPageSize(pageSize);
				response.setPagination(pagination);
				return response;
			}
			else
				return new GroupHistoryResponse();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}


	public MockLog getLogEntrie(String id)  {
		try {
			Query query = new Query(Criteria.where("_id").is(id));
			MockLog logEntry = mongoTemplate.findOne(query, MockLog.class);
			return logEntry;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public Map<String,Object> getLogExpectationNames()  {
		Map<String,Object> searchItems = new HashMap<>();
		try {
			List<String> names = mongoTemplate.findDistinct("expectationName", MockLog.class, String.class);
			List<String> paths = mongoTemplate.findDistinct("path", MockLog.class, String.class);
			searchItems.put("names", names);
			searchItems.put("paths", paths);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return searchItems;
	}

	private boolean isExpectationExist(String name) {
		Query query = new Query(Criteria.where("name").is(name));
		return mongoTemplate.count(query, Expectation.class) > 0;
	}

	public String createScenario(Expectation expectationRequest, String jsessionid) throws ItorixException {
		if (!isExpectationExist(expectationRequest.getName())) {
			User user = identityManagementDao.getUserDetailsFromSessionID(jsessionid);
			expectationRequest.setCreatedBy(user.getFirstName() + " " + user.getLastName());
			expectationRequest.setCts(System.currentTimeMillis());
			String pathValue = expectationRequest.getRequest().getPath().getValue();
			List<String> pathArray = getPathArrayForRegex(pathValue);
			expectationRequest.setPathArray(pathArray);
			mongoTemplate.insert(expectationRequest);
			return expectationRequest.getId();
		}
		throw new ItorixException(ErrorCodes.errorMessage.get("MockServer-1"), "MockServer-1");
	}

	public void updateScenario(Expectation expectationRequest, String scenarioId, String jsessionId) throws ItorixException {

		expectationRequest.setId(scenarioId);
		User user = identityManagementDao.getUserDetailsFromSessionID(jsessionId);
		expectationRequest.setModifiedBy(user.getFirstName() + " " + user.getLastName());
		expectationRequest.setMts(System.currentTimeMillis());
		Expectation expectation = mongoTemplate.findById(scenarioId, Expectation.class);
		if(expectation == null){
			throw new ItorixException(ErrorCodes.errorMessage.get("MockServer-2"), "MockServer-2");
		}
		String pathValue = expectationRequest.getRequest().getPath().getValue();
		List<String> pathArray = getPathArrayForRegex(pathValue);
		expectationRequest.setPathArray(pathArray);
		mongoTemplate.save(expectationRequest);
	}

	private List<String> getPathArrayForRegex(String requestPath) {
		String[] pathSplit = requestPath.split("/");
		List<String> pathList = new ArrayList<>();

		for (String path : pathSplit) {
			if(StringUtils.isEmpty(path)){
				continue;
			}
			if ((path.startsWith("{") && path.contains(":")) || path.contains("?") || path.contains("*")) {
				break;
			} else if (path.startsWith("{")) {
				pathList.add("*");
			} else {
				pathList.add(path);
			}
		}
		return pathList;
	}

	public void deleteScenario(String scenarioId) throws ItorixException {
		Query query = new Query(Criteria.where("id").is(scenarioId));
		if(mongoTemplate.remove(query, Expectation.class).getDeletedCount() == 0){
			throw new ItorixException(ErrorCodes.errorMessage.get("MockServer-2"), "MockServer-2");
		}
	}


	public Expectation getScenario(String scenarioId) throws ItorixException {
		Query query = new Query(Criteria.where("id").is(scenarioId));
		query.fields().exclude("pathArray");
		 Expectation expectation = mongoTemplate.findOne(query, Expectation.class);
		 if(expectation != null){
			 return expectation;
		 }
		 throw new ItorixException(ErrorCodes.errorMessage.get("MockServer-2"), "MockServer-2");
	}


	public ExpectationResponse getScenarios(String groupId, int offset, int pageSize) {

		Query query = new Query(Criteria.where("groupId").is(groupId)).with(Sort.by(Direction.DESC, "_id"))
				.skip(offset > 0 ? ((offset - 1) * pageSize) : 0).limit(pageSize);
		ExpectationResponse response = new ExpectationResponse();
		query.fields().exclude("pathArray");
		List<Expectation> portfolios = mongoTemplate.find(query, Expectation.class);
		Long counter = 0l;
		Pagination pagination = new Pagination();
		if (!CollectionUtils.isEmpty(portfolios)) {
			counter = mongoTemplate.count(query, Expectation.class);
			pagination.setOffset(offset);
			pagination.setPageSize(pageSize);
			response.setPagination(pagination);
			response.setData(portfolios);
		}
		pagination.setTotal(counter);
		response.setPagination(pagination);
		return response;
	}


}
