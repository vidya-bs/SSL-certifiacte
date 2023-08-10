package com.itorix.apiwiz.design.studio.dao;

import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.design.studio.dto.ComplicanceScannerExecutorEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

@Repository
@Slf4j
public class ComplianceScannerSqlDao {

  @Autowired
  @Qualifier("complianceJdbcTemplate")
  JdbcTemplate jdbcTemplate;


  public void insertIntoComplianceExecutorEntity(String tenant, String complianceDetectExecutionId, String status, String lockedBy, String operation)
          throws ItorixException {
    if (!StringUtils.hasText(tenant) || !StringUtils.hasText(complianceDetectExecutionId) ||
            !StringUtils.hasText(status) || !StringUtils.hasText(operation)) {
      log.error("mandatory parameters missing");
      throw new ItorixException(ErrorCodes.errorMessage.get("MonitorAgent-7"), "MonitorAgent-7");
    }
    jdbcTemplate.update(
            "insert into " + ComplicanceScannerExecutorEntity.TABLE_NAME
                    + " (tenant, compliancescannerexecutionid, operation, status, lockedby) values(?,?,?,?,?)",
            tenant, complianceDetectExecutionId, operation, status, lockedBy);
  }

}
