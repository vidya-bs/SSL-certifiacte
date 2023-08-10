package com.itorix.apiwiz.marketing.configuration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;
import org.sqlite.SQLiteOpenMode;

import javax.sql.DataSource;


@Configuration
public class NotificationApiSqlLiteConfig {

  @Value("${datasource.url}")
  private String url;
  @Value("${datasource.username}")
  private String userName;
  @Value("${datasource.password}")
  private String password;

  @Bean(name = "notificationDataSource")
  public DataSource notificationDataSource() {
    DriverManagerDataSource driverManagerDataSource = new DriverManagerDataSource();
    driverManagerDataSource.setUrl(url);
    driverManagerDataSource.setUsername(userName);
    driverManagerDataSource.setPassword(password);
    return driverManagerDataSource;
  }

  @Bean(name = "notificationSQLiteDB")
  public JdbcTemplate notificationJdbcTemplate( @Qualifier("notificationDataSource") DataSource notificationDataSource) {
    return new JdbcTemplate(notificationDataSource);
  }
}