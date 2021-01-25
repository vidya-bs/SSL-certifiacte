package com.itorix.apiwiz.test.db;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

public class TestExecutorEntityMapper implements RowMapper<TestExecutorEntity> {
   public TestExecutorEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
      TestExecutorEntity student = new TestExecutorEntity();
      student.setId(rs.getLong("id"));
      student.setStatus(rs.getString("status"));
      student.setTenant(rs.getString("tenant"));
      student.setTestSuiteExecutionId(rs.getString("testSuiteExecutionId"));
      return student;
   }
}