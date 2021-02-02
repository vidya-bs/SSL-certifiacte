package com.itorix.apiwiz.design.studio.business;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Set;

import javax.mail.MessagingException;

import org.json.JSONException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.design.studio.model.Revision;
import com.itorix.apiwiz.design.studio.model.RowData;
import com.itorix.apiwiz.design.studio.model.Swagger3Comment;
import com.itorix.apiwiz.design.studio.model.Swagger3ReviewComents;
import com.itorix.apiwiz.design.studio.model.Swagger3VO;
import com.itorix.apiwiz.design.studio.model.SwaggerComment;
import com.itorix.apiwiz.design.studio.model.SwaggerHistoryResponse;
import com.itorix.apiwiz.design.studio.model.SwaggerImport;
import com.itorix.apiwiz.design.studio.model.SwaggerIntegrations;
import com.itorix.apiwiz.design.studio.model.SwaggerMetadata;
import com.itorix.apiwiz.design.studio.model.SwaggerReview;
import com.itorix.apiwiz.design.studio.model.SwaggerReviewComents;
import com.itorix.apiwiz.design.studio.model.SwaggerTeam;
import com.itorix.apiwiz.design.studio.model.SwaggerVO;

@Service
public interface SwaggerBusiness {

	/**
	 * createSwagger
	 * 
	 * @param swaggerVO
	 * @return SwaggerVO
	 */
	public SwaggerVO createSwagger(SwaggerVO swaggerVO);

	/**
	 * createSwagger
	 * 
	 * @param swaggerVO
	 * @return SwaggerVO
	 */
	public Swagger3VO createSwagger(Swagger3VO swaggerVO);

	/**
	 * findSwagger
	 * 
	 * @param swaggerVO
	 * @return
	 */
	public SwaggerVO findSwagger(SwaggerVO swaggerVO);

	/**
	 * findSwagger
	 * 
	 * @param swagger3VO
	 * @return
	 */
	public Swagger3VO findSwagger(Swagger3VO swaggerVO);

	public List<SwaggerImport> importSwaggers(MultipartFile zipFile, String type, String gitURI, String branch, String authType, String userName, String password, String personalToken)
			throws Exception;

	/**
	 * findSwagger
	 * 
	 * @param name
	 * @param interactionid
	 * @return
	 */
	public SwaggerVO findSwagger(String name, String interactionid) throws ItorixException;

	/**
	 * findSwagger
	 * 
	 * @param name
	 * @param interactionid
	 * @return
	 */
	public Swagger3VO findSwagger3(String name, String interactionid) throws ItorixException;

	/**
	 * createSwaggerWithNewRevision
	 * 
	 * @param swaggerVO
	 * @param jsessionid
	 * @return
	 */
	public SwaggerVO createSwaggerWithNewRevision(SwaggerVO swaggerVO, String jsessionid) throws ItorixException;

	/**
	 * createSwaggerWithNewRevision
	 * 
	 * @param swaggerVO
	 * @param jsessionid
	 * @return
	 */
	public Swagger3VO createSwaggerWithNewRevision(Swagger3VO swaggerVO, String jsessionid) throws ItorixException;

	/**
	 * updateSwagger
	 * 
	 * @param vo
	 * @return
	 */
	public SwaggerVO updateSwagger(SwaggerVO vo);

	/**
	 * updateSwagger
	 * 
	 * @param Swagger3VO
	 * @return
	 */
	public Swagger3VO updateSwagger(Swagger3VO vo);

	/**
	 * findSwagger
	 * 
	 * @param swaggerVO
	 * @param revision
	 * @return
	 */
	public SwaggerVO findSwagger(SwaggerVO swaggerVO, Integer revision);

	/**
	 * findSwagger
	 * 
	 * @param swagger3VO
	 * @param revision
	 * @return
	 */
	public Swagger3VO findSwagger(Swagger3VO swaggerVO, Integer revision);

	/**
	 * findSwagger
	 * 
	 * @param swaggername
	 * @param revision
	 * @param interactionid
	 * @return
	 */
	public SwaggerVO findSwagger(String swaggername, Integer revision, String interactionid);

	/**
	 * getListOfRevisions
	 * 
	 * @param name
	 * @param interactionid
	 * @return
	 */
	public List<Revision> getListOfRevisions(String name, String interactionid);

	public List<Revision> getListOf3Revisions(String name, String interactionid);

