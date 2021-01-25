package com.itorix.mockserver.service;


import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.itorix.hyggee.mockserver.client.serialization.model.ExpectationVO;
import com.itorix.mockserver.common.model.GroupVO;
import com.itorix.mockserver.common.model.ItorixException;
import com.mongodb.WriteResult;

@Component("scenarioService")
public class ScenarioService {
	@Autowired
	private MongoTemplate mongoTemplate;
	
	public List<ExpectationVO> getExpectationDTOs() throws ItorixException  {
		List <ExpectationVO> expectationDTOs = new ArrayList<ExpectationVO>();
		try {
			expectationDTOs = mongoTemplate.findAll(ExpectationVO.class);
			return expectationDTOs;
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new ItorixException("","");
		}
	}

	public ExpectationVO getExpectationDTOs(String id) throws ItorixException  {
		try {
			Query query = new Query(Criteria.where("_id").is(id));
			ExpectationVO expectation = mongoTemplate.findOne(query, ExpectationVO.class);
			return expectation;
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new ItorixException("","");
		}
	}

	public boolean deletetExpectation(ExpectationVO group) throws ItorixException  {
		try {
			Query query = new Query(Criteria.where("_id").is(group.getId()));
			WriteResult result = mongoTemplate.remove(query,GroupVO.class);
			return  result.wasAcknowledged();
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new ItorixException("","");
		}
	}

}