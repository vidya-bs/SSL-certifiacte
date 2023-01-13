package com.itorix.apiwiz.data.management.businessimpl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Update;

import com.itorix.apiwiz.common.model.apigee.ApigeeConfigurationVO;
import com.itorix.apiwiz.common.model.apigee.ApigeeIntegrationVO;
import com.itorix.apiwiz.common.model.apigee.ApigeeServiceUser;
import com.itorix.apiwiz.common.model.apigee.CommonConfiguration;
import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.properties.ApplicationProperties;
import com.itorix.apiwiz.common.util.apigee.ApigeeUtil;
import com.itorix.apiwiz.common.util.apigeeX.ApigeeXUtill;
import com.itorix.apiwiz.data.management.business.ApigeeConfigurationBusiness;
import com.itorix.apiwiz.datamanagement.service.ApigeeConfigurationService;
import com.itorix.apiwiz.identitymanagement.dao.BaseRepository;
import org.springframework.web.client.RestClientException;

@Service
public class ApigeeConfigurationBusinessImpl implements ApigeeConfigurationBusiness {

	private static final Logger logger = LoggerFactory.getLogger(ApigeeConfigurationService.class);
	@Autowired
	BaseRepository baseRepository;

	@Autowired
	OrganizationBusinessImpl organizationService;

	@Autowired
	ApigeeUtil apigeeUtil;
	@Autowired
	ApigeeXUtill apigeexUtil;
	@Autowired
	ApplicationProperties applicationProperties;
	@Autowired
	MongoTemplate mongoTemplate;

	/**
	 * getConfiguration
	 *
	 * @param interactionid
	 * @param jsessionid
	 * 
	 * @return
	 */
	public List<ApigeeConfigurationVO> getConfiguration(String interactionid, String jsessionid)
			throws ItorixException {
		logger.debug("ApigeeConfigurationService.getConfiguration : CorelationId=" + interactionid);
		List<ApigeeConfigurationVO> list = baseRepository.findAll(ApigeeConfigurationVO.class);
		logger.debug(
				"ApigeeConfigurationService.getConfiguration : CorelationId=" + interactionid + " : list = " + list);
		return list;
	}
	/*
	 * public List<ApigeeConfigurationVO> getConfiguration(String interactionid,
	 * String jsessionid)throws ItorixException { String userId =
	 * commonServices.getUserId(jsessionid);
	 * 
	 * logger.
	 * debug("ApigeeConfigurationService.getConfiguration : CorelationId=" +
	 * interactionid); List<ApigeeConfigurationVO> list =
	 * baseRepository.find("createdBy", userId, ApigeeConfigurationVO.class);
	 * logger.debug(
	 * "ApigeeConfigurationService.getConfiguration : CorelationId=" +
	 * interactionid + " : list = " + list); return list; }
	 */

	/**
	 * createConfiguration
	 *
	 * @param list
	 * @param interactionid
	 * @param jsessionid
	 * 
	 * @throws ItorixException
	 */
	public void createConfiguration(List<ApigeeConfigurationVO> list, String interactionid, String jsessionid)
			throws ItorixException {
		logger.debug("ApigeeConfigurationService.createConfiguration : CorelationId=" + interactionid + " : =" + list);
		List<ApigeeConfigurationVO> list1 = new ArrayList<>();
		for (ApigeeConfigurationVO vo : list) {
			ApigeeConfigurationVO apigeeConfigurationVO = baseRepository.findOne("type", vo.getType(), "orgname",
					vo.getOrgname(), ApigeeConfigurationVO.class);
			if (apigeeConfigurationVO != null) {
				throw new ItorixException(ErrorCodes.errorMessage.get("Apigee-1004"), "Apigee-1004");
			}
			List<String> environments = getEnvironmentNames(jsessionid, vo.getOrgname(), interactionid, vo.getType(),
					vo.getHostname(), vo.getPort(), vo.getScheme());
			vo.setEnvironments(environments);
			list1.add(vo);
		}
		for (ApigeeConfigurationVO vo : list1) {
			baseRepository.save(vo);
		}
	}

