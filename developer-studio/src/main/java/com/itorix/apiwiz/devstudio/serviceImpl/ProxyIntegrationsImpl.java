package com.itorix.apiwiz.devstudio.serviceImpl;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.amazonaws.regions.Regions;
import com.itorix.apiwiz.common.model.integrations.Integration;
import com.itorix.apiwiz.common.model.integrations.apic.ApicIntegration;
import com.itorix.apiwiz.common.model.integrations.git.GitIntegration;
import com.itorix.apiwiz.common.model.integrations.gocd.GoCDIntegration;
import com.itorix.apiwiz.common.model.integrations.jfrog.JfrogIntegration;
import com.itorix.apiwiz.common.model.integrations.s3.S3Integration;
import com.itorix.apiwiz.common.model.integrations.workspace.WorkspaceIntegration;
import com.itorix.apiwiz.common.util.artifatory.JfrogConnection;
import com.itorix.apiwiz.common.util.s3.S3Connection;
import com.itorix.apiwiz.common.util.s3.S3Utils;
import com.itorix.apiwiz.common.util.scm.ScmUtilImpl;
import com.itorix.apiwiz.devstudio.dao.IntegrationsDao;
import com.itorix.apiwiz.devstudio.service.ProxyIntegrations;

@CrossOrigin
@RestController
public class ProxyIntegrationsImpl implements ProxyIntegrations {

	@Autowired
	private IntegrationsDao integrationsDao;

	@Autowired
	private S3Utils s3Utils;

	@Autowired
	private S3Connection s3Connection;
	
	@Autowired
	private JfrogConnection jfrogConnection;
	

	@Value("${server.contextPath}")
	private String context;

