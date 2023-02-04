package com.itorix.apiwiz.marketing.configuration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;
import org.sqlite.SQLiteOpenMode;

import javax.sql.DataSource;


@Configuration
public class NotificationApiSqlLiteConfig {

  @Value("${datasource.common.notification.url}")
  private String notificationUrl;

  @Bean(name = "notificationDataSource")
  public DataSource notificationDataSource() {
    SQLiteDataSource dataSource = new SQLiteDataSource();
    dataSource.setUrl(notificationUrl);
    dataSource.setDatabaseName("Notification-common");
    SQLiteConfig config = new SQLiteConfig();
    config.setOpenMode(SQLiteOpenMode.FULLMUTEX);
    dataSource.setConfig(config);
    return dataSource;
  }

  @Bean(name = "notificationSQLiteDB")
  public JdbcTemplate notificationJdbcTemplate( @Qualifier("notificationDataSource") DataSource notificationDataSource) {
    return new JdbcTemplate(notificationDataSource);
  }
}