	/**
	 * This method is used to get the list of environments for an organization.
	 *
	 * @param jsessionid
	 * @param organization
	 * @param interactionid
	 * @param type
	 * 
	 * @return
	 * 
	 * @throws ItorixException
	 */
	private List<String> getEnvironmentNames(String jsessionid, String organization, String interactionid, String type,
			String host, String port, String scheme) throws ItorixException {
		logger.debug("ApigeeConfigurationService.getEnvironmentNames : interactionid=" + interactionid + ": jsessionid="
				+ jsessionid + " : organization =" + organization);
		/*
		 * Apigee apigee = getApigeeCredential(jsessionid); if (apigee == null)
		 * { throw new
		 * ItorixException(ErrorCodes.errorMessage.get("Apigee-1007"),
		 * "Apigee-1007"); }
		 */
		List<String> envList = null;
		CommonConfiguration cfg = new CommonConfiguration();
		cfg.setType(type);
		cfg.setOrganization(organization);
		ApigeeServiceUser apigeeServiceUser = apigeeUtil.getApigeeServiceAccount(organization, type);
		cfg.setApigeeEmail(apigeeServiceUser.getUserName());
		cfg.setApigeePassword(apigeeServiceUser.getDecryptedPassword());
		cfg.setApigeeCred(apigeeUtil.getApigeeAuth(organization, type));
		cfg.setInteractionid(interactionid);
		envList = apigeeUtil.getEnvironmentNames(cfg, host, port, scheme);
		return envList;
	}

	/**
	 * updateConfiguration
	 *
	 * @param list
	 * @param interactionid
	 * @param jsessionid
	 * 
	 * @throws ItorixException
	 */
	public void updateConfiguration(List<ApigeeConfigurationVO> list, String interactionid, String jsessionid)
			throws ItorixException {
		logger.debug("ApigeeConfigurationService.updateConfiguration : CorelationId=" + interactionid + " : =" + list);
		List<ApigeeConfigurationVO> list1 = new ArrayList<>();
		for (ApigeeConfigurationVO vo : list) {
			ApigeeConfigurationVO apigeeConfigurationVO = baseRepository.findOne("id", vo.getId(),
					ApigeeConfigurationVO.class);
			if (apigeeConfigurationVO == null) {
				throw new ItorixException(ErrorCodes.errorMessage.get("Apigee-1005"), "Apigee-1005");
			}
			List<String> environments = getEnvironmentNames(jsessionid, vo.getOrgname(), interactionid, vo.getType(),
					vo.getHostname(), vo.getPort(), vo.getScheme());
			vo.setEnvironments(environments);
			list1.add(vo);
		}
		for (ApigeeConfigurationVO vo : list1) {
			baseRepository.save(vo);
		}
	}

