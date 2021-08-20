package com.itorix.apiwiz.devstudio.serviceImpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import com.itorix.apiwiz.common.model.integrations.Integration;
import com.itorix.apiwiz.common.model.integrations.apic.ApicIntegration;
import com.itorix.apiwiz.common.model.integrations.git.GitIntegration;
import com.itorix.apiwiz.common.model.integrations.gocd.GoCDIntegration;
import com.itorix.apiwiz.common.model.integrations.jfrog.JfrogIntegration;
import com.itorix.apiwiz.common.model.integrations.workspace.WorkspaceIntegration;
import com.itorix.apiwiz.devstudio.dao.IntegrationsDao;
import com.itorix.apiwiz.devstudio.service.ProxyIntegrations;

@CrossOrigin
@RestController
public class ProxyIntegrationsImpl implements ProxyIntegrations {

	@Autowired
	private IntegrationsDao integrationsDao;

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
		return null;
	}

	@Override
	public ResponseEntity<?> createWorkspaceIntegratons(String interactionid, String jsessionid,
			WorkspaceIntegration workspaceIntegration) throws Exception {
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

}
