package com.itorix.apiwiz.cicd.serviceimpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.itorix.apiwiz.cicd.beans.BackUpInterval;
import com.itorix.apiwiz.cicd.beans.BackUpRequest;
import com.itorix.apiwiz.cicd.beans.Pipeline;
import com.itorix.apiwiz.cicd.beans.PipelineGroups;
import com.itorix.apiwiz.cicd.beans.PipelineNameValidation;
import com.itorix.apiwiz.cicd.beans.Stage;
import com.itorix.apiwiz.cicd.dao.PipelineDao;
import com.itorix.apiwiz.cicd.gocd.integrations.CiCdIntegrationAPI;
import com.itorix.apiwiz.cicd.service.ManagePipelineService;
import com.itorix.apiwiz.common.model.exception.ErrorObj;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.identitymanagement.dao.IdentityManagementDao;
import com.itorix.apiwiz.identitymanagement.security.annotation.UnSecure;

/**
 * CI/CD Services implementation
 *
 * @author vphani
 */
@CrossOrigin
@RestController
@SuppressWarnings({"rawtypes", "unchecked"})
public class ManagePipelineServiceImpl implements ManagePipelineService {

	@Autowired
	private PipelineDao pipelineDao;

	@Autowired
	private CiCdIntegrationAPI cicdIntegrationApi;

	@Autowired
	private IdentityManagementDao commonServices;

	private static final Logger log = LoggerFactory.getLogger(ManagePipelineServiceImpl.class);

