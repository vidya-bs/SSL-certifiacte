package com.itorix.apiwiz.cicd.serviceimpl;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.itorix.apiwiz.cicd.dao.SoapArtifiactServiceDao;
import com.itorix.apiwiz.cicd.service.SoapArtifactService;
import com.itorix.apiwiz.common.model.exception.ItorixException;

@CrossOrigin
@RestController
public class SoapArtifactServiceImpl implements SoapArtifactService {

	@Autowired
	SoapArtifiactServiceDao soapArtifiactServiceDao;

	@Override
	public ResponseEntity<Object> getPostman(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam(value="orgId", required = false) String orgId, 
			@RequestParam("org") String org, @RequestParam("env") String env, @RequestParam("proxy") String proxy,
			@RequestParam("type") String type, @RequestParam("isSaaS") boolean isSaaS,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid,
			HttpServletRequest request, HttpServletResponse response) throws Throwable, IOException {

		response.setHeader("JSESSIONID", jsessionid);
		return new ResponseEntity<Object>(
				soapArtifiactServiceDao.getPostMan(org, env, proxy, interactionid, type.toLowerCase(), isSaaS), HttpStatus.OK);

	}

	@Override
	public ResponseEntity<Object> getPostmanFilesList(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(value="orgId", required = false) String orgId, 
			@RequestParam(value="org", required = false) String org, 
			@RequestParam(value="env", required = false) String env, 
			@RequestParam(value="proxy", required = false) String proxy,
			@RequestParam(value="type", required = false) String type, 
			@RequestParam(value="isSaaS", required = false) boolean isSaaS,
			HttpServletRequest request, HttpServletResponse response) {

		response.setHeader("JSESSIONID", jsessionid);
		return new ResponseEntity<Object>(soapArtifiactServiceDao.getPostManFilesList(interactionid), HttpStatus.OK);	

	}

	@Override
	public ResponseEntity<Object> getEnvFilesList(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(value="orgId", required = false) String orgId, 
			@RequestParam(value="org", required = false) String org, 
			@RequestParam(value="env", required = false) String env, 
			@RequestParam(value="proxy", required = false) String proxy,
			@RequestParam(value="type", required = false) String type, 
			@RequestParam(value="isSaaS", required = false) boolean isSaaS,
			HttpServletRequest request, HttpServletResponse response) {
		response.setHeader("JSESSIONID", jsessionid);
		if(org!=null && env!=null && proxy!=null && type!=null){

		}
		return new ResponseEntity<Object>(soapArtifiactServiceDao.getEnvFilesList(interactionid), HttpStatus.OK);

	}

	@Override
	public ResponseEntity<Object> updatePostman(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam(value="orgId", required = false) String orgId, 
			@RequestParam("file") MultipartFile file, @RequestParam("org") String org,
			@RequestParam("env") String env, @RequestParam("proxy") String proxy, @RequestParam("type") String type,
			@RequestHeader HttpHeaders headers, @RequestParam("isSaaS") boolean isSaaS,
			@RequestHeader(value = "JSESSIONID") String jsessionid, HttpServletRequest request,
			HttpServletResponse response) throws ItorixException {
		response.setHeader("JSESSIONID", jsessionid);
		return new ResponseEntity<Object>(
				soapArtifiactServiceDao.updatePostMan(file, org, env, proxy, interactionid, type.toLowerCase(), isSaaS),
				HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<Object> getenv(@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam(value="orgId", required = false) String orgId, 
			@RequestParam("org") String org, @RequestParam("env") String env, @RequestParam("proxy") String proxy,
			@RequestParam("type") String type, @RequestParam("isSaaS") boolean isSaaS,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid,
			HttpServletRequest request, HttpServletResponse response) throws IOException, ItorixException {
		response.setHeader("JSESSIONID", jsessionid);
		return new ResponseEntity<Object>(
				soapArtifiactServiceDao.getEnvFile(org, env, proxy, interactionid, type.toLowerCase(), isSaaS), HttpStatus.OK);

	}

	@Override
	public ResponseEntity<Object> updateEnvironementFile(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam(value="orgId", required = false) String orgId, 
			@RequestParam("file") MultipartFile file, @RequestParam("org") String org,
			@RequestParam("env") String env, @RequestParam("proxy") String proxy, @RequestParam("type") String type,
			@RequestParam("isSaaS") boolean isSaaS, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid, HttpServletRequest request,
			HttpServletResponse response) throws ItorixException {
		response.setHeader("JSESSIONID", jsessionid);
		return new ResponseEntity<Object>(
				soapArtifiactServiceDao.updateEnvFile(file, org, env, proxy, interactionid, type.toLowerCase(), isSaaS),
				HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<Object> deletePostManEnvFile(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam(value="orgId", required = false) String orgId, 
			@RequestParam("org") String org, @RequestParam("env") String env, @RequestParam("proxy") String proxy,
			@RequestParam("type") String type, @RequestParam("isSaaS") boolean isSaaS,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid,
			HttpServletRequest request, HttpServletResponse response) throws ItorixException {
		String recordtype = "postman";
		response.setHeader("JSESSIONID", jsessionid);
		return new ResponseEntity<Object>(soapArtifiactServiceDao.deletePostManEnvFile(org, env, proxy, interactionid,
				type.toLowerCase(), recordtype, isSaaS), HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<Object> deleteEnvFile(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam(value="orgId", required = false) String orgId, 
			@RequestParam("org") String org, @RequestParam("env") String env, @RequestParam("proxy") String proxy,
			@RequestParam("type") String type, @RequestParam("isSaaS") boolean isSaaS,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid,
			HttpServletRequest request, HttpServletResponse response) throws ItorixException {

		String recordtype = "env";
		response.setHeader("JSESSIONID", jsessionid);
		return new ResponseEntity<Object>(soapArtifiactServiceDao.deletePostManEnvFile(org, env, proxy, interactionid,
				type.toLowerCase(), recordtype, isSaaS), HttpStatus.NO_CONTENT);
	}

}
