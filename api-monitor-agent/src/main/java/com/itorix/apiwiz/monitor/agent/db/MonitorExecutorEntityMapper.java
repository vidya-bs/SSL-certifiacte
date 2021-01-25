package com.itorix.apiwiz.monitor.agent.db;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

public class MonitorExecutorEntityMapper implements RowMapper<MonitorAgentExecutorEntity> {
	public MonitorAgentExecutorEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
		MonitorAgentExecutorEntity agentExecutorEntity = new MonitorAgentExecutorEntity();
		agentExecutorEntity.setId(rs.getLong("id"));
		agentExecutorEntity.setStatus(rs.getString("status"));
		agentExecutorEntity.setTenant(rs.getString("tenant"));
		agentExecutorEntity.setCollectionId(rs.getString("collection_id"));
		agentExecutorEntity.setSchedulerId(rs.getString("scheduler_id"));
		return agentExecutorEntity;
	}
}