	@RequestMapping(method = RequestMethod.POST, value = "/v1/pipelines/validate", produces = {"application/json"})
	public ResponseEntity<?> validatePipelineName(@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody PipelineNameValidation pipelineName) {
		Map response = new HashMap();
		response.put("isValid", pipelineDao.validatePipelineName(pipelineName));
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> validateGroup(@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@PathVariable("groupName") String groupName) {
		Map response = new HashMap();
		response.put("isValid", pipelineDao.validateGroup(groupName));
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> createPipeline(@RequestBody PipelineGroups pipelineGroups,
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			HttpServletRequest request) {
		log.debug("ConfigManagementController.addTarget : CorelationId= " + interactionid + " : " + "jsessionid="
				+ jsessionId + ": requestUrl " + request.getRequestURI());
		return managePipeline(pipelineGroups, jsessionId, interactionid, true, HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<?> updatePipeline(@RequestBody PipelineGroups pipelineGroups,
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			HttpServletRequest request) {
		log.debug("ConfigManagementController.addTarget : CorelationId= " + interactionid + " : " + "jsessionid="
				+ jsessionId + ": requestUrl " + request.getRequestURI());
		return managePipeline(pipelineGroups, jsessionId, interactionid, false, HttpStatus.NO_CONTENT);
	}

	public ResponseEntity<?> managePipeline(PipelineGroups pipelineGroups, String jsessionId, String interactionid,
			boolean isNew, HttpStatus status) {
		if (pipelineGroups == null || !isValidPipeline(pipelineGroups)) {
			return new ResponseEntity<>(new ErrorObj("Invalid pipeline Data for Create/Edit Pipeline", "CI-CD-CU400"),
					HttpStatus.BAD_REQUEST);
		}
		try {
			log.debug("Creating/updating pipeline: " + pipelineGroups);
			if (isNew) {
				Pipeline pipeline = pipelineDao.getPipeline(pipelineDao.getPipelineName(pipelineGroups));
				if (pipeline != null) {
					return new ResponseEntity<>(new ErrorObj("Pipeline already exists", "CI-CD-CU400"),
							HttpStatus.BAD_REQUEST);
				}
			}
			cicdIntegrationApi.createOrEditPipeline(pipelineGroups, isNew);
		} catch (Exception ex) {
			ex.printStackTrace();
			log.error("Error while creating/updating pipeline", ex.getCause());
			return new ResponseEntity<>(new ErrorObj("Error while creating/updating pipeline", "CI-CD-CU500"),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		// Save Pipeline Details in DB
		String pipelineName = pipelineDao.getPipelineName(pipelineGroups);
		String displayName = pipelineGroups.getPipelines().get(0).getDisplayName();
		pipelineGroups.getPipelines().get(0).setStatus("Active");
		pipelineGroups = pipelineDao.createOrEditPipeline(pipelineGroups,
				commonServices.getUserDetailsFromSessionID(jsessionId));
		HttpHeaders headers = new HttpHeaders();
		headers.add("x-displayname", displayName);
		headers.add("x-pipelinename", pipelineName);
		headers.add("Access-Control-Expose-Headers", "x-displayname, x-pipelinename");
		return new ResponseEntity<>(headers, status);
	}

	@Override
	public ResponseEntity<?> deletePipelineGroup(@PathVariable("pipelineGroupName") String groupName,
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			HttpServletRequest request) {
		log.debug("ConfigManagementController.addTarget : CorelationId= " + interactionid + " : " + "jsessionid="
				+ jsessionId + ": requestUrl " + request.getRequestURI());
		if (StringUtils.isEmpty(groupName)) {
			return new ResponseEntity<>(new ErrorObj("Invalid pipeline Group", ""), HttpStatus.BAD_REQUEST);
		}
		try {
			pipelineDao.deletePipelineGroup(groupName);
		} catch (Exception ex) {
			log.error("Error while deleting pipeline", ex.getCause());
			return new ResponseEntity<>(new ErrorObj("Error while deleting pipeline", "CI-CD-DG500"),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<?> deletePipeline(@PathVariable("pipelineGroupName") String groupName,
			@PathVariable("pipelineName") String name, @RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			HttpServletRequest request) {
		log.debug("ConfigManagementController.addTarget : CorelationId= " + interactionid + " : " + "jsessionid="
				+ jsessionId + ": requestUrl " + request.getRequestURI());
		if (StringUtils.isEmpty(groupName) || StringUtils.isEmpty(name)) {
			return new ResponseEntity<>(new ErrorObj("Invalid pipeline", "CI-CD-D500"), HttpStatus.BAD_REQUEST);
		}
		try {
			cicdIntegrationApi.deletePipeline(name);
		} catch (Exception ex) {
			log.error("Error while deleting pipeline.", ex.getCause());
			return new ResponseEntity<>(new ErrorObj("Error while deleting pipeline. Please try after sometime.", ""),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		try {
			pipelineDao.deletePipeline(name);
		} catch (Exception ex) {
			log.error("Error while deleting pipeline", ex.getCause());
			return new ResponseEntity<>(new ErrorObj("Error while deleting pipeline", "CI-CD-DP500"),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<?> getAllPipelines(@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid, HttpServletRequest request)
			throws ItorixException {
		List<PipelineGroups> projects = null;
		try {
			projects = pipelineDao.getAvailablePipelines();
		} catch (Exception ex) {
			log.error("Error while retrieving pipeline information", ex.getCause());
			throw new ItorixException("Error while retrieving pipeline information", "CI-CD-GA500");
		}
		return new ResponseEntity<>(projects, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getPipelinesFromGroup(@PathVariable("pipelineGroupName") String groupName,
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			HttpServletRequest request) {
		log.debug("ConfigManagementController.addTarget : CorelationId= " + interactionid + " : " + "jsessionid="
				+ jsessionId + ": requestUrl " + request.getRequestURI());
		if (StringUtils.isEmpty(groupName)) {
			return new ResponseEntity<>(new ErrorObj("Invalid pipeline Group Name", "CI-CD-GG400"),
					HttpStatus.BAD_REQUEST);
		}
		PipelineGroups pipelineGroups = null;
		try {
			pipelineGroups = pipelineDao.getPipelineGroups(groupName);
		} catch (Exception ex) {
			log.error("Error while retrieving pipeline information", ex.getCause());
			return new ResponseEntity<>(new ErrorObj("Error while retrieving pipeline information", "CI-CD-GG500"),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(pipelineGroups, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getPipeline(@PathVariable("pipelineGroupName") String groupName,
			@PathVariable("pipelineName") String name, @RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			HttpServletRequest request) {
		log.debug("ConfigManagementController.addTarget : CorelationId= " + interactionid + " : " + "jsessionid="
				+ jsessionId + ": requestUrl " + request.getRequestURI());
		if (StringUtils.isEmpty(name)) {
			return new ResponseEntity<>(new ErrorObj("Invalid pipeline Name", "CI-CD-GP400"), HttpStatus.BAD_REQUEST);
		}
		Pipeline pipeline = null;
		try {
			pipeline = pipelineDao.getPipeline(name);
		} catch (Exception ex) {
			log.error("Error while retrieving pipeline information", ex.getCause());
			return new ResponseEntity<>(new ErrorObj("Error while retrieving pipeline information", "CI-CD-GP500"),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(pipeline, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getPipelineHistory(@PathVariable("pipelineGroupName") String groupName,
			@PathVariable("pipelineName") String name,
			@RequestParam(value = "offset", required = false, defaultValue = "1") String offset,
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid, HttpServletRequest request)
			throws ParseException {
		log.debug("ConfigManagementController.addTarget : CorelationId= " + interactionid + " : " + "jsessionid="
				+ jsessionId + ": requestUrl " + request.getRequestURI());
		if (StringUtils.isEmpty(groupName) || StringUtils.isEmpty(name)) {
			return new ResponseEntity<>(new ErrorObj("Invalid Project Name Or Pipeline Name", "CI-CD-GPH400"),
					HttpStatus.BAD_REQUEST);
		}
		String response = null;
		Pipeline pipeline = pipelineDao.getPipeline(name);

		try {
			response = cicdIntegrationApi.getPipelineHistory(groupName, name, offset);
		} catch (Exception ex) {
			log.error("Error while retrieving pipeline history", ex.getCause());
			return new ResponseEntity<>(new ErrorObj("Error while retrieving pipeline history", "CI-CD-GPH500"),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		HttpHeaders headers = new HttpHeaders();
		headers.add("x-displayname", pipeline.getDisplayName());
		headers.add("Access-Control-Expose-Headers", "x-displayname");
		return new ResponseEntity<>(new JSONParser().parse(response), headers, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getBuildAndTestArtifacts(@PathVariable("pipelineGroupName") String groupName,
			@PathVariable("pipelineName") String pipelineName, @PathVariable("pipelineCounter") String pipelineCounter,
			@PathVariable("stageName") String stageName, @PathVariable("stageCounter") String stageCounter,
			@PathVariable("jobName") String jobName, @RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid, HttpServletRequest request)
			throws ParseException {
		log.debug("ConfigManagementController.addTarget : CorelationId= " + interactionid + " : " + "jsessionid="
				+ jsessionId + ": requestUrl " + request.getRequestURI());
		if (StringUtils.isEmpty(groupName) || StringUtils.isEmpty(pipelineName) || StringUtils.isEmpty(pipelineCounter)
				|| StringUtils.isEmpty(stageName) || StringUtils.isEmpty(stageCounter)
				|| StringUtils.isEmpty(jobName)) {
			return new ResponseEntity<>("Invalid Request", HttpStatus.BAD_REQUEST);
		}
		String response = null;
		try {
			response = cicdIntegrationApi.getArtifactDetails(groupName, pipelineName, pipelineCounter, stageName,
					stageCounter, jobName);
		} catch (Exception ex) {
			log.error("Error while retrieving build and test artifacts", ex.getCause());
			return new ResponseEntity<>(HttpStatus.OK);
		}
		return new ResponseEntity<>(new JSONParser().parse(response), HttpStatus.OK);
	}

	@Override
	@UnSecure(ignoreValidation = true)
	public ResponseEntity<Resource> getArtifacts(@PathVariable("pipelineName") String pipelineName,
			@PathVariable("pipelineCounter") String pipelineCounter, @PathVariable("stageName") String stageName,
			@PathVariable("stageCounter") String stageCounter, @PathVariable("artifactName") String artifactName,
			@RequestHeader(value = "JSESSIONID", required = false) String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid, HttpServletRequest request)
			throws ParseException {
		log.debug("ConfigManagementController.addTarget : CorelationId= " + interactionid + " : " + "jsessionid="
				+ jsessionId + ": requestUrl " + request.getRequestURI());
		if (StringUtils.isEmpty(pipelineName) || StringUtils.isEmpty(pipelineCounter) || StringUtils.isEmpty(stageName)
				|| StringUtils.isEmpty(stageCounter) || StringUtils.isEmpty(artifactName)) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		Resource response = null;
		try {
			response = cicdIntegrationApi.getArtifacts(pipelineName, pipelineCounter, stageName, stageCounter,
					artifactName);
		} catch (Exception ex) {
			log.error("Error while retrieving build and test artifacts", ex.getCause());
			return new ResponseEntity<>(HttpStatus.OK);
		}
		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + artifactName)
				.contentType(MediaType.parseMediaType("application/octet-stream")).body(response);
	}

	@UnSecure
	@Override
	public ResponseEntity<String> getConsoleLogs(@PathVariable("pipelineName") String pipelineName,
			@PathVariable("pipelineCounter") String pipelineCounter, @PathVariable("stageName") String stageName,
			@PathVariable("stageCounter") String stageCounter,
			@RequestHeader(value = "JSESSIONID", required = false) String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			HttpServletRequest request) {

		log.debug("ConfigManagementController.addTarget : CorelationId= " + interactionid + " : " + "jsessionid="
				+ jsessionId + ": requestUrl " + request.getRequestURI());
		if (StringUtils.isEmpty(pipelineName) || StringUtils.isEmpty(pipelineCounter) || StringUtils.isEmpty(stageName)
				|| StringUtils.isEmpty(stageCounter)) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		String response = null;
		try {
			response = cicdIntegrationApi.getRuntimeLogs("", pipelineName, pipelineCounter, stageName, stageCounter,
					"BuildAndDeploy");
		} catch (Exception ex) {
			log.error("Error while retrieving build and test artifacts", ex.getCause());
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> triggerPipeline(@PathVariable("pipelineGroupName") String groupName,
			@PathVariable("pipelineName") String pipelineName, @RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			HttpServletRequest request) {
		log.debug("ConfigManagementController.addTarget : CorelationId= " + interactionid + " : " + "jsessionid="
				+ jsessionId + ": requestUrl " + request.getRequestURI());
		if (StringUtils.isEmpty(groupName) || StringUtils.isEmpty(pipelineName)) {
			return new ResponseEntity<>(new ErrorObj("Invalid Request", ""), HttpStatus.BAD_REQUEST);
		}
		try {
			cicdIntegrationApi.triggerPipeline(groupName, pipelineName);
		} catch (Exception ex) {
			log.error("Error while triggering pipeline", ex.getCause());
			return new ResponseEntity<>(new ErrorObj("Error while triggering pipeline", "CI-CD-GTP500"),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(HttpStatus.ACCEPTED);
	}

	@Override
	public ResponseEntity<?> cancelPipeline(@PathVariable("pipelineGroupName") String groupName,
			@PathVariable("pipelineName") String pipelineName, @PathVariable("stageName") String stageName,
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			HttpServletRequest request) {
		log.debug("ConfigManagementController.addTarget : CorelationId= " + interactionid + " : " + "jsessionid="
				+ jsessionId + ": requestUrl " + request.getRequestURI());
		if (StringUtils.isEmpty(groupName) || StringUtils.isEmpty(pipelineName)) {
			return new ResponseEntity<>(new ErrorObj("Invalid Request", ""), HttpStatus.BAD_REQUEST);
		}
		try {
			cicdIntegrationApi.cancelPipeline(groupName, pipelineName, stageName);
		} catch (Exception ex) {
			log.error("Error while retrieving pipeline information", ex.getCause());
			return new ResponseEntity<>(new ErrorObj("Error while retrieving pipeline information", "CI-CD-GTP500"),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(HttpStatus.ACCEPTED);
	}

	@Override
	public ResponseEntity<?> triggerStage(@PathVariable("pipelineGroupName") String groupName,
			@PathVariable("pipelineName") String pipelineName, @PathVariable("counter") String counter,
			@PathVariable("stageName") String stageName, @RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			HttpServletRequest request) {
		log.debug("ConfigManagementController.addTarget : CorelationId= " + interactionid + " : " + "jsessionid="
				+ jsessionId + ": requestUrl " + request.getRequestURI());
		if (StringUtils.isEmpty(groupName) || StringUtils.isEmpty(pipelineName) || StringUtils.isEmpty(stageName)) {
			return new ResponseEntity<>(new ErrorObj("Invalid Request", ""), HttpStatus.BAD_REQUEST);
		}
		try {
			cicdIntegrationApi.triggerStage(groupName, pipelineName, stageName, counter);
		} catch (Exception ex) {
			log.error("Error while retrieving pipeline information", ex.getCause());
			return new ResponseEntity<>(new ErrorObj("Error while retrieving pipeline information", "CI-CD-GTS500"),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(HttpStatus.ACCEPTED);
	}

	private boolean isValidPipeline(PipelineGroups pipelineGroups) {
		if (pipelineGroups == null || pipelineGroups.getPipelines() == null || pipelineGroups.getPipelines().isEmpty()
				|| pipelineGroups.getPipelines().get(0).getStages() == null
				|| pipelineGroups.getPipelines().get(0).getStages().isEmpty()
				|| isValidStage(pipelineGroups.getPipelines().get(0).getStages())) {
			return false;
		}
		return true;
	}

	private boolean isValidStage(List<Stage> stages) {
		if (stages != null && stages.isEmpty()) {
			for (Stage stage : stages) {
				if (stage == null || stage.getName() == null || stage.getEnvName() == null || stage.getOrgName() == null
						|| stage.getType() == null) {
					return false;
				}
			}
		}
		return false;
	}

	@UnSecure
	@Override
	public ResponseEntity<?> sendNotifications(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody String emailBody, @RequestParam("pipelineName") String pipelineName,
			@RequestParam(value = "projectName", required = false) String projectName,
			@RequestParam("subject") String subject) throws Exception {
		cicdIntegrationApi.sendNotification(emailBody, pipelineName, projectName, subject, interactionid);
		return new ResponseEntity(HttpStatus.OK);
	}

	@UnSecure
	public ResponseEntity<?> getLogs(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam("url") String url) throws Exception {
		if (url == null || url.isEmpty()) {
			return new ResponseEntity<>(new ErrorObj("Invalid Request or bad URL Parameter", ""),
					HttpStatus.BAD_REQUEST);
		}

		String logUrl = url + "?startLineNumber=0";
		return new ResponseEntity<>(cicdIntegrationApi.getLogs(logUrl), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getMetricsForProject(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam("topk") String topk, @PathVariable("pipelineGroupName") String pipelineGroupName,
			@PathVariable("pipelineName") String pipelineName) throws Exception {
		return new ResponseEntity(
				cicdIntegrationApi.getMetricsForProject(interactionid, topk, pipelineGroupName, pipelineName),
				HttpStatus.OK);
	}

	@Override
	@UnSecure
	public ResponseEntity<?> getPipelineDashBoard(
			@RequestHeader(value = "interactionid", required = false) String interactionid) throws Exception {
		return new ResponseEntity(cicdIntegrationApi.getPipelineDashBoard(), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getRunTimeLogs(@PathVariable("pipelineGroupName") String groupName,
			@PathVariable("pipelineName") String pipelineName, @PathVariable("pipelineCounter") String pipelineCounter,
			@PathVariable("stageName") String stageName, @PathVariable("stageCounter") String stageCounter,
			@PathVariable("jobName") String jobName, @RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid) throws Exception {
		if (StringUtils.isEmpty(groupName) || StringUtils.isEmpty(pipelineName) || StringUtils.isEmpty(pipelineCounter)
				|| StringUtils.isEmpty(stageName) || StringUtils.isEmpty(stageCounter)
				|| StringUtils.isEmpty(jobName)) {
			return new ResponseEntity<>("Invalid Request", HttpStatus.BAD_REQUEST);
		}
		String responseLogs = null;
		try {
			responseLogs = cicdIntegrationApi.getRuntimeLogs(groupName, pipelineName, pipelineCounter, stageName,
					stageCounter, jobName);
		} catch (Exception ex) {
			log.error("Error while retrieving pipeline information", ex.getCause());
			return new ResponseEntity<>(new ErrorObj("Error while retrieving pipeline information", "CI-CD-GBTA500"),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(responseLogs, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> pausePipeline(@PathVariable("pipelineGroupName") String groupName,
			@PathVariable("pipelineName") String pipelineName, @RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			HttpServletRequest request) {
		log.debug("ConfigManagementController.addTarget : CorelationId= " + interactionid + " : " + "jsessionid="
				+ jsessionId + ": requestUrl " + request.getRequestURI());
		if (StringUtils.isEmpty(groupName) || StringUtils.isEmpty(pipelineName)) {
			return new ResponseEntity<>(new ErrorObj("Invalid pipeline Group or Pipeline Name", "CI-CD-D500"),
					HttpStatus.BAD_REQUEST);
		}
		try {
			cicdIntegrationApi.pausePipeline(pipelineName);
		} catch (Exception ex) {
			log.error("Error while pausing pipeline. Please try after sometime.", ex.getCause());
			return new ResponseEntity<>(new ErrorObj("Error while pausing pipeline. Please try after sometime.", ""),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		pipelineDao.updatePipelineStatus(groupName, pipelineName, "Paused",
				commonServices.getUserDetailsFromSessionID(jsessionId));
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<?> unpausePipeline(@PathVariable("pipelineGroupName") String groupName,
			@PathVariable("pipelineName") String pipelineName, @RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			HttpServletRequest request) {

		log.debug("ConfigManagementController.addTarget : CorelationId= " + interactionid + " : " + "jsessionid="
				+ jsessionId + ": requestUrl " + request.getRequestURI());
		if (StringUtils.isEmpty(groupName) || StringUtils.isEmpty(pipelineName)) {
			return new ResponseEntity<>(new ErrorObj("Invalid pipeline Group or Pipeline Name", "CI-CD-D500"),
					HttpStatus.BAD_REQUEST);
		}
		try {
			cicdIntegrationApi.unPausePipeline(pipelineName);
		} catch (Exception ex) {
			log.error("Error while unpausing pipeline. Please try after sometime.", ex.getCause());
			return new ResponseEntity<>(new ErrorObj("Error while unpausing pipeline. Please try after sometime.", ""),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		pipelineDao.updatePipelineStatus(groupName, pipelineName, "Active",
				commonServices.getUserDetailsFromSessionID(jsessionId));
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<?> getPipelineStatus(@PathVariable("pipelineGroupName") String groupName,
			@PathVariable("pipelineName") String pipelineName, @RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid, HttpServletRequest request)
			throws ParseException {
		String response = null;
		log.debug("ConfigManagementController.addTarget : CorelationId= " + interactionid + " : " + "jsessionid="
				+ jsessionId + ": requestUrl " + request.getRequestURI());
		if (StringUtils.isEmpty(groupName) || StringUtils.isEmpty(pipelineName)) {
			return new ResponseEntity<>(new ErrorObj("Invalid pipeline Group or Pipeline Name", "CI-CD-D500"),
					HttpStatus.BAD_REQUEST);
		}
		try {
			response = cicdIntegrationApi.getPipelineStatus(pipelineName);
		} catch (Exception ex) {
			log.error("Error while retrieving pipeline status. Pipeline might not be available", ex.getCause());
			return new ResponseEntity<>(
					new ErrorObj("Error while retrieving pipeline status. Please check the pipeline in input url", ""),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(new JSONParser().parse(response), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getcicdStats(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(value = "timeunit", required = false) String timeunit,
			@RequestParam(value = "timerange", required = false) String timerange) throws Exception {
		return new ResponseEntity(cicdIntegrationApi.getcicdtats(timeunit, timerange), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> createcicdBackUp(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestBody BackUpRequest backUpRequest)
			throws Exception {
		if (BackUpInterval.isBackUpIntervalValid(backUpRequest.getInterval())) {
			cicdIntegrationApi.createcicdBackUp(backUpRequest);
		} else {
			throw new ItorixException(new Throwable().getMessage(), "CICD_006", new Throwable());
		}
		return new ResponseEntity(HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<?> updatecicdBackUp(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid, @RequestBody BackUpRequest backUpRequest)
			throws Exception {
		cicdIntegrationApi.updatecicdBackUp(backUpRequest);
		return new ResponseEntity(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<?> getcicdBackUp(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {
		return new ResponseEntity(cicdIntegrationApi.getcicdBackUpDetails(), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getcicdBackUpHistory(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {
		return new ResponseEntity(cicdIntegrationApi.getcicdBackUpHistory(), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getGoCdHealth(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {
		return new ResponseEntity(cicdIntegrationApi.getGoCdHealth(), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getrelaseStats(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(value = "timeunit", required = false) String timeunit,
			@RequestParam(value = "timerange", required = false) String timerange) throws Exception {
		return new ResponseEntity(cicdIntegrationApi.getreleaseStats(timeunit, timerange), HttpStatus.OK);
	}
}
