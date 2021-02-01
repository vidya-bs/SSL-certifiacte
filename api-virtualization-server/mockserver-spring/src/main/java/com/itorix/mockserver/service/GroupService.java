package com.itorix.mockserver.service;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import com.itorix.mockserver.common.model.GroupVO;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;

@Component("groupService")
public class GroupService {

	@Autowired
	private MongoTemplate mongoTemplate;

	public List<GroupVO> getGroups()  {
		try {
			List<GroupVO> listVO = mongoTemplate.findAll(GroupVO.class);
			return listVO;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public GroupVO getGroup(GroupVO group)  {
		try {
			if(group!=null) {
				Query query = new Query(Criteria.where("_id").is(group.getId()));
				return mongoTemplate.findOne(query, GroupVO.class);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public boolean saveGroup(GroupVO group)  {
		try {
			mongoTemplate.save(group);
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}

	public boolean updateGroup(GroupVO group)  {
		try {
			Query query = new Query(Criteria.where("_id").is(group.getId()));
			DBObject dbDoc = new BasicDBObject();
			mongoTemplate.getConverter().write(group, dbDoc);
			Update update = Update.fromDBObject(dbDoc, "_id");
			WriteResult result = mongoTemplate.updateFirst(query, update, GroupVO.class);
			return  result.wasAcknowledged();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}

	public boolean deleteGroup(GroupVO group)  {
		try {
			Query query = new Query(Criteria.where("_id").is(group.getId()));
			WriteResult result = mongoTemplate.remove(query,GroupVO.class);
			return  result.wasAcknowledged();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}

}
