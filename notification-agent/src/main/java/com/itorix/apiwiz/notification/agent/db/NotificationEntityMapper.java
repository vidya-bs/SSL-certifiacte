package com.itorix.apiwiz.notification.agent.db;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class NotificationEntityMapper implements RowMapper<NotificationExecutorEntity> {
    public NotificationExecutorEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        NotificationExecutorEntity executorEntity = new NotificationExecutorEntity();
        executorEntity.setId(rs.getLong("id"));
        executorEntity.setStatus(rs.getString("status"));
        executorEntity.setType(rs.getString("type"));
        executorEntity.setContent(rs.getString("content"));
        executorEntity.setTenantId(rs.getString("tenantId"));
        return executorEntity;
    }
}