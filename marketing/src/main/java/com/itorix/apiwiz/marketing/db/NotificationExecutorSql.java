package com.itorix.apiwiz.marketing.db;

import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class NotificationExecutorSql {

  private final Logger logger = LoggerFactory.getLogger(NotificationExecutorSql.class);

  @Autowired
  @Qualifier("notificationSQLiteDB")
  JdbcTemplate notificationJdbcTemplate;

  public void insertIntoNotificationEntity(String tenant, String notificationExecutionId, String status, String lockedBy)
          throws ItorixException {
    if (!StringUtils.hasText(notificationExecutionId) || !StringUtils.hasText(status)) {
      logger.error("mandatory parameters missing");
      throw new ItorixException(ErrorCodes.errorMessage.get("Notification-Scheduler-1"), "Notification-Scheduler-1");
    }
    notificationJdbcTemplate.update(
            "insert into " + NotificationExecutorEntity.TABLE_NAME
                    + " (tenant, notificationexecutionid , status, lockedby) values(?,?,?,?)",
            tenant, notificationExecutionId, status, lockedBy);
  }
  public void updateNotificationField(String notificationExecutionId, String field, String value) {
    notificationJdbcTemplate.update(
            "update " + NotificationExecutorEntity.TABLE_NAME + " set " + field + " = ? where notificationexecutionid = ?", value, notificationExecutionId);
  }
}