	/**
	 * getListOfSwagger3Revisions
	 * 
	 * @param name
	 * @param interactionid
	 * @return
	 */
	public List<Revision> getListOfSwagger3Revisions(String name, String interactionid);

	/**
	 * getListOfSwaggerNames
	 * 
	 * @param interactionid
	 * @return
	 */
	public List<String> getListOfSwaggerNames(String interactionid) throws ItorixException;

	/**
	 * getListOfSwagger3Names
	 * 
	 * @param interactionid
	 * @return
	 */
	public List<String> getListOfSwagger3Names(String interactionid) throws ItorixException;

	public List<Swagger3VO> getSwagger3Names() throws ItorixException;

	public List<SwaggerVO> getSwaggerNames() throws ItorixException;

	/**
	 * getListOfSwaggerDetails
	 * 
	 * @param interactionid
	 * @param jsessionid
	 * @return
	 * @throws IOException
	 * @throws JsonProcessingException
	 */
	public SwaggerHistoryResponse getListOfSwaggerDetails(String status, String interactionid, String jsessionid,
			int offset, String oas, String swagger, int pageSize) throws ItorixException, JsonProcessingException, IOException;

	/**
	 * getSwaggerCount
	 * 
	 * @param status
	 * @return
	 */
	public int getSwaggerCount(String status);

	/**
	 * getSwagger3Count
	 * 
	 * @param status
	 * @return
	 */
	public int getSwagger3Count(String status);

	/**
	 * getListOfSwagger3Details
	 * 
	 * @param interactionid
	 * @param jsessionid
	 * @return
	 * @throws IOException
	 * @throws JsonProcessingException
	 */
	public SwaggerHistoryResponse getListOfSwagger3Details(String status, String interactionid, String jsessionid,
			int offset, String oas, String swagger, int pageSize) throws ItorixException, JsonProcessingException, IOException;

	/**
	 * getListOfPublishedSwaggerDetails
	 * 
	 * @param interactionid
	 * @param jsessionid
	 * @param status
	 * @return
	 * @throws IOException
	 * @throws JsonProcessingException
	 */

	public ArrayNode getListOfPublishedSwaggerDetails(String interactionid, String jsessionid, String status)
			throws ItorixException, JsonProcessingException, IOException;

	/**
	 * getListOfPublishedSwagger3Details
	 * 
	 * @param interactionid
	 * @param jsessionid
	 * @param status
	 * @return
	 * @throws IOException
	 * @throws JsonProcessingException
	 */
	public ArrayNode getListOfPublishedSwagger3Details(String interactionid, String jsessionid, String status)
			throws ItorixException, JsonProcessingException, IOException;

	/**
	 * getSwagger
	 * 
	 * @param name
	 * @param interactionid
	 * @return
	 */
	public SwaggerVO getSwagger(String name, String interactionid);

	/**
	 * getSwagger3
	 * 
	 * @param name
	 * @param interactionid
	 * @return
	 */
	public Swagger3VO getSwagger3(String name, String interactionid);

	/**
	 * getSwaggerWithVersionNumber
	 * 
	 * @param name
	 * @param revision
	 * @param interactionid
	 * @return
	 */
	public SwaggerVO getSwaggerWithVersionNumber(String name, Integer revision, String interactionid)
			throws ItorixException;

	/**
	 * getSwagger3WithVersionNumber
	 * 
	 * @param name
	 * @param revision
	 * @param interactionid
	 * @return
	 */
	public Swagger3VO getSwagger3WithVersionNumber(String name, Integer revision, String interactionid)
			throws ItorixException;

	/**
	 * 
	 * @param name
	 * @param revision
	 * @param interactionid
	 * @return
	 */
	public List<SwaggerComment> getSwaggerComments(String name, Integer revision, String interactionid);

	/**
	 * getSwagger3Comments
	 * 
	 * @param name
	 * @param revision
	 * @param interactionid
	 * @return
	 */
	public List<Swagger3Comment> getSwagger3Comments(String name, Integer revision, String interactionid);

	/**
	 * getLockStatus
	 * 
	 * @param name
	 * @param revision
	 * @param interactionid
	 * @return
	 */
	public Boolean getLockStatus(String name, Integer revision, String interactionid);

