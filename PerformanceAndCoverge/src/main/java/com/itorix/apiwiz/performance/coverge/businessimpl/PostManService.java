package com.itorix.apiwiz.performance.coverge.businessimpl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.itorix.apiwiz.common.model.GridFsData;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.model.postman.PostManFileInfo;
import com.itorix.apiwiz.common.model.postman.SoapUiFileInfo;
import com.itorix.apiwiz.common.service.GridFsRepository;
import com.itorix.apiwiz.identitymanagement.dao.BaseRepository;
import com.itorix.apiwiz.performance.coverge.model.PostManEnvFileInfo;
import com.mongodb.client.gridfs.model.GridFSFile;

@Component("postManService")
public class PostManService {

	private static final Logger logger = LoggerFactory.getLogger(PostManService.class);
	@Autowired
	GridFsRepository gridFsRepository;

	@Autowired
	BaseRepository baseRepository;

	@Autowired
	private MongoTemplate mongoTemplate;

	public PostManEnvFileInfo savePostMan(MultipartFile postmanFile, String org, String env, String proxy, String type)
			throws ItorixException {

		PostManEnvFileInfo postManEnvFileInfo = new PostManEnvFileInfo();
		try {

			PostManEnvFileInfo postManEnvFiledbInfo = findByOrgEnvProxy(org, env, proxy, type, false);
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
				postManEnvFileInfo.setGridPostmanFSFileid(oid);
				postManEnvFileInfo.setOrganization(org);
				postManEnvFileInfo.setEnvironment(env);
				postManEnvFileInfo.setProxy(proxy);
				postManEnvFileInfo.setPostManFileContent(postManFileContent);
				postManEnvFileInfo = baseRepository.save(postManEnvFileInfo);
			}
		} catch (IOException e) {
			logger.error("Exception occurred", e);
		}

