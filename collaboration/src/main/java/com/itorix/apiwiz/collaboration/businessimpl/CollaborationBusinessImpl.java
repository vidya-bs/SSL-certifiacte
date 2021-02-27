package com.itorix.apiwiz.collaboration.businessimpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.itorix.apiwiz.collaboration.business.CollaborationBusiness;
import com.itorix.apiwiz.collaboration.model.Swagger3VO;
import com.itorix.apiwiz.collaboration.model.SwaggerContacts;
import com.itorix.apiwiz.collaboration.model.SwaggerMetadata;
import com.itorix.apiwiz.collaboration.model.SwaggerTeam;
import com.itorix.apiwiz.collaboration.model.SwaggerVO;
import com.itorix.apiwiz.collaboration.model.TeamsHistoryResponse;
import com.itorix.apiwiz.common.model.SearchItem;
import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.identitymanagement.dao.BaseRepository;
import com.itorix.apiwiz.identitymanagement.model.Pagination;
import com.itorix.apiwiz.identitymanagement.model.ServiceRequestContextHolder;
import com.itorix.apiwiz.identitymanagement.model.User;
import com.itorix.apiwiz.identitymanagement.model.UserSession;

@Service
public class CollaborationBusinessImpl implements CollaborationBusiness {
	private static final Logger logger = LoggerFactory.getLogger(CollaborationBusinessImpl.class);
	@Autowired
	BaseRepository baseRepository;

	@Autowired
	private MongoTemplate mongoTemplate;
	@Qualifier("masterMongoTemplate")
	@Autowired
	private MongoTemplate masterMongoTemplate;

	/**
	 * findSwaggerTeam
	 * 
	 * @param swaggerTeam
	 * @return
	 */
	public SwaggerTeam findSwaggerTeam(SwaggerTeam swaggerTeam) {
		log("findSwagger", swaggerTeam.getInteractionid(), swaggerTeam);
		return baseRepository.findOne("name", swaggerTeam.getName(), SwaggerTeam.class);
	}

	/**
	 * createTeam
	 * 
	 * @param swaggerTeam
	 * @throws ItorixException
	 */
	public void createTeam(SwaggerTeam swaggerTeam) throws ItorixException {
		log("createTeam", swaggerTeam.getInteractionid(), swaggerTeam);
		if (swaggerTeam != null) {
			Set<String> swaggerSet = swaggerTeam.getSwaggers();
			if (swaggerSet != null)
				for (String swaggerName : swaggerSet) {
					Query query = new Query(Criteria.where("swaggerName").is(swaggerName).and("oas").is("2.0"));
					SwaggerMetadata metadata = mongoTemplate.findOne(query, SwaggerMetadata.class);
					if (metadata == null) {
						metadata = new SwaggerMetadata();
						metadata.setSwaggerName(swaggerName);
						metadata.setOas("2.0");
					}
					Set<String> teamSet = metadata.getTeams() != null ? metadata.getTeams() : new HashSet<String>();
					teamSet.add(swaggerTeam.getName());
					metadata.setTeams(teamSet);
					mongoTemplate.save(metadata);
				}
			swaggerSet = swaggerTeam.getSwagger3();
			if (swaggerSet != null)
				for (String swaggerName : swaggerSet) {
					Query query = new Query(Criteria.where("swaggerName").is(swaggerName).and("oas").is("3.0"));
					SwaggerMetadata metadata = mongoTemplate.findOne(query, SwaggerMetadata.class);
					if (metadata == null) {
						metadata = new SwaggerMetadata();
						metadata.setSwaggerName(swaggerName);
						metadata.setOas("3.0");
					}
					Set<String> teamSet = metadata.getTeams() != null ? metadata.getTeams() : new HashSet<String>();
					teamSet.add(swaggerTeam.getName());
					metadata.setTeams(teamSet);
					mongoTemplate.save(metadata);
				}
			swaggerTeam = baseRepository.save(swaggerTeam);
		}
	}

