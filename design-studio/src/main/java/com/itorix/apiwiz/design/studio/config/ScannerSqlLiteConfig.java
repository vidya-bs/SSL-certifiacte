package com.itorix.apiwiz.design.studio.config;

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
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;
import org.sqlite.SQLiteOpenMode;

import javax.sql.DataSource;


@Configuration
public class ScannerSqlLiteConfig {

  @Value("${datasource.url}")
  private String url;
  @Value("${datasource.username}")
  private String userName;
  @Value("${datasource.password}")
  private String password;

  @Bean(name = "complianceDataSource")
  public DataSource dataSource() {
    DriverManagerDataSource driverManagerDataSource = new DriverManagerDataSource();
    driverManagerDataSource.setUrl(url);
    driverManagerDataSource.setUsername(userName);
    driverManagerDataSource.setPassword(password);
    return driverManagerDataSource;
  }

  @Bean(name = "complianceJdbcTemplate")
  public JdbcTemplate commonJdbcTemplate(@Qualifier("complianceDataSource") DataSource dataSource) {
    return new JdbcTemplate(dataSource);
  }

}