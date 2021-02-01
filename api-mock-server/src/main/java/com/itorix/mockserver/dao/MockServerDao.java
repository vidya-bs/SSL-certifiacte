package com.itorix.mockserver.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Controller;

import com.itorix.mockserver.common.model.Group;
import com.itorix.mockserver.common.model.MockLog;

@Controller
public class MockServerDao {

	@Autowired
	private MongoTemplate mongoTemplate;

	public String getGroupName(String id) {
		Group group = mongoTemplate.findById(id, Group.class);
		if (group != null) {
			return group.getName();
		}
		return null;
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


}
