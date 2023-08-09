package com.itorix.apiwiz.collaboration.business;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.itorix.apiwiz.collaboration.model.Swagger3VO;
import com.itorix.apiwiz.collaboration.model.SwaggerMetadata;
import com.itorix.apiwiz.collaboration.model.SwaggerTeam;
import com.itorix.apiwiz.collaboration.model.SwaggerVO;
import com.itorix.apiwiz.collaboration.model.TeamsHistoryResponse;
import com.itorix.apiwiz.common.model.exception.ItorixException;

@Service
public interface CollaborationBusiness {
	/**
	 * findSwaggerTeam
	 *
	 * @param swaggerTeam
	 * 
	 * @return
	 */
	public SwaggerTeam findSwaggerTeam(SwaggerTeam swaggerTeam);

	/**
	 * createTeam
	 *
	 * @param swaggerTeam
	 * 
	 * @throws ItorixException
	 */
	public void createTeam(SwaggerTeam swaggerTeam) throws ItorixException;

	/**
	 * updateTeam
	 *
	 * @param swaggerTeam
	 * @param name
	 * 
	 * @throws ItorixException
	 */
	public void updateTeam(SwaggerTeam swaggerTeam, String name) throws ItorixException;

	public SwaggerTeam getTeam(String teamName, String interactionid) throws ItorixException;

	/**
	 * deleteTeam
	 *
	 * @param teamName
	 * @param interactionid
	 * @param jsessionid
	 * 
	 * @throws ItorixException
	 */
	public void deleteTeam(String teamName, String interactionid, String jsessionid) throws ItorixException;

	/**
	 * associateTeam
	 *
	 * @param swaggerName
	 * @param teamSet
	 * @param interactionId
	 * 
	 * @throws ItorixException
	 */
	public void associateTeam(String swaggerName, Set<String> teamSet, String interactionId, String oas)
			throws ItorixException;

	public SwaggerMetadata getSwaggerMetadata(String name, String oas);

	/**
	 * findSwaggerTeames
	 *
	 * @param jsessionid
	 * @param interactionid
	 * 
	 * @return
	 */
	public Map<String, Object> findSwaggerTeams(String jsessionid, String interactionid, int offset, int pageSize,
												String name, boolean paginated);

	/**
	 * findSwaggerTeameNames
	 *
	 * @param jsessionid
	 * @param interactionid
	 * 
	 * @return
	 */
	public List<String> findSwaggerTeameNames(String jsessionid, String interactionid);

	/**
	 * getTeamPermissions
	 *
	 * @param interactionid
	 * @param jsessionid
	 * 
	 * @return
	 * 
	 * @throws JsonProcessingException
	 */
	public String getTeamPermissions(String interactionid, String jsessionid)
			throws JsonProcessingException, ItorixException;

	/**
	 * findSwagger
	 *
	 * @param name
	 * @param interactionid
	 * 
	 * @return
	 */
	public SwaggerVO findSwagger(String name, String interactionid) throws ItorixException;

	public Swagger3VO findSwagger3(String name, String interactionid) throws ItorixException;

	/**
	 * teamSearch
	 *
	 * @param interactionid
	 * @param name
	 * @param limit
	 * 
	 * @return
	 * 
	 * @throws ItorixException
	 * @throws JsonProcessingException
	 */
	public Object teamSearch(String interactionid, String name, int limit)
			throws ItorixException, JsonProcessingException;
}
