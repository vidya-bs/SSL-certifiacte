package com.itorix.apiwiz.data.management.dao;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.itorix.apiwiz.common.model.apigeeX.ApigeeXConfigurationVO;
import com.itorix.apiwiz.common.model.apigeeX.ApigeeXEnvironment;
import com.itorix.apiwiz.common.util.apigeeX.ApigeeXUtill;
import com.itorix.apiwiz.identitymanagement.dao.BaseRepository;

@Component
public class ApigeeXIntegrationDAO {

	@Autowired
	private BaseRepository baseRepository;
	
	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Autowired
	private ApigeeXUtill apigeeXUtill;
	
	public void saveJSONKey(ApigeeXConfigurationVO apigeeXConfigurationVO){
		
		baseRepository.save(apigeeXConfigurationVO);
	}
	
	public ApigeeXConfigurationVO poplulateEnvironments(ApigeeXConfigurationVO apigeeXConfigurationVO) throws Exception{
		List<String> envs = apigeeXUtill.getEnveronments(apigeeXConfigurationVO.getOrgName(), apigeeXConfigurationVO.getJsonKey());
		List<ApigeeXEnvironment> evironments = new ArrayList<>();
		for(String env : envs){
			ApigeeXEnvironment apigeeXEnvironment = new ApigeeXEnvironment();
			apigeeXEnvironment.setName(env);
			evironments.add(apigeeXEnvironment);
		}
		apigeeXConfigurationVO.setEvironments(evironments);
		return apigeeXConfigurationVO;
	}
	
	public ApigeeXConfigurationVO getConfiguration(String org){
		Query query = new Query();
		query.addCriteria(Criteria.where("orgName").is(org));
		ApigeeXConfigurationVO dbIntegrations = mongoTemplate.findOne(query, ApigeeXConfigurationVO.class);
		return dbIntegrations;
	}
	
	public List<ApigeeXConfigurationVO> getConfigurations(){
		List<ApigeeXConfigurationVO> dbIntegrations = mongoTemplate.findAll(ApigeeXConfigurationVO.class);
		return dbIntegrations;
	}
	
	public void deleteConfiguration(String id){
		Query query = new Query();
		query.addCriteria(Criteria.where("id").is(id));
		mongoTemplate.remove(query, ApigeeXConfigurationVO.class);
	}
	
	public ApigeeXConfigurationVO updateKVM(String org, ApigeeXEnvironment environment){
		ApigeeXConfigurationVO apigeeXConfigurationVO = getConfiguration(org);
		for(ApigeeXEnvironment env : apigeeXConfigurationVO.getEvironments()){
			if(env.getName().equals(environment.getName())){
				env.setKvmProxy(environment.getKvmProxy());
				env.setKvmProxyEndpoint(environment.getKvmProxyEndpoint());
				env.setKvmProxyKey(environment.getKvmProxyKey());
			}
		}
		apigeeXConfigurationVO = baseRepository.save(apigeeXConfigurationVO);
		return apigeeXConfigurationVO;
	}
}
