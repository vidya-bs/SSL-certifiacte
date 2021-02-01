package com.itorix.apiwiz.virtualization.dao;


import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.identitymanagement.model.Pagination;
import com.itorix.apiwiz.identitymanagement.model.ServiceRequestContextHolder;
import com.itorix.apiwiz.identitymanagement.model.User;
import com.itorix.apiwiz.identitymanagement.model.UserSession;
import com.itorix.apiwiz.identitymanagement.model.Workspace;
import com.itorix.apiwiz.virtualization.model.GroupHistoryResponse;
import com.itorix.apiwiz.virtualization.model.GroupVO;
import com.itorix.apiwiz.virtualization.model.Metadata;
import com.itorix.apiwiz.virtualization.model.expectation.Expectation;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

@Component("groupServiceDAO")
public class GroupServiceDAO {

	@Autowired
	private MongoTemplate mongoTemplate;
	@Qualifier("masterMongoTemplate")
	@Autowired
	private MongoTemplate masterMongoTemplate;
	@Autowired
	private ScenarioServiceDAO scenarioService;

	@Value("${itorix.core.mock.port:9002}")
	private String mockPort;


	private final String MOCK_URL = "http://#URL#-mock.apiwiz.io";

	private Workspace getWorkspace(String workapaceId){
		Query query  = new Query();
		query.addCriteria(new Criteria().orOperator(Criteria.where("name").is(workapaceId)));
		Workspace workspace = masterMongoTemplate.findOne(query, Workspace.class);
		return workspace;
	}

	public GroupHistoryResponse getGroups(int offset, int pageSize) throws ItorixException  {
		try {
			Query query = new Query().with(Sort.by(Direction.DESC, "_id")).skip(offset > 0 ? ((offset - 1) * pageSize) : 0).limit(pageSize);
			List<GroupVO> listVO = mongoTemplate.find(query,GroupVO.class);
			UserSession userSessionToken = ServiceRequestContextHolder.getContext().getUserSessionToken();
			Workspace workspace = getWorkspace(userSessionToken.getWorkspaceId());
			String tenantId = workspace.getKey();
			String mockURL = MOCK_URL.replaceAll("#URL#", userSessionToken.getWorkspaceId());
			mockURL = mockURL + ":" + mockPort;
			for(GroupVO vo : listVO){
				vo.setTenantId(tenantId);
				vo.setMockURL(mockURL);
			}
			if(listVO.size()>0){
				GroupHistoryResponse response = new GroupHistoryResponse();
				response.setData( polulateExpectation(listVO));
				Pagination pagination = new Pagination();
				long total = mongoTemplate.count(new Query(),GroupVO.class);
				long count = total/pageSize;
				count = total % pageSize > 0 ? count + 1: count;
				pagination.setOffset(offset);
				pagination.setTotal(total);
				pagination.setPageSize(pageSize);
				response.setPagination(pagination);
				return response;
			}
			else
				return new GroupHistoryResponse();
		}catch (Exception ex) {
			throw new ItorixException("","");
		}
	}
	
	public List<GroupVO> getGroups(String filter) throws ItorixException  {
		try {
			List<GroupVO> listVO = mongoTemplate.findAll(GroupVO.class);
			if(listVO.size()>0){
				listVO = polulateExpectation(listVO);
				return listVO;
			}
			else
				return null;
		}catch (Exception ex) {
			throw new ItorixException("","");
		}
	}

	public List<GroupVO> getGroupNames(){
		List<GroupVO> listVO = mongoTemplate.findAll(GroupVO.class);
		for(GroupVO vo : listVO)
		{
			vo.setMetadata(null);
			vo.setExpectations(null);
			vo.setDescription(null);
			vo.setSummary(null);
		}
		return listVO;
	}