	/**
	 * deleteConfiguration
	 *
	 * @param apigeeConfigurationVO
	 * @param interactionid
	 * 
	 * @throws ItorixException
	 */
	public void deleteConfiguration(ApigeeConfigurationVO apigeeConfigurationVO, String interactionid)
			throws ItorixException {
		logger.debug("ApigeeConfigurationService.deleteConfiguration : CorelationId=" + interactionid + " : ="
				+ apigeeConfigurationVO);
		ApigeeConfigurationVO vo = baseRepository.findOne("id", apigeeConfigurationVO.getId(),
				ApigeeConfigurationVO.class);
		if (vo == null) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Apigee-1005"), "Apigee-1005");
		}
		baseRepository.delete(apigeeConfigurationVO.getId(), ApigeeConfigurationVO.class);
	}

	/**
	 * getApigeeHost
	 *
	 * @param type
	 * @param org
	 * 
	 * @return
	 * 
	 * @throws ItorixException
	 */
	public Object getApigeeHost(String type, String org) throws ItorixException {
		return apigeeUtil.getApigeeHost(type, org);
	}

	public Object getApigeeAuthorization(String type, String org) throws ItorixException {
		return apigeeUtil.getApigeeAuth(org, type);
	}

	public Object getApigeexAuthorization(String type, String org) throws Exception {
		return apigeexUtil.getApigeeCredentials(org, type);
	}

	public Object getApigeexHost(String type, String org) throws ItorixException {
		return apigeexUtil.getApigeeHost(org);
	}

	/*
	 * public Apigee getApigeeCredential(String jsessionid) { UserSession
	 * userSessionToken = baseRepository.findById(jsessionid,
	 * UserSession.class); User user =
	 * baseRepository.findById(userSessionToken.getUserId(), User.class); if
	 * (user != null) { Apigee apigee = user.getApigee(); return apigee; } else
	 * { return null; } }
	 */

	@Override
	public void updateServiceAccount(List<ApigeeServiceUser> apigeeServiceUsers) throws ItorixException {
		try {
			for (ApigeeServiceUser apigeeServiceUser : apigeeServiceUsers) {
				Query query = new Query(Criteria.where("orgName").is(apigeeServiceUser.getOrgName()).and("type")
						.is(apigeeServiceUser.getType()));
				Update update = new Update();
				update.set("userName", apigeeServiceUser.getUserName());
				update.set("password", apigeeServiceUser.getPassword());
				update.set("authType", apigeeServiceUser.getAuthType());
				update.set("tokenURL", apigeeServiceUser.getTokenURL());
				update.set("grantType", apigeeServiceUser.getGrantType());
				update.set("basicToken", apigeeServiceUser.getBasicToken());
				mongoTemplate.upsert(query, update, ApigeeServiceUser.class);
			}
		} catch (Exception e) {
			throw new ItorixException(e.getMessage());
		}
	}

	@Override
	public List<ApigeeServiceUser> getServiceAccounts() throws ItorixException {
		try {
			return mongoTemplate.findAll(ApigeeServiceUser.class);
		} catch (Exception e) {
			throw new ItorixException(e.getMessage());
		}
	}

	@Override
	public void createApigeeIntegration(ApigeeIntegrationVO apigeeIntegrationVO) throws ItorixException {
		Query query = new Query(
				new Criteria().andOperator(Criteria.where("orgname").is(apigeeIntegrationVO.getOrgname()),
						Criteria.where("hostname").is(apigeeIntegrationVO.getHostname()),
						Criteria.where("port").is(apigeeIntegrationVO.getPort())));
		ApigeeConfigurationVO apigeeConfigurationVO = mongoTemplate.findOne(query, ApigeeConfigurationVO.class);
		if (apigeeConfigurationVO != null) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Apigee-1004"), "Apigee-1004");
		}
		List<String> environments= new ArrayList<>();
		try {
			environments = getEnvironmentNames(null, apigeeIntegrationVO.getOrgname(), null,
					apigeeIntegrationVO.getType(), apigeeIntegrationVO.getHostname(),
					apigeeIntegrationVO.getPort(),
					apigeeIntegrationVO.getScheme());
		} catch (RestClientException exception) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Apigee-1005"), "Apigee-1005");
		}
		apigeeConfigurationVO.setEnvironments(environments);
		apigeeConfigurationVO = apigeeIntegrationVO.getApigeeConfigObject();
		apigeeConfigurationVO = baseRepository.save(apigeeConfigurationVO);
		apigeeConfigurationVO = mongoTemplate.save(apigeeConfigurationVO);
	}

	@Override
	public void updateApigeeIntegration(ApigeeIntegrationVO apigeeIntegrationVO) throws ItorixException {
		Query query = new Query(new Criteria().andOperator(Criteria.where("id").is(apigeeIntegrationVO.getId())));
		ApigeeConfigurationVO dbApigeeConfigurationVO = mongoTemplate.findOne(query, ApigeeConfigurationVO.class);
		if (dbApigeeConfigurationVO == null) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Apigee-1005"), "Apigee-1005");
		}
		ApigeeConfigurationVO apigeeConfigurationVO = apigeeIntegrationVO.getApigeeConfigObject();
		apigeeConfigurationVO.setId(dbApigeeConfigurationVO.getId());
		apigeeConfigurationVO.setCts(dbApigeeConfigurationVO.getCts());
		apigeeConfigurationVO.setCreatedBy(dbApigeeConfigurationVO.getCreatedBy());
		apigeeConfigurationVO.setCreatedUserName(dbApigeeConfigurationVO.getCreatedUserName());
		apigeeConfigurationVO = baseRepository.save(apigeeConfigurationVO);
		List<String> environments = getEnvironmentNames(null, apigeeIntegrationVO.getOrgname(), null,
				apigeeIntegrationVO.getType(), apigeeIntegrationVO.getHostname(), apigeeIntegrationVO.getPort(),
				apigeeIntegrationVO.getScheme());
		apigeeConfigurationVO.setEnvironments(environments);
		apigeeConfigurationVO = mongoTemplate.save(apigeeConfigurationVO);
	}

	@Override
	public List<ApigeeIntegrationVO> listApigeeIntegrations() throws ItorixException {
		List<ApigeeIntegrationVO> apigeeIntegrations = new ArrayList<ApigeeIntegrationVO>();
		List<ApigeeConfigurationVO> apigeeConfigurations = mongoTemplate.findAll(ApigeeConfigurationVO.class);
		if (apigeeConfigurations != null)
			for (ApigeeConfigurationVO apigeeConfigurationVO : apigeeConfigurations)
				apigeeIntegrations.add(new ApigeeIntegrationVO(apigeeConfigurationVO));
		return apigeeIntegrations;
	}

	@Override
	public ApigeeIntegrationVO getApigeeIntegration(String id) throws ItorixException {
		Query query = new Query(new Criteria().andOperator(Criteria.where("id").is(id)));
		ApigeeConfigurationVO apigeeConfigurationVO = mongoTemplate.findOne(query, ApigeeConfigurationVO.class);
		return new ApigeeIntegrationVO(apigeeConfigurationVO);
	}

	@Override
	public void deleteApigeeIntegration(String id) throws ItorixException {
		Query query = new Query(new Criteria().andOperator(Criteria.where("id").is(id)));
		mongoTemplate.remove(query, ApigeeConfigurationVO.class);
	}
}
