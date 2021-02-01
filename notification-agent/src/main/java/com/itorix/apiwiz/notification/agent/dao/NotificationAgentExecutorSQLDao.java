package com.itorix.apiwiz.notification.agent.dao;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import com.itorix.apiwiz.notification.agent.db.NotificationEntityMapper;
import com.itorix.apiwiz.notification.agent.db.NotificationExecutorEntity;
import com.itorix.apiwiz.notification.agent.executor.exception.ItorixException;

@Component
public class NotificationAgentExecutorSQLDao {

	private Logger logger = LoggerFactory.getLogger(NotificationAgentExecutorSQLDao.class);

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Autowired
	private DataSource dataSource;

	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	@PostConstruct
	private void postConstruct() {
		namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
	}

	public int updateField(Long id, String field, String value) {
		return jdbcTemplate.update(
				"update " + NotificationExecutorEntity.TABLE_NAME + " set " + field + " = ? where id = ?", value, id);
	}

	public List<NotificationExecutorEntity> getExecutorEntityByColumn(String columnName, Object columnValue, int count) {
		String SQL = "select * from " + NotificationExecutorEntity.TABLE_NAME + " where " + columnName
				+ " = ? ORDER BY id LIMIT " + count;
		return jdbcTemplate.query(SQL, new Object[] { columnValue }, new NotificationEntityMapper());
	}

	public void updateStatus(List<String> statuses, String newStatus) {
		MapSqlParameterSource parameters = new MapSqlParameterSource();
		parameters.addValue("status", statuses);
		parameters.addValue("newStatus", newStatus);

		namedParameterJdbcTemplate.update("update " + NotificationExecutorEntity.TABLE_NAME
				+ " set status = :newStatus WHERE status IN (:status)", parameters);

	}

	public void insertIntoTestExecutorEntity(String type, String content, String status) throws ItorixException {
		jdbcTemplate.update(
				"insert into " + NotificationExecutorEntity.TABLE_NAME + " (type, content , status) values(?,?,?)",
				type, content, status);
	}
}