	/**
	 * getSwagger3LockStatus
	 * 
	 * @param name
	 * @param revision
	 * @param interactionid
	 * @return
	 */
	public Boolean getSwagger3LockStatus(String name, Integer revision, String interactionid);

	/**
	 * updateStatus
	 * 
	 * @param name
	 * @param revision
	 * @param json
	 * @param interactionid
	 * @param jsessionid
	 * @return
	 * @throws MessagingException
	 * @throws JSONException
	 */
	public SwaggerVO updateStatus(String name, Integer revision, String json, String interactionid, String jsessionid)
			throws MessagingException, JSONException, ItorixException;

	public List<String> getSwaggerRoles(String name, String oas, String interactionid, String jsessionid)
			throws ItorixException;

	/**
	 * updateSwagger3Status
	 * 
	 * @param name
	 * @param revision
	 * @param json
	 * @param interactionid
	 * @param jsessionid
	 * @return
	 * @throws MessagingException
	 * @throws JSONException
	 */
	public Swagger3VO updateSwagger3Status(String name, Integer revision, String json, String interactionid,
			String jsessionid) throws MessagingException, JSONException, ItorixException;

	/**
	 * updateComment
	 * 
	 * @param comment
	 */
	public void updateComment(SwaggerComment comment);

	/**
	 * updateSwagger3Comment
	 * 
	 * @param comment
	 */
	public void updateSwagger3Comment(Swagger3Comment comment);

	/**
	 * updateLockStatus
	 * 
	 * @param swaggerVO
	 */
	public void updateLockStatus(SwaggerVO swaggerVO, String jsessionid);

	/**
	 * updateSwagger3LockStatus
	 * 
	 * @param swagger3VO
	 */
	public void updateSwagger3LockStatus(Swagger3VO swaggerVO, String jsessionid);

	/**
	 * deprecate
	 * 
	 * @param swaggerVO
	 * @return
	 */
	public SwaggerVO deprecate(SwaggerVO swaggerVO);

	/**
	 * deprecate
	 * 
	 * @param Swagger3VO
	 * @return
	 */
	public Swagger3VO deprecate(Swagger3VO swaggerVO);

	/**
	 * updateProxies
	 * 
	 * @param swaggerVO
	 */
	public void updateProxies(SwaggerVO swaggerVO);

	/**
	 * genarateXpath
	 * 
	 * @param xsdFile
	 * @param elementName
	 * @param interactionid
	 * @return
	 * @throws Exception
	 */
	public String genarateXpath(MultipartFile xsdFile, String elementName, String interactionid) throws Exception;

	/**
	 * genarateSwaggerDefinations
	 * 
	 * @param swaggerVO
	 * @param xpathFile
	 * @param sheetName
	 * @param revision
	 * @return
	 * @throws Exception
	 */
	public SwaggerVO genarateSwaggerDefinations(SwaggerVO swaggerVO, MultipartFile xpathFile, String sheetName,
			Integer revision) throws Exception;

	/**
	 * genarateSwaggerJsonDefinations
	 * 
	 * @param swaggerVO
	 * @param rowDataList
	 * @param revision
	 * @return
	 * @throws Exception
	 */
	public SwaggerVO genarateSwaggerJsonDefinations(SwaggerVO swaggerVO, List<RowData> rowDataList, Integer revision)
			throws Exception;

	/**
	 * genarateSwaggerJsonDefinations
	 * 
	 * @param swaggerVO
	 * @param rowDataList
	 * @param revision
	 * @return
	 * @throws Exception
	 */
	public Swagger3VO genarateSwaggerJsonDefinations(Swagger3VO swaggerVO, List<RowData> rowDataList, Integer revision)
			throws Exception;

	/**
	 * createReview
	 * 
	 * @param swaggerReview
	 * @throws MessagingException
	 */
	public void createReview(SwaggerReview swaggerReview) throws MessagingException;

	/**
	 * createOrUpdateReviewComment
	 * 
	 * @param swaggerReviewComents
	 */
	public void createOrUpdateReviewComment(SwaggerReviewComents swaggerReviewComents) throws ItorixException;

	public void createOrUpdateReviewComment(Swagger3ReviewComents swaggerReviewComents) throws ItorixException;

	/**
	 * updateReviewComment
	 * 
	 * @param swaggerReviewComents
	 * @throws Exception
	 */
	public void updateReviewComment(SwaggerReviewComents swaggerReviewComents) throws Exception;

