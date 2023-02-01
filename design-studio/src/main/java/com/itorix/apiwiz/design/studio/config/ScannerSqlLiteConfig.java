package com.itorix.apiwiz.design.studio.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;
import org.springframework.data.relational.core.dialect.AnsiDialect;
import org.springframework.data.relational.core.dialect.Dialect;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;
import org.sqlite.SQLiteOpenMode;


@Configuration
public class ScannerSqlLiteConfig extends AbstractJdbcConfiguration {

  @Value("${datasource.common.url}")
  private String url;

  @Bean
  public SQLiteDataSource dataSource() {
    SQLiteDataSource dataSource = new SQLiteDataSource();
    dataSource.setUrl(url);
    dataSource.setDatabaseName("compliance");
    SQLiteConfig config = new SQLiteConfig();
    config.setOpenMode(SQLiteOpenMode.FULLMUTEX);
    dataSource.setConfig(config);
    return dataSource;
  }

  @Override
  public Dialect jdbcDialect(NamedParameterJdbcOperations operations) {
    return AnsiDialect.INSTANCE;
  }

}