	@Override
	public ResponseEntity<?> getGitIntegraton(String interactionid, String jsessionid) throws Exception {
		return new ResponseEntity<>(integrationsDao.getGitIntegration(), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> createupdateGitIntegraton(String interactionid, String jsessionid,
			GitIntegration gitIntegration) throws Exception {
		Integration integration = new Integration();
		integration.setType("GIT");
		integration.setGitIntegration(gitIntegration);
		integrationsDao.updateGITIntegratoin(integration);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<?> removeGitIntegraton(String interactionid, String jsessionid, String id) throws Exception {
		integrationsDao.removeIntegratoin(id);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<?> getJfrogIntegraton(String interactionid, String jsessionid) throws Exception {
		return new ResponseEntity<>(integrationsDao.getJfrogIntegration(), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> updateJfrogIntegraton(String interactionid, String jsessionid,
			JfrogIntegration jfrogIntegration) throws Exception {
		Integration integration = new Integration();
		integration.setType("JFROG");
		integration.setJfrogIntegration(jfrogIntegration);
		integrationsDao.updateJfrogIntegratoin(integration);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<?> removeJfrogIntegraton(String interactionid, String jsessionid, String id)
			throws Exception {
		integrationsDao.removeIntegratoin(id);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<?> getGitLabIntegraton(String interactionid, String jsessionid) throws Exception {
		return new ResponseEntity<>(integrationsDao.getGitLabIntegration(), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> createupdateGitLabIntegraton(String interactionid, String jsessionid,
			GitIntegration gitIntegration) throws Exception {
		Integration integration = new Integration();
		integration.setType("GITLAB");
		integration.setGitIntegration(gitIntegration);
		integrationsDao.updateGITIntegratoin(integration);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<?> removeGitLabIntegraton(String interactionid, String jsessionid, String id)
			throws Exception {
		integrationsDao.removeIntegratoin(id);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<?> getBitBucketIntegraton(String interactionid, String jsessionid) throws Exception {
		return new ResponseEntity<>(integrationsDao.getBitBucketIntegration(), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> createupdateBitBucketIntegraton(String interactionid, String jsessionid,
			GitIntegration gitIntegration) throws Exception {
		Integration integration = new Integration();
		integration.setType("BITBUCKET");
		integration.setGitIntegration(gitIntegration);
		integrationsDao.updateGITIntegratoin(integration);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<?> removeBitBucketIntegraton(String interactionid, String jsessionid, String id)
			throws Exception {
		integrationsDao.removeIntegratoin(id);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<?> getGocdIntegraton(String interactionid, String jsessionid) throws Exception {
		List<Integration> dbIntegrationList = integrationsDao.getIntegration("GOCD");
		Integration integration = new Integration();
		if (dbIntegrationList != null && dbIntegrationList.size() > 0)
			integration = dbIntegrationList.get(0);
		return new ResponseEntity<>(integration, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> updateGocdIntegraton(String interactionid, String jsessionid,
			GoCDIntegration goCDIntegration) throws Exception {
		Integration integration = new Integration();
		integration.setType("GOCD");
		integration.setGoCDIntegration(goCDIntegration);
		String version = integrationsDao.getGoServerVersion(goCDIntegration);
		goCDIntegration.setVersion(version);
		integrationsDao.updateIntegratoin(integration);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<?> removeGocdIntegraton(String interactionid, String jsessionid, String id) throws Exception {
		integrationsDao.removeIntegratoin(id);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<?> getWorkspaceIntegratons(String interactionid, String jsessionid) throws Exception {
		return new ResponseEntity<>(integrationsDao.getWorkspaceIntegration(),HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getWorkspaceIntegratonsKeys(String interactionid, String jsessionid) throws Exception {
		return new ResponseEntity<>(integrationsDao.getMetaData(),HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> createWorkspaceIntegratons(String interactionid, String jsessionid,
			WorkspaceIntegration workspaceIntegration) throws Exception {
		integrationsDao.updateWorkspaceIntegration(workspaceIntegration);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	public ResponseEntity<?> removeWorkspaceIntegratons(
			String interactionid, String jsessionid, String id) throws Exception {
		integrationsDao.removeWorkspaceIntegration(id);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
	
	
	
	@Override
	public ResponseEntity<?> getApicIntegratons(String interactionid, String jsessionid) throws Exception {
		List<Integration> dbIntegrationList = integrationsDao.getIntegration("APIC");
		Integration integration = new Integration();
		if (dbIntegrationList != null && dbIntegrationList.size() > 0)
			integration = dbIntegrationList.get(0);
		return new ResponseEntity<>(integration.getApicIntegration(), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> createApicIntegratons(String interactionid, String jsessionid,
			ApicIntegration apicIntegration) throws Exception {
		Integration integration = new Integration();
		integration.setType("APIC");
		integration.setApicIntegration(apicIntegration);
		integrationsDao.updateApicIntegratoin(integration);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<?> getS3Integratons(String interactionid, String jsessionid) throws Exception {
		return new ResponseEntity<>(integrationsDao.getS3Integration(), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> createS3Integratons(String interactionid, String jsessionid, S3Integration s3Integration)
			throws Exception {
		Integration integration = new Integration();
		integration.setType("S3");
		integration.setS3Integration(s3Integration);
		integrationsDao.updateS3Integratoin(integration);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
	
	@Override
	public ResponseEntity<?> removeS3Integraton( 
			String interactionid, String jsessionid, String id) throws Exception{
		integrationsDao.removeIntegratoin(id);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}


	@Override
	public void downloadFile(
			String interactionid,String jsessionid,
			String type, HttpServletRequest httpServletRequest,HttpServletResponse response) throws Exception{
		String uri = httpServletRequest.getRequestURI();
		uri = uri.replaceAll(context + "/v1/download/", "");
		if(type.equalsIgnoreCase("s3")){
			S3Integration s3Integration = s3Connection.getS3Integration();
			if(s3Integration != null)
			{
				InputStream inputStream = s3Utils.getFile(s3Integration.getKey(), s3Integration.getDecryptedSecret(), 
						Regions.fromName(s3Integration.getRegion()), s3Integration.getBucketName(), 
						uri);
				response.setContentType("application/octet-stream");
				response.setHeader("Content-Disposition", String.format("inline; filename=\"" + uri + "\""));
				FileCopyUtils.copy(inputStream, response.getOutputStream());
			}
		}else{
			Resource resource = jfrogConnection.getArtifact(jfrogConnection.getJfrogIntegration(),uri);
			InputStream inputStream = resource.getInputStream();
			response.setContentType("application/octet-stream");
			response.setHeader("Content-Disposition", String.format("inline; filename=\"" + uri + "\""));
			FileCopyUtils.copy(inputStream, response.getOutputStream());
		}
	}

	@Override
	public ResponseEntity<?> getCodeConnectIntegraton(
			String interactionid, String jsessionid) throws Exception{
		return new ResponseEntity<>(integrationsDao.getCodeconnectIntegration(), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> createupdateCodeconnectIntegraton(
			String interactionid,
			String jsessionid, GitIntegration gitIntegration)
					throws Exception{
		Integration integration = new Integration();
		integration.setType("CODECOMMIT");
		integration.setGitIntegration(gitIntegration);
		integrationsDao.updateGITIntegratoin(integration);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<?> removeCodeconnectIntegraton(
			String interactionid, String jsessionid, String id) throws Exception{
		integrationsDao.removeIntegratoin(id);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
	
	

}
