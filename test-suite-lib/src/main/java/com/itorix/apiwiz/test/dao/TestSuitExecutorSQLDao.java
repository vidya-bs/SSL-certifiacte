package com.itorix.apiwiz.test.dao;

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
import org.springframework.util.StringUtils;

import com.itorix.apiwiz.test.db.TestExecutorEntity;
import com.itorix.apiwiz.test.db.TestExecutorEntityMapper;
import com.itorix.apiwiz.test.executor.exception.ItorixException;
import com.itorix.apiwiz.test.executor.model.ErrorCodes;

@Component
public class TestSuitExecutorSQLDao {

	private Logger logger = LoggerFactory.getLogger(TestSuitExecutorSQLDao.class);

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Autowired
	private DataSource dataSource;

	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	@PostConstruct
	private void postConstruct() {
		namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
	}

	public List<TestExecutorEntity> getExecutorEntityByColumn(String columnName, Object columnValue, int count) {
		String SQL = "select * from " + TestExecutorEntity.TABLE_NAME + " where " + columnName
				+ " = ? ORDER BY id LIMIT " + count;
		return jdbcTemplate.query(SQL, new Object[] { columnValue }, new TestExecutorEntityMapper());
	}

	public int updateField(Long id, String field, String value) {
		return jdbcTemplate.update("update " + TestExecutorEntity.TABLE_NAME + " set " + field + " = ? where id = ?",
				value, id);
	}

	public void updateStatus(List<String> statuses, String newStatus) {
		MapSqlParameterSource parameters = new MapSqlParameterSource();
		parameters.addValue("status", statuses);
		parameters.addValue("newStatus", newStatus);

		namedParameterJdbcTemplate.update(
				"update " + TestExecutorEntity.TABLE_NAME + " set status = :newStatus WHERE status IN (:status)",
				parameters);

	}

	public void insertIntoTestExecutorEntity(String tenant, String executionId, String status) throws ItorixException {
		if (!StringUtils.hasText(tenant) || !StringUtils.hasText(executionId) || !StringUtils.hasText(status)) {
			logger.error("mandatory parameters missing");
			throw new ItorixException(ErrorCodes.errorMessage.get("TestSuiteAgent-7"), "TestSuiteAgent-7");
		}
		jdbcTemplate.update("insert into " + TestExecutorEntity.TABLE_NAME
				+ " (tenant, testSuiteExecutionId , status) values(?,?,?)", tenant, executionId, status);
	}

	public void updateErrorDescription(Long id, String error) {

		MapSqlParameterSource parameters = new MapSqlParameterSource();
		parameters.addValue("errorDescription", error);
		parameters.addValue("id", id);
		parameters.addValue("status", TestExecutorEntity.STATUSES.COMPLETED.getValue());

		namedParameterJdbcTemplate.update("update " + TestExecutorEntity.TABLE_NAME
				+ " set status = :status , errorDescription = :errorDescription WHERE id = :id", parameters);

	}

	public int updateStatusForTestExecutionId(String id , String value) {
		return jdbcTemplate.update("update " + TestExecutorEntity.TABLE_NAME + " set status = ? where testSuiteExecutionId = ? and status <> 'Cancelled'",
				value, id);
	}

	public String getValueByColumnName(String id, String columnName) {
		String query = "select " + columnName +  " from " + TestExecutorEntity.TABLE_NAME + " where testSuiteExecutionId = ? ";
		Object[] inputs = new Object[] { id };
		return jdbcTemplate.queryForObject(query, inputs, String.class);
	}
}