	/**
	 * updateTeam
	 * 
	 * @param swaggerTeam
	 * @param name
	 * @throws ItorixException
	 */
	public void updateTeam(SwaggerTeam swaggerTeam, String name) throws ItorixException {
		log("createTeam", swaggerTeam.getInteractionid(), swaggerTeam);
		UserSession userSessionToken = ServiceRequestContextHolder.getContext().getUserSessionToken();
		User user = getUserDetailsFromSessionID(swaggerTeam.getJsessionid());
		boolean isAdmin = user.isWorkspaceAdmin(userSessionToken.getWorkspaceId());
		SwaggerTeam team = baseRepository.findOne("name", name, SwaggerTeam.class);

		if (team != null) {
			boolean isTeamAdmin = false;
			for (SwaggerContacts sc : team.getContacts()) {
				if (sc.getEmail().equals(user.getEmail())) {
					isTeamAdmin = true;
				}
			}
			if (isTeamAdmin || isAdmin) {
				Set<String> removeSet = symmetricDifference(team.getSwaggers(), swaggerTeam.getSwaggers());
				if(removeSet != null && removeSet.size() >0)
				for (String s : removeSet) {
					Query query = new Query(Criteria.where("swaggerName").is(s).and("oas").is("2.0"));
					SwaggerMetadata metadata = mongoTemplate.findOne(query, SwaggerMetadata.class);
					if (metadata != null && metadata.getTeams() != null) {
						Set<String> swaggerTeams = metadata.getTeams();
						swaggerTeams.remove(name);
						metadata.setTeams(swaggerTeams);
					}
					if(metadata != null)
					mongoTemplate.save(metadata);
				}
				removeSet = symmetricDifference(team.getSwagger3(), swaggerTeam.getSwagger3());
				if(removeSet != null && removeSet.size() >0)
				for (String s : removeSet) {
					Query query = new Query(Criteria.where("swaggerName").is(s).and("oas").is("3.0"));
					SwaggerMetadata metadata = mongoTemplate.findOne(query, SwaggerMetadata.class);
					if (metadata != null && metadata.getTeams() != null) {
						Set<String> swaggerTeams = metadata.getTeams();
						swaggerTeams.remove(name);
						metadata.setTeams(swaggerTeams);
					}
					if(metadata != null)
					mongoTemplate.save(metadata);
				}
				Set<String> addSet = symmetricDifference(swaggerTeam.getSwaggers(), team.getSwaggers());
				for (String s : addSet) {
					Query query = new Query(Criteria.where("swaggerName").is(s).and("oas").is("2.0"));
					SwaggerMetadata metadata = mongoTemplate.findOne(query, SwaggerMetadata.class);
					Set<String> swaggerTeams = null;
					if (metadata == null) {
						metadata = new SwaggerMetadata();
						metadata.setOas("2.0");
						metadata.setSwaggerName(s);
					}
					swaggerTeams = metadata.getTeams() == null ? new HashSet<String>() : metadata.getTeams();
					swaggerTeams.add(name);
					metadata.setTeams(swaggerTeams);
					if(metadata != null)
					mongoTemplate.save(metadata);
				}
				addSet = symmetricDifference(swaggerTeam.getSwagger3(), team.getSwagger3());
				for (String s : addSet) {
					Query query = new Query(Criteria.where("swaggerName").is(s).and("oas").is("3.0"));
					SwaggerMetadata metadata = mongoTemplate.findOne(query, SwaggerMetadata.class);
					Set<String> swaggerTeams = null;
					if (metadata == null) {
						metadata = new SwaggerMetadata();
						metadata.setOas("3.0");
						metadata.setSwaggerName(s);
					}
					swaggerTeams = metadata.getTeams() == null ? new HashSet<String>() : metadata.getTeams();
					swaggerTeams.add(name);
					metadata.setTeams(swaggerTeams);
					if(metadata != null)
					mongoTemplate.save(metadata);
				}
				team.setContacts(swaggerTeam.getContacts());
				team.setSwaggers(swaggerTeam.getSwaggers());
				team.setSwagger3(swaggerTeam.getSwagger3());
				team.setProjects(swaggerTeam.getProjects());
				team.setDisplayName(swaggerTeam.getDisplayName());
				swaggerTeam = baseRepository.save(team);
			} else {
				throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Teams-1002"), name), "Teams-1002");
			}
		} else {
			throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Teams-1001"), name), "Teams-1001");
		}
	}