	private List<GroupVO> polulateExpectation(List<GroupVO> listVO) {
		List<GroupVO> groupList = new ArrayList<GroupVO>();
		for(GroupVO groupVO : listVO){
			List<Map<Object, Object>> expectations = new ArrayList<Map<Object, Object>>();
			for(Expectation expectationVO: scenarioService.getExpectationByGroup(groupVO.getId())){
				Map<Object, Object> expectation = new HashMap<Object, Object>();
				expectation.put("expectationId", expectationVO.getId());
				expectation.put("expectationName", expectationVO.getName());
				expectations.add(expectation);
			}
			groupVO.setExpectations(expectations);
			groupList.add(groupVO);
		}
		return groupList;
	}

	public GroupVO getGroup(String groupId) throws ItorixException  {
		try {
			if(groupId!=null) {
				Query query = new Query(Criteria.where("_id").is(groupId));
				List<GroupVO> groupList = new ArrayList<GroupVO>();
				groupList.add(mongoTemplate.findOne(query, GroupVO.class));
				GroupVO vo = polulateExpectation(groupList).get(0);
				UserSession userSessionToken = ServiceRequestContextHolder.getContext().getUserSessionToken();
				Workspace workspace = getWorkspace(userSessionToken.getWorkspaceId());
				String tenantId = workspace.getKey();
				String mockURL = MOCK_URL.replaceAll("#URL#", userSessionToken.getWorkspaceId());
				mockURL = mockURL + ":" + mockPort;
				vo.setTenantId(tenantId);
				vo.setMockURL(mockURL);
				return vo;
			}
			else
				throw new ItorixException("","");
		} catch (ItorixException ex) {
			throw ex;
		}catch (Exception ex) {
			throw new ItorixException("","");
		}
	}

	public GroupVO getGroup(GroupVO group)  {
		try {
			if(group != null) {
				Query query = new Query(Criteria.where("_id").is(group.getId()));
				UserSession userSessionToken = ServiceRequestContextHolder.getContext().getUserSessionToken();
				Workspace workspace = getWorkspace(userSessionToken.getWorkspaceId());
				String tenantId = workspace.getKey();
				String mockURL = MOCK_URL.replaceAll("#URL#", userSessionToken.getWorkspaceId());
				mockURL = mockURL + ":" + mockPort;
				GroupVO vo = mongoTemplate.findOne(query, GroupVO.class);
				vo.setTenantId(tenantId);
				vo.setMockURL(mockURL);
				return vo;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public boolean isValidGroup(String groupId)  {
		try {
			if(groupId!=null) {
				Query query = new Query(Criteria.where("_id").is(groupId));
				if(mongoTemplate.findOne(query, GroupVO.class) != null)
					return true;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}

	public GroupVO saveGroup(GroupVO group, User user)  {
		try {
			group.setMetadata(manageMetadata(group.getMetadata(), user));
			mongoTemplate.save(group);
			return group;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public boolean updateGroup(GroupVO group, User user)  {
		try {
			Query query = new Query(Criteria.where("_id").is(group.getId()));
			Document dbDoc = new Document();
			group.setMetadata(manageMetadata(group.getMetadata(), user));
			mongoTemplate.getConverter().write(group, dbDoc);
			Update update = Update.fromDocument(dbDoc, "_id");
			UpdateResult result = mongoTemplate.updateFirst(query, update, GroupVO.class);
			return  result.wasAcknowledged();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}

	public boolean deleteGroup(GroupVO group)  {
		try {
			Query query = new Query(Criteria.where("_id").is(group.getId()));
			DeleteResult result = mongoTemplate.remove(query,GroupVO.class);
			return  result.wasAcknowledged();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}

	private Metadata manageMetadata(Metadata metadata2, User user) {
		Metadata metadata = null;
		String username = (user != null && user.getFirstName() != null) ? user.getFirstName() + " " + user.getLastName()
				: "";
		if (metadata2 == null || metadata2.getCreatedBy() == null) {
			metadata = new Metadata(username, Instant.now().toEpochMilli(), username, Instant.now().toEpochMilli());
		} else {
			metadata = new Metadata(metadata2.getCreatedBy(), metadata2.getCts(), username, Instant.now().toEpochMilli());
		}
		return metadata;
	}

}
