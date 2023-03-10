package com.itorix.apiwiz.design.studio.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;
import org.springframework.data.relational.core.dialect.AnsiDialect;
import org.springframework.data.relational.core.dialect.Dialect;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;
import org.sqlite.SQLiteOpenMode;

import javax.sql.DataSource;


@Configuration
public class ScannerSqlLiteConfig {

  @Value("${datasource.common.url}")
  private String url;

  @Bean(name = "complianceDataSource")
  public SQLiteDataSource dataSource() {
    SQLiteDataSource dataSource = new SQLiteDataSource();
    dataSource.setUrl(url);
    dataSource.setDatabaseName("compliance");
    SQLiteConfig config = new SQLiteConfig();
    config.setOpenMode(SQLiteOpenMode.FULLMUTEX);
    dataSource.setConfig(config);
    return dataSource;
  }

  @Bean(name = "complianceJdbcTemplate")
  public JdbcTemplate commonJdbcTemplate(@Qualifier("complianceDataSource") DataSource commonDataSource) {
    return new JdbcTemplate(commonDataSource);
  }

}