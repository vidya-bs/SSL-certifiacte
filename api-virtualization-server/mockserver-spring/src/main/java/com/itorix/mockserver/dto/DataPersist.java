package com.itorix.mockserver.dto;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itorix.hyggee.mockserver.client.serialization.ObjectMapperFactory;
import com.itorix.hyggee.mockserver.client.serialization.model.ExpectationDTO;
import com.itorix.hyggee.mockserver.client.serialization.model.ExpectationVO;
import com.itorix.hyggee.mockserver.client.serialization.model.GroupVO;
import com.itorix.hyggee.mockserver.mock.Expectation;
import com.itorix.mockserver.logging.model.MockLog;


@Component("dataPersist")
public class DataPersist extends com.itorix.hyggee.mockserver.mock.DataPersist{
	
	@Value("${log.retention}")
	private int days;
	
	@Autowired
	private MongoTemplate mongoTemplate;

	private ObjectMapper objectMapper = ObjectMapperFactory.createObjectMapper();

	public boolean updateExpectation_old(Expectation expectation) {
		try {
			mongoTemplate.save(expectation);
			return true;
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
		return false;
	}

	public boolean updateExpectation(ExpectationVO expectationDTO, String id) {
		try {
			if(id!= null && id !="") {
				Query query = new Query(Criteria.where("_id").is(id));
				ExpectationVO expectation = mongoTemplate.findOne(query, ExpectationVO.class);
				expectation.setName(expectationDTO.getName());
				expectation.setDTO(expectationDTO.getDTO());
				expectation.setPath(expectationDTO.getPath());
				mongoTemplate.save(expectation);
				return true;
			}else 
			{
				mongoTemplate.save(expectationDTO);
				return true;
			}
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
		return false;
	}

	public List<Expectation> getExpectations()  {
		List <Expectation> expectations = new ArrayList<Expectation>();
		try {
			List<ExpectationVO> listVO = mongoTemplate.findAll(ExpectationVO.class);
			for(ExpectationVO elementVO : listVO) {
				ExpectationDTO expectationDTO = objectMapper.readValue(elementVO.getDTO(), ExpectationDTO.class);
				Expectation expectation = expectationDTO.buildObject();
				expectation.setId(elementVO.getId());
				expectations.add(expectation);
			}
			return expectations;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public List<ExpectationVO> getExpectationDTOs()  {
		List <ExpectationVO> expectationDTOs = new ArrayList<ExpectationVO>();
		try {
			expectationDTOs = mongoTemplate.findAll(ExpectationVO.class);
			return expectationDTOs;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public ExpectationVO getExpectation(String id) {
		ExpectationVO expectation = null;
		try {

			if(id!= null && id !="") {
				Query query = new Query(Criteria.where("_id").is(id));
				expectation = mongoTemplate.findOne(query, ExpectationVO.class);
				return expectation;
			}
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
		return expectation;
	}
	
	public String getExpectationName(String id) {
		try {
			if(id!= null && id !="") {
				Query query = new Query(Criteria.where("_id").is(id));
				ExpectationVO expectation = mongoTemplate.findOne(query, ExpectationVO.class);
				return expectation.getName();
			}
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
		return null;
	}

	public GroupVO getGroup(String id) {
		GroupVO group = null;
		try {
			if(id!= null && id !="") {
				Query query = new Query(Criteria.where("_id").is(id));
				group = mongoTemplate.findOne(query, GroupVO.class);
				return group;
			}
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
		return group;
	}

	public boolean addLogEntry(MockLog mockLog) {
		try {
			mongoTemplate.save(mockLog);
			return true;
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
		return false;
	}

	@Scheduled(cron = "0 0 0 * * ?")
	public void logCleanUpDaily() {
		long time = System.currentTimeMillis() - (1000*60*60*24*days);
		try {
			List<MockLog> logentries = mongoTemplate.findAll(MockLog.class);
			for(MockLog logEntry : logentries)
				if(logEntry.getLoggedTime() >= time) {
					Query query = new Query(Criteria.where("_id").is(logEntry.getId()));
					mongoTemplate.remove(query,MockLog.class);
				}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

}