		return null;
	}

	public PostManEnvFileInfo updatePostMan(MultipartFile postmanFile, String org, String env, String proxy,
			String interactionid, String type, boolean isSaaS) throws ItorixException {
		log("updatePostMan", interactionid, org, env, proxy);
		PostManFileInfo postManEnvFileInfo = new PostManFileInfo();
		try {

			PostManFileInfo postManFiledbInfo = findPostManByOrgEnvProxy(org, env, proxy, type, isSaaS);
			if (postManFiledbInfo != null) {
				logger.debug("Updating postman file");
				GridFSFile gridFSFile = gridFsRepository.store(new GridFsData(postmanFile.getInputStream(),
						org + "-" + env + "-" + proxy + "-" + type + "_postManFile"));
				String oid = gridFSFile.getId().toString();
				ByteArrayInputStream stream = new ByteArrayInputStream(postmanFile.getBytes());
				String myString = IOUtils.toString(stream, "UTF-8");

				String postManFileContent = null;
				postManFileContent = myString;
				postManFiledbInfo.setGridPostmanFSFileid(oid);
				postManFiledbInfo.setPostManFileContent(postManFileContent);
				postManFiledbInfo.setOriginalPostManFileName(postmanFile.getOriginalFilename());
				postManFiledbInfo = baseRepository.save(postManFiledbInfo);
			} else {
				GridFSFile gridFSFile = gridFsRepository.store(new GridFsData(postmanFile.getInputStream(),
						org + "-" + env + "-" + proxy + "-" + type + "_postManFile"));
				String oid = gridFSFile.getId().toString();
				ByteArrayInputStream stream = new ByteArrayInputStream(postmanFile.getBytes());
				String myString = IOUtils.toString(stream, "UTF-8");

				String postManFileContent = null;
				postManFileContent = myString;
				postManEnvFileInfo.setGridPostmanFSFileid(oid);
				postManEnvFileInfo.setOrganization(org);
				postManEnvFileInfo.setEnvironment(env);
				postManEnvFileInfo.setProxy(proxy);
				postManEnvFileInfo.setType(type);
				postManEnvFileInfo.setIsSaaS(isSaaS);
				postManEnvFileInfo.setOriginalPostManFileName(postmanFile.getOriginalFilename());
				postManEnvFileInfo.setPostManFileContent(postManFileContent);
				postManEnvFileInfo = baseRepository.save(postManEnvFileInfo);
			}
		} catch (IOException e) {
			logger.error("Exception occurred", e);
		}

		return null;
	}

	public PostManEnvFileInfo findByOrgEnvProxy(String org, String env, String proxy, String type, boolean isSaaS) {
		Query query = new Query(
				new Criteria().andOperator(Criteria.where(PostManEnvFileInfo.LABEL_PROXY_INFO).is(proxy),
						Criteria.where(PostManEnvFileInfo.LABEL_ENV_NAME).is(env),
						Criteria.where(PostManEnvFileInfo.LABEL_ORG_NAME).is(org),
						Criteria.where(PostManEnvFileInfo.LABEL_TYPE).is(type),
						Criteria.where(PostManEnvFileInfo.IS_SAAS).is(isSaaS)));
		PostManEnvFileInfo postManEnvFileInfo = mongoTemplate.findOne(query, PostManEnvFileInfo.class);
		return postManEnvFileInfo;
	}

	public PostManFileInfo findPostManByOrgEnvProxy(String org, String env, String proxy, String type, boolean isSaaS) {
		Query query = new Query(
				new Criteria().andOperator(Criteria.where(PostManEnvFileInfo.LABEL_PROXY_INFO).is(proxy),
						Criteria.where(PostManEnvFileInfo.LABEL_ENV_NAME).is(env),
						Criteria.where(PostManEnvFileInfo.LABEL_ORG_NAME).is(org),
						Criteria.where(PostManEnvFileInfo.LABEL_TYPE).is(type),
						Criteria.where(PostManEnvFileInfo.IS_SAAS).is(isSaaS)));
		PostManFileInfo postManFileInfo = mongoTemplate.findOne(query, PostManFileInfo.class);
		return postManFileInfo;
	}

	public SoapUiFileInfo findSoapUiFileByOrgEnvProxy(String org, String env, String proxy, String type,
			boolean isSaaS) {
		Query query = new Query(
				new Criteria().andOperator(Criteria.where(PostManEnvFileInfo.LABEL_PROXY_INFO).is(proxy),
						Criteria.where(PostManEnvFileInfo.LABEL_ENV_NAME).is(env),
						Criteria.where(PostManEnvFileInfo.LABEL_ORG_NAME).is(org),
						Criteria.where(PostManEnvFileInfo.LABEL_TYPE).is(type),
						Criteria.where(PostManEnvFileInfo.IS_SAAS).is(isSaaS)));
		SoapUiFileInfo postManFileInfo = mongoTemplate.findOne(query, SoapUiFileInfo.class);
		return postManFileInfo;
	}

	public PostManEnvFileInfo saveEnvFile(MultipartFile envFile, String org, String env, String proxy, String type)
			throws ItorixException {
		PostManEnvFileInfo postManEnvFileInfo = new PostManEnvFileInfo();
		try {

			PostManEnvFileInfo postManEnvFiledbInfo = findByOrgEnvProxy(org, env, proxy, type, false);
			if (postManEnvFiledbInfo == null
					|| (postManEnvFiledbInfo != null && postManEnvFiledbInfo.getEnvFileContent() == null)) {
				GridFSFile gridFSFile = gridFsRepository
						.store(new GridFsData(envFile.getInputStream(), org + "-" + env + "-" + proxy + "_envFile"));
				String oid = gridFSFile.getId().toString();
				ByteArrayInputStream stream = new ByteArrayInputStream(envFile.getBytes());
				String myString = IOUtils.toString(stream, "UTF-8");

				String envFileContent = null;
				envFileContent = myString;
				if (postManEnvFiledbInfo != null) {
					postManEnvFiledbInfo.setEnvFileContent(envFileContent);
					postManEnvFiledbInfo.setGridEnvFSFileid(oid);
					postManEnvFiledbInfo = baseRepository.save(postManEnvFiledbInfo);
				} else {
					postManEnvFileInfo.setGridEnvFSFileid(oid);
					postManEnvFileInfo.setOrganization(org);
					postManEnvFileInfo.setEnvironment(env);
					postManEnvFileInfo.setProxy(proxy);
					postManEnvFileInfo.setEnvFileContent(envFileContent);
					postManEnvFileInfo = baseRepository.save(postManEnvFileInfo);
				}
			} else {
				throw new ItorixException(new Throwable().getMessage(), "Connector-1001", new Throwable());
			}
		} catch (IOException e) {
			logger.error("Exception occurred", e);
		}

		return null;
	}

	public PostManEnvFileInfo updateEnvFile(MultipartFile envFile, String org, String env, String proxy,
			String interactionid, String type, boolean isSaaS) throws ItorixException {
		log("updateEnvFile", interactionid, org, env, proxy);
		try {

			PostManEnvFileInfo postManEnvFiledbInfo = findByOrgEnvProxy(org, env, proxy, type, isSaaS);
			if (postManEnvFiledbInfo != null) {
				GridFSFile gridFSFile = gridFsRepository.store(new GridFsData(envFile.getInputStream(),
						org + "-" + env + "-" + proxy + "-" + type + "_envFile"));
				String oid = gridFSFile.getId().toString();
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
				PostManEnvFileInfo postManEnvFileInfo = new PostManEnvFileInfo();
				GridFSFile gridFSFile = gridFsRepository.store(new GridFsData(envFile.getInputStream(),
						org + "-" + env + "-" + proxy + "-" + type + "_envFile"));
				String oid = gridFSFile.getId().toString();
				ByteArrayInputStream stream = new ByteArrayInputStream(envFile.getBytes());
				String myString = IOUtils.toString(stream, "UTF-8");

				String envFileContent = null;
				envFileContent = myString;
				postManEnvFileInfo.setGridEnvFSFileid(oid);
				postManEnvFileInfo.setOrganization(org);
				postManEnvFileInfo.setEnvironment(env);
				postManEnvFileInfo.setProxy(proxy);
				postManEnvFileInfo.setType(type);
				postManEnvFileInfo.setIsSaaS(isSaaS);
				postManEnvFileInfo.setEnvFileContent(envFileContent);
				postManEnvFileInfo.setOriginalEnvFileName(envFile.getOriginalFilename());
				postManEnvFileInfo = baseRepository.save(postManEnvFileInfo);
			}
		} catch (IOException e) {
			logger.error("Exception occurred", e);
		}

		return null;
	}

	public Object getSoapUiInfoFile(String org, String env, String proxy, String interactionid, String type,
			boolean isSaaS) throws ItorixException, IOException {
		log("SoapUiFileInfo", interactionid, org, env, proxy);
		SoapUiFileInfo soapUiFile = findSoapUiFileByOrgEnvProxy(org, env, proxy, type, isSaaS);

		if (soapUiFile != null) {
			File file1 = new File("SoapUiFileInfo");
			byte[] filebytes = IOUtils.toByteArray(soapUiFile.getPostManFileContent());
			String str = new String(filebytes, "UTF-8");
			log("getSoapUiInfoFile", interactionid, str);
			return str;
		} else {
			throw new ItorixException(new Throwable().getMessage(), "Connector-1002", new Throwable());
		}
	}

	public Object getPostMan(String org, String env, String proxy, String interactionid, String type, boolean isSaaS)
			throws ItorixException, IOException {
		log("getPostMan", interactionid, org, env, proxy);
		PostManFileInfo postManEnvFiledbInfo = findPostManByOrgEnvProxy(org, env, proxy, type, isSaaS);

		if (postManEnvFiledbInfo != null) {
			File file1 = new File("PostManfile");
			byte[] filebytes = IOUtils.toByteArray(postManEnvFiledbInfo.getPostManFileContent());
			String str = new String(filebytes, "UTF-8");
			log("getPostMan", interactionid, str);
			return str;
		} else {
			throw new ItorixException(new Throwable().getMessage(), "Connector-1002", new Throwable());
		}
	}

	public Object getEnvFile(String org, String env, String proxy, String interactionid, String type, boolean isSaaS)
			throws IOException, ItorixException {
		log("getEnvFile", interactionid, org, env, proxy);
		PostManEnvFileInfo postManEnvFiledbInfo = findByOrgEnvProxy(org, env, proxy, type, isSaaS);

		if (postManEnvFiledbInfo != null) {
			File file1 = new File("EnvironmentFile");
			byte[] filebytes = IOUtils.toByteArray(postManEnvFiledbInfo.getEnvFileContent());
			String str = new String(filebytes, "UTF-8");
			log("getEnvFile", interactionid, org, env, str);
			return str;
			/*
			 * FileUtils.writeByteArrayToFile(file1, str.getBytes()); return
			 * file1;
			 */
		} else {
			throw new ItorixException(new Throwable().getMessage(), "Connector-1003", new Throwable());
		}
	}

	public Object deletePostManEnvFile(String org, String env, String proxy, String interactionid, String type,
			String recordtype, boolean isSaaS) throws ItorixException {
		log("deletePostManEnvFile", interactionid, org, env, proxy);

		if (recordtype != null && recordtype.equalsIgnoreCase("env")) {
			PostManEnvFileInfo postManEnvFiledbInfo = findByOrgEnvProxy(org, env, proxy, type, isSaaS);
			if (postManEnvFiledbInfo != null) {
				baseRepository.delete(postManEnvFiledbInfo.getId(), PostManEnvFileInfo.class);

			} else {
				throw new ItorixException(new Throwable().getMessage(), "Connector-1004", new Throwable());
			}

		} else if (recordtype != null && recordtype.equalsIgnoreCase("postman")) {
			PostManFileInfo postManFiledbInfo = findPostManByOrgEnvProxy(org, env, proxy, type, isSaaS);
			if (postManFiledbInfo != null) {
				baseRepository.delete(postManFiledbInfo.getId(), PostManFileInfo.class);
			} else {
				throw new ItorixException(new Throwable().getMessage(), "Connector-1004", new Throwable());
			}
		}

		return null;
	}

	private void log(String methodName, String interactionid, Object... body) {

		logger.debug("PostManService." + methodName + " : CorelationId=" + interactionid + " : request/response Body ="
				+ body);
	}

	public Object getPostManFilesList(String interactionid) {

		logger.debug("getPostManFilesList : CorelationId= " + interactionid);
		List<PostManFileInfo> postManEnvFileInfolist = mongoTemplate.findAll(PostManFileInfo.class);
		List<Map> postManFileNamesList = new ArrayList<Map>();

		for (PostManFileInfo postManEnvFileInfo : postManEnvFileInfolist) {
			Map<String, Object> postManEnvFileInfoMap = new HashMap<String, Object>();
			if (postManEnvFileInfo.getOriginalPostManFileName() != null) {
				postManEnvFileInfoMap.put("fileName", postManEnvFileInfo.getOriginalPostManFileName());
				postManEnvFileInfoMap.put("org", postManEnvFileInfo.getOrganization());
				postManEnvFileInfoMap.put("env", postManEnvFileInfo.getEnvironment());
				postManEnvFileInfoMap.put("type", postManEnvFileInfo.getType());
				postManEnvFileInfoMap.put("proxy", postManEnvFileInfo.getProxy());
				postManEnvFileInfoMap.put("isSaaS", postManEnvFileInfo.getIsSaaS());
				postManEnvFileInfoMap.put("createdBy", postManEnvFileInfo.getCreatedUserName());
				postManEnvFileInfoMap.put("modifiedBy", postManEnvFileInfo.getModifiedUserName());
				postManEnvFileInfoMap.put("mts", postManEnvFileInfo.getMts());
				postManFileNamesList.add(postManEnvFileInfoMap);
			}
		}
		return postManFileNamesList;
	}

	public Object getEnvFilesList(String interactionid) {

		logger.debug("getEnvFilesList : CorelationId= " + interactionid);
		List<PostManEnvFileInfo> postManEnvFileInfolist = mongoTemplate.findAll(PostManEnvFileInfo.class);
		List<Map> envFileNamesList = new ArrayList<Map>();
		for (PostManEnvFileInfo postManEnvFileInfo : postManEnvFileInfolist) {
			Map<String, Object> postManEnvFileInfoMap = new HashMap<String, Object>();
			if (postManEnvFileInfo.getOriginalEnvFileName() != null) {
				postManEnvFileInfoMap.put("fileName", postManEnvFileInfo.getOriginalEnvFileName());
				postManEnvFileInfoMap.put("org", postManEnvFileInfo.getOrganization());
				postManEnvFileInfoMap.put("env", postManEnvFileInfo.getEnvironment());
				postManEnvFileInfoMap.put("type", postManEnvFileInfo.getType());
				postManEnvFileInfoMap.put("proxy", postManEnvFileInfo.getProxy());
				postManEnvFileInfoMap.put("isSaaS", postManEnvFileInfo.getIsSaaS());
				postManEnvFileInfoMap.put("createdBy", postManEnvFileInfo.getCreatedUserName());
				postManEnvFileInfoMap.put("modifiedBy", postManEnvFileInfo.getModifiedUserName());
				postManEnvFileInfoMap.put("mts", postManEnvFileInfo.getMts());
				envFileNamesList.add(postManEnvFileInfoMap);
			}
		}
		return envFileNamesList;
	}
}
