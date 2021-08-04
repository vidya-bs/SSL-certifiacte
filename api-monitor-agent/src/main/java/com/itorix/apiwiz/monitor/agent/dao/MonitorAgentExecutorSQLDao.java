package com.itorix.apiwiz.monitor.agent.dao;

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

import com.itorix.apiwiz.monitor.agent.db.MonitorAgentExecutorEntity;
import com.itorix.apiwiz.monitor.agent.db.MonitorExecutorEntityMapper;
import com.itorix.apiwiz.monitor.agent.executor.exception.ItorixException;
import com.itorix.apiwiz.monitor.agent.executor.model.ErrorCodes;

@Component
public class MonitorAgentExecutorSQLDao {

    private Logger logger = LoggerFactory.getLogger(MonitorAgentExecutorSQLDao.class);

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    private DataSource dataSource;

    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @PostConstruct
    private void postConstruct() {
        namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    public List<MonitorAgentExecutorEntity> getExecutorEntityByColumn(String columnName, Object columnValue,
            int count) {
        String SQL = "select * from " + MonitorAgentExecutorEntity.TABLE_NAME + " where " + columnName
                + " = ? ORDER BY id LIMIT " + count;
        return jdbcTemplate.query(SQL, new Object[] { columnValue }, new MonitorExecutorEntityMapper());
    }

    public int updateField(Long id, String field, String value) {
        return jdbcTemplate.update(
                "update " + MonitorAgentExecutorEntity.TABLE_NAME + " set " + field + " = ? where id = ?", value, id);
    }

    public void updateStatus(List<String> statuses, String newStatus) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("status", statuses);
        parameters.addValue("newStatus", newStatus);

        namedParameterJdbcTemplate.update("update " + MonitorAgentExecutorEntity.TABLE_NAME
                + " set status = :newStatus WHERE status IN (:status)", parameters);

    }

    public void insertIntoTestExecutorEntity(String tenant, String collectionId, String schedulerId, String status)
            throws ItorixException {
        if (!StringUtils.hasText(tenant) || !StringUtils.hasText(collectionId) || !StringUtils.hasText(status)
                || !StringUtils.hasText(schedulerId)) {
            logger.error("mandatory parameters missing");
            throw new ItorixException(ErrorCodes.errorMessage.get("MonitorAgent-7"), "MonitorAgent-7");
        }
        jdbcTemplate.update(
                "insert into " + MonitorAgentExecutorEntity.TABLE_NAME
                        + " (tenant, collection_id , scheduler_id , status) values(?,?,?,?)",
                tenant, collectionId, schedulerId, status);
    }

    public void updateErrorDescription(Long id, String error) {

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("errorDescription", error);
        parameters.addValue("id", id);
        parameters.addValue("status", MonitorAgentExecutorEntity.STATUSES.COMPLETED.getValue());

        namedParameterJdbcTemplate.update("update " + MonitorAgentExecutorEntity.TABLE_NAME
                + " set status = :status , errorDescription = :errorDescription WHERE id = :id", parameters);

    }

    public int updateStatusForTestExecutionId(long id, String value) {
        return jdbcTemplate.update("update " + MonitorAgentExecutorEntity.TABLE_NAME + " set status = ? where id = ?",
                value, id);
    }

    public String getValueByColumnName(String id, String columnName) {
        String query = "select " + columnName + " from " + MonitorAgentExecutorEntity.TABLE_NAME
                + " where testSuiteExecutionId = ? ";
        Object[] inputs = new Object[] { id };
        return jdbcTemplate.queryForObject(query, inputs, String.class);
    }
}
