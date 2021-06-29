package com.itorix.apiwiz.cicd.dao;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.itorix.apiwiz.common.model.GridFsData;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.model.postman.PostManEnvFileInfo;
import com.itorix.apiwiz.common.model.postman.PostManFileInfo;
import com.itorix.apiwiz.common.model.postman.PostmanEnvValue;
import com.itorix.apiwiz.common.model.postman.SoapUiEnvFileInfo;
import com.itorix.apiwiz.common.model.postman.SoapUiFileInfo;
import com.itorix.apiwiz.common.service.GridFsRepository;
import com.itorix.apiwiz.identitymanagement.dao.BaseRepository;
//import com.mongodb.gridfs.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSFile;


@Component
public class SoapArtifiactServiceDao {

	@Autowired
	GridFsRepository gridFsRepository;

	@Autowired
	BaseRepository baseRepository;

	@Autowired
	private MongoTemplate mongoTemplate;

	public PostmanEnvValue savePostMan(MultipartFile postmanFile, String org, String env, String proxy,String type)
			throws ItorixException {
		PostManEnvFileInfo soapUiEnvFileInfo = new PostManEnvFileInfo();
		try {
			SoapUiEnvFileInfo postManEnvFiledbInfo = findByOrgEnvProxy(org, env, proxy,type,false);
			if (postManEnvFiledbInfo != null && postManEnvFiledbInfo.getPostManFileContent() != null) {
				throw new ItorixException(new Throwable().getMessage(), "Connector-1000", new Throwable());
			} else {
				GridFSFile gridFSFile = gridFsRepository.store(
						new GridFsData(postmanFile.getInputStream(), org + "-" + env + "-" + proxy + "_postManFile"));
				String oid = gridFSFile.getId().toString();
				ByteArrayInputStream stream = new ByteArrayInputStream(postmanFile.getBytes());
				String myString = IOUtils.toString(stream, "UTF-8");
				String postManFileContent = null;
				postManFileContent = myString;
				soapUiEnvFileInfo.setGridPostmanFSFileid(oid);
				soapUiEnvFileInfo.setOrganization(org);
				soapUiEnvFileInfo.setEnvironment(env);
				soapUiEnvFileInfo.setProxy(proxy);
				soapUiEnvFileInfo.setPostManFileContent(postManFileContent);
				soapUiEnvFileInfo = baseRepository.save(soapUiEnvFileInfo);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public PostManEnvFileInfo updatePostMan(MultipartFile file, String org, String env, String proxy,String interactionid, String type, boolean isSaaS)
			throws ItorixException {
		//log("updatePostMan",interactionid,org, env, proxy);
		SoapUiFileInfo soapUiFileInfo = new SoapUiFileInfo();
		try {
			SoapUiFileInfo soapuiFiledbInfo = findPostManByOrgEnvProxy(org, env, proxy,type, isSaaS);
			if (soapuiFiledbInfo != null ) {
				String oid = gridFsRepository.storeFile(
						new GridFsData(file.getInputStream(), org + "-" + env + "-" + proxy + "-" +type + "_postManFile"));
				//String oid = gridFSFile.getId().toString();
				ByteArrayInputStream stream = new ByteArrayInputStream(file.getBytes());
				String myString = IOUtils.toString(stream, "UTF-8");
				String postManFileContent = null;
				postManFileContent = myString;
				soapuiFiledbInfo.setGridPostmanFSFileid(oid);
				soapuiFiledbInfo.setPostManFileContent(postManFileContent);
				soapuiFiledbInfo.setOriginalPostManFileName(file.getOriginalFilename());
				soapuiFiledbInfo = baseRepository.save(soapuiFiledbInfo);
			} else {
				String oid = gridFsRepository.storeFile(
						new GridFsData(file.getInputStream(), org + "-" + env + "-" + proxy + "-" +type + "_postManFile"));
				//String oid = gridFSFile.getId().toString();
				ByteArrayInputStream stream = new ByteArrayInputStream(file.getBytes());
				String myString = IOUtils.toString(stream, "UTF-8");
				String postManFileContent = null;
				postManFileContent = myString;
				soapUiFileInfo.setGridPostmanFSFileid(oid);
				soapUiFileInfo.setOrganization(org);
				soapUiFileInfo.setEnvironment(env);
				soapUiFileInfo.setProxy(proxy);
				soapUiFileInfo.setType(type);
				soapUiFileInfo.setIsSaaS(isSaaS);
				soapUiFileInfo.setOriginalPostManFileName(file.getOriginalFilename());
				soapUiFileInfo.setPostManFileContent(postManFileContent);
				soapUiFileInfo = baseRepository.save(soapUiFileInfo);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public SoapUiEnvFileInfo findByOrgEnvProxy(String org, String env, String proxy, String type, boolean isSaaS) {
		Query query = new Query(
				new Criteria().andOperator(Criteria.where(PostManEnvFileInfo.LABEL_PROXY_INFO).is(proxy),
						Criteria.where(PostManEnvFileInfo.LABEL_ENV_NAME).is(env),
						Criteria.where(PostManEnvFileInfo.LABEL_ORG_NAME).is(org),
						Criteria.where(PostManEnvFileInfo.LABEL_TYPE).is(type),
						Criteria.where(PostManEnvFileInfo.IS_SAAS).is(isSaaS)));
		SoapUiEnvFileInfo soapUiEnvFileInfo = mongoTemplate.findOne(query, SoapUiEnvFileInfo.class);
		return soapUiEnvFileInfo;
	}
	

	public SoapUiFileInfo findPostManByOrgEnvProxy(String org, String env, String proxy, String type, boolean isSaaS) {
		Query query = new Query(
				new Criteria().andOperator(Criteria.where(PostManEnvFileInfo.LABEL_PROXY_INFO).is(proxy),
						Criteria.where(PostManEnvFileInfo.LABEL_ENV_NAME).is(env),
						Criteria.where(PostManEnvFileInfo.LABEL_ORG_NAME).is(org),
						Criteria.where(PostManEnvFileInfo.LABEL_TYPE).is(type),
						Criteria.where(PostManEnvFileInfo.IS_SAAS).is(isSaaS)));
		SoapUiFileInfo soapUiFileInfo = mongoTemplate.findOne(query, SoapUiFileInfo.class);
		return soapUiFileInfo;
	}
	

	public SoapUiEnvFileInfo saveEnvFile(MultipartFile envFile, String org, String env, String proxy,String type)
			throws ItorixException {
		SoapUiEnvFileInfo soapUiEnvFileInfo = new SoapUiEnvFileInfo();
		try {
			SoapUiEnvFileInfo soapUiEnvFiledbInfo = findByOrgEnvProxy(org, env, proxy,type,false);
			if (soapUiEnvFiledbInfo == null
					|| (soapUiEnvFiledbInfo != null && soapUiEnvFiledbInfo.getEnvFileContent() == null)) {
				GridFSFile gridFSFile = gridFsRepository
						.store(new GridFsData(envFile.getInputStream(), org + "-" + env + "-" + proxy + "_envFile"));
				String oid = gridFSFile.getId().toString();
				ByteArrayInputStream stream = new ByteArrayInputStream(envFile.getBytes());
				String myString = IOUtils.toString(stream, "UTF-8");
				String envFileContent = null;
				envFileContent = myString;
				if (soapUiEnvFiledbInfo != null) {
					soapUiEnvFiledbInfo.setEnvFileContent(envFileContent);
					soapUiEnvFiledbInfo.setGridEnvFSFileid(oid);
					soapUiEnvFiledbInfo = baseRepository.save(soapUiEnvFiledbInfo);
				} else {
					soapUiEnvFileInfo.setGridEnvFSFileid(oid);
					soapUiEnvFileInfo.setOrganization(org);
					soapUiEnvFileInfo.setEnvironment(env);
					soapUiEnvFileInfo.setProxy(proxy);
					soapUiEnvFileInfo.setEnvFileContent(envFileContent);
					soapUiEnvFileInfo = baseRepository.save(soapUiEnvFileInfo);
				}
			} else {
				throw new ItorixException(new Throwable().getMessage(), "Connector-1001", new Throwable());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public SoapUiEnvFileInfo updateEnvFile(MultipartFile envFile, String org, String env, String proxy,String interactionid,String type, boolean isSaaS)
			throws ItorixException {
		//log("updateEnvFile",interactionid,org, env, proxy);
		try {
			SoapUiEnvFileInfo postManEnvFiledbInfo = findByOrgEnvProxy(org, env, proxy,type, isSaaS);
			if (postManEnvFiledbInfo != null ) {
				String oid = gridFsRepository
						.storeFile(new GridFsData(envFile.getInputStream(), org + "-" + env + "-" + proxy+ "-" +type + "_envFile"));
				//String oid = gridFSFile.getId().toString();
				ByteArrayInputStream stream = new ByteArrayInputStream(envFile.getBytes());
				String myString = IOUtils.toString(stream, "UTF-8");
				String envFileContent = null;
				envFileContent = myString;
				postManEnvFiledbInfo.setEnvFileContent(envFileContent);
				postManEnvFiledbInfo.setGridEnvFSFileid(oid);
				postManEnvFiledbInfo.setOrganization(org);
				postManEnvFiledbInfo.setEnvironment(env);
				postManEnvFiledbInfo.setProxy(proxy);
				postManEnvFiledbInfo.setOriginalEnvFileName(envFile.getOriginalFilename());
				postManEnvFiledbInfo = baseRepository.save(postManEnvFiledbInfo);
			} else {
				SoapUiEnvFileInfo soapUiEnvFileInfo = new SoapUiEnvFileInfo();
				String oid =gridFsRepository
						.storeFile(new GridFsData(envFile.getInputStream(), org + "-" + env + "-" + proxy+ "-" +type + "_envFile"));
				//String oid = gridFSFile.getId().toString();
				ByteArrayInputStream stream = new ByteArrayInputStream(envFile.getBytes());
				String myString = IOUtils.toString(stream, "UTF-8");
				String envFileContent = null;
				envFileContent = myString;
				soapUiEnvFileInfo.setGridEnvFSFileid(oid);
				soapUiEnvFileInfo.setOrganization(org);
				soapUiEnvFileInfo.setEnvironment(env);
				soapUiEnvFileInfo.setProxy(proxy);
				soapUiEnvFileInfo.setType(type);
				soapUiEnvFileInfo.setIsSaaS(isSaaS);
				soapUiEnvFileInfo.setEnvFileContent(envFileContent);
				soapUiEnvFileInfo.setOriginalEnvFileName(envFile.getOriginalFilename());
				soapUiEnvFileInfo = baseRepository.save(soapUiEnvFileInfo);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public Object getEnvFile(String org, String env, String proxy,String interactionid, String type, boolean isSaaS) throws IOException, ItorixException {
		SoapUiEnvFileInfo soapUiEnvFiledbInfo = findByOrgEnvProxy(org, env, proxy,type, isSaaS);
		if (soapUiEnvFiledbInfo != null) {
			File file1 = new File("EnvironmentFile");
			byte[] filebytes = IOUtils.toByteArray(soapUiEnvFiledbInfo.getEnvFileContent());
			String str = new String(filebytes, "UTF-8");
			return str;
		} else {
			throw new ItorixException(new Throwable().getMessage(), "Connector-1003", new Throwable());
		}
	}

	public Object deletePostManEnvFile(String org, String env, String proxy,String interactionid, String type, String recordtype, boolean isSaaS) throws ItorixException {
		if(recordtype!=null &&  recordtype.equalsIgnoreCase("env")){
			SoapUiEnvFileInfo soapUiEnvFileInfo = findByOrgEnvProxy(org, env, proxy,type, isSaaS);
			if (soapUiEnvFileInfo != null) {
				baseRepository.delete(soapUiEnvFileInfo.getId(), SoapUiEnvFileInfo.class);
			}else{
				throw new ItorixException(new Throwable().getMessage(), "Connector-1004", new Throwable());
			}
		}else if(recordtype!=null &&  recordtype.equalsIgnoreCase("postman")){
			SoapUiFileInfo soapUiFileInfo = findPostManByOrgEnvProxy(org, env, proxy,type,isSaaS);
			if (soapUiFileInfo != null) {
				baseRepository.delete(soapUiFileInfo.getId(), SoapUiFileInfo.class);
			}else{
				throw new ItorixException(new Throwable().getMessage(), "Connector-1004", new Throwable());
			}
		}
		return null;
	}
	
	public Object getPostManFilesList(String interactionid) {
	//logger.info("getPostManFilesList : CorelationId= "+ interactionid );
	List<SoapUiFileInfo>  soapUiFileInfolist= mongoTemplate.findAll(SoapUiFileInfo.class);
	List<Map> soapUiFileNamesList=new ArrayList<Map>();
	for (SoapUiFileInfo soapUiFileInfo : soapUiFileInfolist) {
		Map<String,Object> soapUiFileInfoMap =new HashMap<String,Object>();
		if(soapUiFileInfo.getOriginalPostManFileName()!=null){
			soapUiFileInfoMap.put("fileName", soapUiFileInfo.getOriginalPostManFileName());
			soapUiFileInfoMap.put("org",soapUiFileInfo.getOrganization());
			soapUiFileInfoMap.put("env",soapUiFileInfo.getEnvironment());
			soapUiFileInfoMap.put("type", soapUiFileInfo.getType());
			soapUiFileInfoMap.put("proxy", soapUiFileInfo.getProxy());
			soapUiFileInfoMap.put("isSaaS", soapUiFileInfo.getIsSaaS());
			soapUiFileInfoMap.put("createdBy", soapUiFileInfo.getCreatedUserName());
			soapUiFileInfoMap.put("modifiedBy", soapUiFileInfo.getModifiedUserName());
			soapUiFileInfoMap.put("mts", soapUiFileInfo.getMts());
			soapUiFileNamesList.add(soapUiFileInfoMap);
		}
	}
		return soapUiFileNamesList;
	}

	public Object getEnvFilesList(String interactionid) {
		//logger.info("getEnvFilesList : CorelationId= "+ interactionid );
		List<SoapUiEnvFileInfo>  soapUiEnvFileInfolist= mongoTemplate.findAll(SoapUiEnvFileInfo.class);
		List<Map> envFileNamesList=new ArrayList<Map>();
		for (SoapUiEnvFileInfo postManEnvFileInfo : soapUiEnvFileInfolist) {
			Map<String,Object> soapUiEnvFileInfoMap =new HashMap<String,Object>();
			if(postManEnvFileInfo.getOriginalEnvFileName()!=null){
				soapUiEnvFileInfoMap.put("fileName", postManEnvFileInfo.getOriginalEnvFileName());
				soapUiEnvFileInfoMap.put("org",postManEnvFileInfo.getOrganization());
				soapUiEnvFileInfoMap.put("env",postManEnvFileInfo.getEnvironment());
				soapUiEnvFileInfoMap.put("type", postManEnvFileInfo.getType());
				soapUiEnvFileInfoMap.put("proxy", postManEnvFileInfo.getProxy());
				soapUiEnvFileInfoMap.put("isSaaS", postManEnvFileInfo.getIsSaaS());
				soapUiEnvFileInfoMap.put("createdBy", postManEnvFileInfo.getCreatedUserName());
				soapUiEnvFileInfoMap.put("modifiedBy", postManEnvFileInfo.getModifiedUserName());
				soapUiEnvFileInfoMap.put("mts", postManEnvFileInfo.getMts());
				envFileNamesList.add(soapUiEnvFileInfoMap);
			}
		}
		return envFileNamesList;
	}

	
	public Object getPostMan(String org, String env, String proxy,String interactionid, String type, boolean isSaaS) throws ItorixException, IOException {
		SoapUiFileInfo postManEnvFiledbInfo = findPostManByOrgEnvProxy(org, env, proxy,type,isSaaS);
		if (postManEnvFiledbInfo != null) {
			File file1 = new File("PostManfile");
			byte[] filebytes = IOUtils.toByteArray(postManEnvFiledbInfo.getPostManFileContent());
			String str = new String(filebytes, "UTF-8");
			return str;
		} else {
			throw new ItorixException(new Throwable().getMessage(), "Connector-1002", new Throwable());
		}
	}
	
	
}
