package com.itorix.apiwiz.cicd.dao;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
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
import com.itorix.apiwiz.common.service.GridFsRepository;
import com.itorix.apiwiz.identitymanagement.dao.BaseRepository;
// import com.mongodb.gridfs.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSFile;
@Slf4j
@Component
public class PostmanArtifiactServiceDao {

	@Autowired
	GridFsRepository gridFsRepository;

	@Autowired
	BaseRepository baseRepository;

	@Autowired
	private MongoTemplate mongoTemplate;

	public PostmanEnvValue savePostMan(MultipartFile postmanFile, String org, String env, String proxy, String type)
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
			log.error("Exception occurred", e);
		}
		return null;
	}

	public PostManEnvFileInfo updatePostMan(MultipartFile postmanFile, String org, String env, String proxy,
			String interactionid, String type, boolean isSaaS) throws ItorixException {
		// log("updatePostMan",interactionid,org, env, proxy);
		PostManFileInfo postManEnvFileInfo = new PostManFileInfo();
		try {
			PostManFileInfo postManFiledbInfo = findPostManByOrgEnvProxy(org, env, proxy, type, isSaaS);
			if (postManFiledbInfo != null) {
				String oid = gridFsRepository.storeFile(new GridFsData(postmanFile.getInputStream(),
						org + "-" + env + "-" + proxy + "-" + type + "_postManFile"));
				// String oid = gridFSFile.getId().toString();
				ByteArrayInputStream stream = new ByteArrayInputStream(postmanFile.getBytes());
				String myString = IOUtils.toString(stream, "UTF-8");
				String postManFileContent = null;
				postManFileContent = myString;
				postManFiledbInfo.setGridPostmanFSFileid(oid);
				postManFiledbInfo.setPostManFileContent(postManFileContent);
				postManFiledbInfo.setOriginalPostManFileName(postmanFile.getOriginalFilename());
				postManFiledbInfo = baseRepository.save(postManFiledbInfo);
			} else {
				String oid = gridFsRepository.storeFile(new GridFsData(postmanFile.getInputStream(),
						org + "-" + env + "-" + proxy + "-" + type + "_postManFile"));
				// String oid = gridFSFile.getId().toString();
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
			log.error("Exception occurred", e);
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

	public PostManEnvFileInfo saveEnvFile(MultipartFile envFile, String org, String env, String proxy, String type)
			throws ItorixException {
		PostManEnvFileInfo postManEnvFileInfo = new PostManEnvFileInfo();
		try {
			PostManEnvFileInfo postManEnvFiledbInfo = findByOrgEnvProxy(org, env, proxy, type, false);
			if (postManEnvFiledbInfo == null
					|| (postManEnvFiledbInfo != null && postManEnvFiledbInfo.getEnvFileContent() == null)) {
				log.debug("Saving postManEnvFiledbInfo");
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
			log.error("Exception occurred", e);
		}
		return null;
	}

	public PostManEnvFileInfo updateEnvFile(MultipartFile envFile, String org, String env, String proxy,
			String interactionid, String type, boolean isSaaS) throws ItorixException {
		try {
			PostManEnvFileInfo postManEnvFiledbInfo = findByOrgEnvProxy(org, env, proxy, type, isSaaS);
			if (postManEnvFiledbInfo != null) {
				String oid = gridFsRepository.storeFile(new GridFsData(envFile.getInputStream(),
						org + "-" + env + "-" + proxy + "-" + type + "_envFile"));
				// String oid = gridFSFile.getId().toString();
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
				String oid = gridFsRepository.storeFile(new GridFsData(envFile.getInputStream(),
						org + "-" + env + "-" + proxy + "-" + type + "_envFile"));
				// String oid = gridFSFile.getId().toString();
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
			log.error("Exception occurred", e);
		}
		return null;
	}

	public Object getEnvFile(String org, String env, String proxy, String interactionid, String type, boolean isSaaS)
			throws IOException, ItorixException {
		PostManEnvFileInfo postManEnvFiledbInfo = findByOrgEnvProxy(org, env, proxy, type, isSaaS);
		if (postManEnvFiledbInfo != null) {
			File file1 = new File("EnvironmentFile");
			byte[] filebytes = IOUtils.toByteArray(postManEnvFiledbInfo.getEnvFileContent());
			String str = new String(filebytes, "UTF-8");
			// log("getEnvFile",interactionid,org, env, str);
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
		if (recordtype != null && recordtype.equalsIgnoreCase("env")) {
			log.debug("Deleting PostManEnvFileInfo");
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

	public Object getPostManFilesList(String interactionid) {
		Query query = new Query().with(Sort.by(Sort.Direction.DESC, "mts"));
		List<PostManFileInfo> postManEnvFileInfolist = mongoTemplate.find(query, PostManFileInfo.class);
		List<Map> postManFileNamesList = new ArrayList<Map>();
		for (PostManFileInfo postManEnvFileInfo : postManEnvFileInfolist) {
			Map<String, Object> postManEnvFileInfoMap = new HashMap<String, Object>();
			if (postManEnvFileInfo.getOriginalPostManFileName() != null) {
				log.debug("Adding postManEnvFileInfoMap to postManFileNamesList");
				postManEnvFileInfoMap.put("id", postManEnvFileInfo.getId());
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

	public Object getPostManFilesList(String org, String env, String proxy, String type, boolean isSaaS) {
		PostManFileInfo postManFiledbInfo = findPostManByOrgEnvProxy(org, env, proxy, type, isSaaS);
		postManFiledbInfo.setPostManFileContent(null);
		postManFiledbInfo.setEnvFileContent(null);
		return postManFiledbInfo;
	}

	public Object getEnvFilesList(String interactionid) {
		Query query = new Query().with(Sort.by(Sort.Direction.DESC, "mts"));
		List<PostManEnvFileInfo> postManEnvFileInfolist = mongoTemplate.find(query, PostManEnvFileInfo.class);
		List<Map> envFileNamesList = new ArrayList<Map>();
		for (PostManEnvFileInfo postManEnvFileInfo : postManEnvFileInfolist) {
			Map<String, Object> postManEnvFileInfoMap = new HashMap<String, Object>();
			if (postManEnvFileInfo.getOriginalEnvFileName() != null) {
				postManEnvFileInfoMap.put("id", postManEnvFileInfo.getId());
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

	public Object getEnvFilesList(String org, String env, String proxy, String type, boolean isSaaS) {
		PostManEnvFileInfo postManEnvFiledbInfo = findByOrgEnvProxy(org, env, proxy, type, isSaaS);
		postManEnvFiledbInfo.setPostManFileContent(null);
		postManEnvFiledbInfo.setEnvFileContent(null);
		return postManEnvFiledbInfo;
	}

	public Object getPostMan(String org, String env, String proxy, String interactionid, String type, boolean isSaaS)
			throws ItorixException, IOException {
		PostManFileInfo postManEnvFiledbInfo = findPostManByOrgEnvProxy(org, env, proxy, type, isSaaS);
		if (postManEnvFiledbInfo != null) {
			File file1 = new File("PostManfile");
			byte[] filebytes = IOUtils.toByteArray(postManEnvFiledbInfo.getPostManFileContent());
			String str = new String(filebytes, "UTF-8");

			return str;
			/*
			 * FileUtils.writeByteArrayToFile(file1, str.getBytes()); return
			 * file1;
			 */

			/*
			 * OutputStream os = new ByteArrayOutputStream(); try {
			 * os.write(filebytes); os.close(); } catch (IOException e){
			 * log.error("Exception occurred",e)(); }
			 */

		} else {
			throw new ItorixException(new Throwable().getMessage(), "Connector-1002", new Throwable());
		}
	}
}