	public void updateReviewComment(Swagger3ReviewComents swaggerReviewComents) throws Exception;

	/**
	 * getReviewComment
	 * 
	 * @param swaggerReviewComents
	 * @return
	 * @throws MessagingException
	 */
	public ObjectNode getReviewComment(SwaggerReviewComents swaggerReviewComents)
			throws MessagingException, ItorixException;

	public ObjectNode getReviewComment(Swagger3ReviewComents swaggerReviewComents)
			throws MessagingException, ItorixException;

	public void addReplayNodes(ObjectMapper mapper, SwaggerReviewComents sc, ArrayNode replayNode);

	/**
	 * deleteSwagger
	 * 
	 * @param name
	 * @param interactionid
	 */
	public void deleteSwagger(String name, String interactionid);

	/**
	 * deleteSwagger3
	 * 
	 * @param name
	 * @param interactionid
	 */
	public void deleteSwagger3(String name, String interactionid);

	/**
	 * deleteSwaggerVersion
	 * 
	 * @param name
	 * @param revision
	 * @param interactionid
	 */
	public void deleteSwaggerVersion(String name, Integer revision, String interactionid);

	/**
	 * deleteSwagger3Version
	 * 
	 * @param name
	 * @param revision
	 * @param interactionid
	 */
	public void deleteSwagger3Version(String name, Integer revision, String interactionid);

	/**
	 * getSwaggerStats
	 * 
	 * @param timeunit
	 * @param timerange
	 * @return
	 * @throws ParseException
	 */
	public ObjectNode getSwaggerStats(String timeunit, String timerange) throws ParseException, ItorixException;

	/**
	 * getSwagger3Stats
	 * 
	 * @param timeunit
	 * @param timerange
	 * @return
	 * @throws ParseException
	 */
	public ObjectNode getSwagger3Stats(String timeunit, String timerange) throws ParseException, ItorixException;

	/**
	 * getTeamStats
	 * 
	 * @param timeunit
	 * @param timerange
	 * @return
	 * @throws ParseException
	 */
	public ObjectNode getTeamStats(String timeunit, String timerange) throws ParseException, ItorixException;

	/**
	 * associateTeam
	 * 
	 * @param swaggerName
	 * @param productSet
	 * @param interactionId
	 * @throws ItorixException
	 */
	public void associateProduct(String swaggerName, Set<String> productSet, String interactionId)
			throws ItorixException;

	public SwaggerMetadata getSwaggerMetadata(String swaggerName, String oas) throws ItorixException;

	/**
	 * associateTeam
	 * 
	 * @param swaggerName
	 * @param productSet
	 * @param interactionId
	 * @throws ItorixException
	 */
	public void assoiateTeamsToProject(String team_name, Set<String> projectSet, String interactionId)
			throws ItorixException;

	/**
	 * associatePortfolio
	 * 
	 * @param swaggerName
	 * @param productSet
	 * @param interactionId
	 * @throws ItorixException
	 */
	public void associatePortfolio(String swaggerName, Set<String> portfolioSet, String interactionId)
			throws ItorixException;

	/**
	 * findSwagger
	 * 
	 * @param name
	 * @param interactionid
	 * @return
	 */
	public SwaggerTeam findSwaggerTeam(String team_name, String interactionid) throws ItorixException;

	/**
	 * swaggerSearch
	 * 
	 * @param interactionid
	 * @param name
	 * @param limit
	 * @return
	 * @throws ItorixException
	 */
	public Object swaggerSearch(String interactionid, String name, int limit)
			throws ItorixException, JsonProcessingException;

	/**
	 * swaggerSearch
	 * 
	 * @param interactionid
	 * @param name
	 * @param limit
	 * @return
	 * @throws ItorixException
	 */
	public Object swagger3Search(String interactionid, String name, int limit)
			throws ItorixException, JsonProcessingException;

	public Object getSwagger2BasePathsObj();

	public void createOrUpdateGitIntegrations(String interactionid, String jsessionid, String swaggerid, String oas, 
			SwaggerIntegrations swaggerIntegrations) throws ItorixException;

	public SwaggerIntegrations getGitIntegrations(String interactionid, String jsessionid, String swaggerid, String oas)
			throws ItorixException;

	public void deleteGitIntegrations(String interactionid, String jsessionid, String swaggerid, String oas) throws ItorixException;

}