	public SwaggerTeam getTeam(String teamName, String interactionid) throws ItorixException {
		SwaggerTeam team = baseRepository.findOne("name", teamName, SwaggerTeam.class);
		if (team != null)
			return team;
		throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Teams-1001"), teamName), "Teams-1001");
	}

	/**
	 * deleteTeam
	 * 
	 * @param teamName
	 * @param interactionid
	 * @param jsessionid
	 * @throws ItorixException
	 */
	public void deleteTeam(String teamName, String interactionid, String jsessionid) throws ItorixException {
		log("deleteTeam", interactionid, jsessionid, teamName);
		UserSession userSessionToken = ServiceRequestContextHolder.getContext().getUserSessionToken();
		User user = getUserDetailsFromSessionID(jsessionid);
		boolean isAdmin = user.isWorkspaceAdmin(userSessionToken.getWorkspaceId());
		boolean isTeamAdmin = false;
		SwaggerTeam team = baseRepository.findOne("name", teamName, SwaggerTeam.class);
		if (!isAdmin)
			if (team != null) {
				for (SwaggerContacts sc : team.getContacts()) {
					if (sc.getEmail().equals(user.getEmail())) {
						if (sc.getRole() != null && sc.getRole().contains("Admin"))
							isTeamAdmin = true;
					} else {
						throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Teams-1002"), teamName),
								"Teams-1002");
					}
				}
			} else {
				throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Teams-1001"), teamName), "Teams-1001");
			}
		if (isAdmin || isTeamAdmin) {
			if (team.getSwaggers() != null)
				for (String s : team.getSwaggers()) {
					Query query = new Query(Criteria.where("swaggerName").is(s).and("oas").is("2.0"));
					SwaggerMetadata metadata = mongoTemplate.findOne(query, SwaggerMetadata.class);
					if (metadata != null && metadata.getTeams() != null) {
						Set<String> swaggerTeams = metadata.getTeams();
						swaggerTeams.remove(teamName);
						metadata.setTeams(swaggerTeams);
					}
					mongoTemplate.save(metadata);
				}
			if (team.getSwagger3() != null)
				for (String s : team.getSwagger3()) {
					Query query = new Query(Criteria.where("swaggerName").is(s).and("oas").is("3.0"));
					SwaggerMetadata metadata = mongoTemplate.findOne(query, SwaggerMetadata.class);
					if (metadata != null && metadata.getTeams() != null) {
						Set<String> swaggerTeams = metadata.getTeams();
						swaggerTeams.remove(teamName);
						metadata.setTeams(swaggerTeams);
					}
					mongoTemplate.save(metadata);
				}
			baseRepository.delete("name", teamName, SwaggerTeam.class);
		}
	}

	/**
	 * associateTeam
	 * 
	 * @param swaggerName
	 * @param teamSet
	 * @param interactionId
	 * @throws ItorixException
	 */
	public void associateTeam(String swaggerName, Set<String> teamSet, String interactionId) throws ItorixException {
		log("associateTeam", interactionId, swaggerName, teamSet);
		for (String teamName : teamSet) {
			SwaggerTeam team = baseRepository.findOne("name", teamName, SwaggerTeam.class);
			if (team != null) {
				SwaggerVO vo = baseRepository.findOne("name", swaggerName, SwaggerVO.class);
				if (vo != null) {
					Set<String> teams = vo.getTeams();
					if (teams == null) {
						teams = new HashSet<String>();
					}
					teams.add(teamName);
					vo.setTeams(teams);
					baseRepository.save(vo);
					Set<String> swaggers = team.getSwaggers();
					if (swaggers == null) {
						swaggers = new HashSet<String>();
					}
					swaggers.add(swaggerName);
					team.setSwaggers(swaggers);
					team = baseRepository.save(team);
				} else {
					throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Swagger-1001")),
							"Swagger-1001");
				}
			} else {
				throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Teams-1001"), teamName),
						"Teams-1001");
			}
		}
	}

	public SwaggerMetadata getSwaggerMetadata(String swaggerName, String oas) {
		Query query = new Query(Criteria.where("swaggerName").is(swaggerName).and("oas").is(oas));
		SwaggerMetadata metadata = mongoTemplate.findOne(query, SwaggerMetadata.class);
		return metadata;
	}

	public void associateTeam(String swaggerName, Set<String> teamSet, String interactionId, String oas)
			throws ItorixException {
		log("associateTeam", interactionId, swaggerName, teamSet);
		if (oas.equals("2.0")) {
			SwaggerVO vo = findSwagger(swaggerName, interactionId);
			if (vo != null)
				swaggerName = vo.getName();
		} else if (oas.equals("3.0")) {
			Swagger3VO vo = findSwagger3(swaggerName, interactionId);
			if (vo != null)
				swaggerName = vo.getName();
		}
		Query query = new Query();
		if (oas.equals("2.0"))
			query.addCriteria(Criteria.where("swaggers").is(swaggerName));
		else if (oas.equals("3.0"))
			query.addCriteria(Criteria.where("swagger3").is(swaggerName));
		List<SwaggerTeam> teamlist = baseRepository.find(query, SwaggerTeam.class);
		Set<String> existingNames = new HashSet<String>();
		for (SwaggerTeam team : teamlist) {
			if (!teamSet.contains(team.getName())) {
				Set<String> swaggers = (oas.equals("2.0")) ? team.getSwaggers() : team.getSwagger3();
				swaggers.remove(swaggerName);
				if (oas.equals("2.0"))
					team.setSwaggers(swaggers);
				else if (oas.equals("3.0"))
					team.setSwagger3(swaggers);
				baseRepository.save(team);
			}
			existingNames.add(team.getName());
		}
		for (String teamName : teamSet) {
			if (!existingNames.contains(teamName)) {
				SwaggerTeam team = baseRepository.findOne("name", teamName, SwaggerTeam.class);
				if (team != null) {
					Set<String> swaggers = (oas.equals("2.0")) ? team.getSwaggers() : team.getSwagger3();
					if (swaggers == null) {
						swaggers = new HashSet<String>();
					}
					swaggers.add(swaggerName);
					if (oas.equals("2.0"))
						team.setSwaggers(swaggers);
					else if (oas.equals("3.0"))
						team.setSwagger3(swaggers);
					baseRepository.save(team);
				}
			}
		}
		query = new Query(Criteria.where("swaggerName").is(swaggerName).and("oas").is(oas));
		SwaggerMetadata metadata = mongoTemplate.findOne(query, SwaggerMetadata.class);
		if (metadata == null) {
			metadata = new SwaggerMetadata();
			metadata.setSwaggerName(swaggerName);
			metadata.setOas(oas);
		}
		metadata.setTeams(teamSet);
		mongoTemplate.save(metadata);
	}
	
	public List<SwaggerTeam> getTeamsBySwaggerName(String swaggerName, String oas){
		Query query = new Query();
		if (oas.equals("2.0"))
			query.addCriteria(Criteria.where("swaggers").is(swaggerName));
		else if (oas.equals("3.0"))
			query.addCriteria(Criteria.where("swagger3").is(swaggerName));
		List<SwaggerTeam> teamlist = baseRepository.find(query, SwaggerTeam.class);
		return teamlist;
	}
	
	/**
	 * findSwaggerTeames
	 * 
	 * @param jsessionid
	 * @param interactionid
	 * @return
	 */
	public TeamsHistoryResponse findSwaggerTeames(String jsessionid, String interactionid, int offset, int pageSize) {
		log("findSwaggerTeames", interactionid);
		UserSession userSessionToken = ServiceRequestContextHolder.getContext().getUserSessionToken();
		User user = getUserDetailsFromSessionID(jsessionid);
		boolean isAdmin = user.isWorkspaceAdmin(userSessionToken.getWorkspaceId());
		
		TeamsHistoryResponse historyResponse = new TeamsHistoryResponse();
		List<SwaggerTeam> swaggerTeams = new ArrayList<SwaggerTeam>();
		if (isAdmin) {
			Query query = new Query().with(Sort.by(Direction.DESC, "_id"))
					.skip(offset > 0 ? ((offset - 1) * pageSize) : 0).limit(pageSize);
			swaggerTeams = baseRepository.find(query, SwaggerTeam.class);
		} else {
			Query query = new Query(Criteria.where("contacts.email").is(user.getEmail()))
					.with(Sort.by(Direction.DESC, "_id")).skip(offset > 0 ? ((offset - 1) * pageSize) : 0)
					.limit(pageSize);
			//query.addCriteria(Criteria.where("contacts.email").is(user.getEmail()));
			swaggerTeams = baseRepository.find(query, SwaggerTeam.class);
		}
		if (swaggerTeams != null) {
			Query query;
			if (isAdmin)
				query = new Query();
			else
				query = new Query(Criteria.where("contacts.email").is(user.getEmail()));
			Long counter = mongoTemplate.count(query, SwaggerTeam.class);
			Pagination pagination = new Pagination();
			pagination.setOffset(offset);
			pagination.setTotal(counter);
			pagination.setPageSize(pageSize);
			historyResponse.setPagination(pagination);
			historyResponse.setData(swaggerTeams);
		}
		return historyResponse;
	}

	/**
	 * findSwaggerTeameNames
	 * 
	 * @param jsessionid
	 * @param interactionid
	 * @return
	 */
	public List<String> findSwaggerTeameNames(String jsessionid, String interactionid) {
		List<String> responseList = new ArrayList<>();
		log("findSwaggerTeames", interactionid);
		UserSession userSessionToken = ServiceRequestContextHolder.getContext().getUserSessionToken();
		User user = getUserDetailsFromSessionID(jsessionid);
		boolean isAdmin = user.isWorkspaceAdmin(userSessionToken.getWorkspaceId());
		if (isAdmin) {
			List<SwaggerTeam> list = baseRepository.findAll(SwaggerTeam.class);
			for (SwaggerTeam team : list) {
				responseList.add(team.getName());
			}
		} else {
			Query query = new Query();
			query.addCriteria(Criteria.where("contacts.email").is(user.getEmail()));
			List<SwaggerTeam> list = baseRepository.find(query, SwaggerTeam.class);
			for (SwaggerTeam team : list) {
				responseList.add(team.getName());
			}
		}
		return responseList;
	}

	/**
	 * getTeamPermissions
	 * 
	 * @param interactionid
	 * @param jsessionid
	 * @return
	 * @throws JsonProcessingException
	 */
	public String getTeamPermissions(String interactionid, String jsessionid)
			throws JsonProcessingException, ItorixException {
		log("getTeamPermissions", interactionid, jsessionid);
		ObjectMapper mapper = new ObjectMapper();
		ArrayNode arrayNode = mapper.createArrayNode();
		UserSession userSessionToken = ServiceRequestContextHolder.getContext().getUserSessionToken();
		User user = getUserDetailsFromSessionID(jsessionid);
		boolean isAdmin = user.isWorkspaceAdmin(userSessionToken.getWorkspaceId());
		if (user != null) {
			if (isAdmin) {
				List<SwaggerTeam> list = baseRepository.findAll(SwaggerTeam.class);
				for (SwaggerTeam st : list) {
					ObjectNode objectNode = mapper.createObjectNode();
					objectNode.put("type", user.getUserWorkspace(userSessionToken.getWorkspaceId()).getUserType() != null ? user.getUserWorkspace(userSessionToken.getWorkspaceId()).getUserType() : "Admin");
					objectNode.putPOJO("roles", Arrays.asList("Admin", "Write", "Read"));
					objectNode.put("name", st.getName());
					arrayNode.add(objectNode);
				}
			} else {
				Query query = new Query();
				query.addCriteria(Criteria.where("contacts.email").is(user.getEmail()));
				List<SwaggerTeam> list = baseRepository.find(query, SwaggerTeam.class);
				for (SwaggerTeam st : list) {
					ObjectNode objectNode = mapper.createObjectNode();
					objectNode.put("type", user.getUserWorkspace(userSessionToken.getWorkspaceId()).getUserType() != null ? user.getUserWorkspace(userSessionToken.getWorkspaceId()).getUserType() : "Non_Admin");
					for (SwaggerContacts sc : st.getContacts())
						if (sc.getEmail().equals(user.getEmail()))
							if (sc.getRole() != null) {
								objectNode.putPOJO("roles", sc.getRole());
								break;
							}
					objectNode.put("name", st.getName());
					arrayNode.add(objectNode);
				}
			}
		}
		return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(arrayNode);
	}

	/**
	 * findSwagger
	 * 
	 * @param name
	 * @param interactionid
	 * @return
	 */
	public SwaggerVO findSwagger(String name, String interactionid) throws ItorixException {
		log("findSwagger", interactionid, name);
		SwaggerVO vo = baseRepository.findOne("id", name, SwaggerVO.class);
		if(vo == null)
			vo = baseRepository.findOne("swaggerId", name, SwaggerVO.class);
		if (vo == null)
			vo = baseRepository.findOne("name", name, SwaggerVO.class);
		return vo;
	}

	public Swagger3VO findSwagger3(String name, String interactionid) throws ItorixException {
		log("findSwagger", interactionid, name);
		Swagger3VO vo = baseRepository.findOne("id", name, Swagger3VO.class);
		if(vo == null)
			vo = baseRepository.findOne("swaggerId", name, Swagger3VO.class);
		if (vo == null)
			vo = baseRepository.findOne("name", name, Swagger3VO.class);
		return vo;
	}

	/**
	 * symmetricDifference
	 * 
	 * @param existing
	 * @param newSet
	 * @return
	 */
	private Set<String> symmetricDifference(Set<String> existing, Set<String> newSet) {
		if (newSet == null)
			return existing == null ? new HashSet<String>() : existing;
		Set<String> result = existing == null ? new HashSet<String>() : new HashSet<String>(existing);
		for (String element : newSet) {
			if (!result.add(element)) {
				result.remove(element);
			}
		}
		return result;
	}

	private User getUserDetailsFromSessionID(String jsessionid) {
		UserSession userSessionToken = masterMongoTemplate.findById(jsessionid, UserSession.class);
		User user = masterMongoTemplate.findById(userSessionToken.getUserId(), User.class);
		return user;
	}

	/**
	 * teamSearch
	 * 
	 * @param interactionid
	 * @return
	 * @throws JsonProcessingException
	 */
	public Object teamSearch(String interactionid, String name, int limit)
			throws ItorixException, JsonProcessingException {
		log("teamSearch", interactionid, "");
		BasicQuery query = new BasicQuery("{\"name\": {$regex : '" + name + "', $options: 'i'}}");
		query.limit(limit > 0 ? limit : 10);
		List<SwaggerTeam> allteams = mongoTemplate.find(query, SwaggerTeam.class);
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode response = mapper.createObjectNode();
		ArrayNode responseFields = mapper.createArrayNode();
		for (SwaggerTeam vo : allteams) {
			SearchItem searchItem = new SearchItem();
			searchItem.setId(vo.getId());
			searchItem.setName(vo.getName());
			responseFields.addPOJO(searchItem);
		}
		response.set("Teams", responseFields);
		return response;
	}

	/**
	 * log
	 * 
	 * @param methodName
	 * @param interactionid
	 * @param body
	 */
	private void log(String methodName, String interactionid, Object... body) {
		logger.debug("CollaborationBusinessImpl." + methodName + " | CorelationId=" + interactionid
				+ " | request/response Body =" + body);
	}

}
