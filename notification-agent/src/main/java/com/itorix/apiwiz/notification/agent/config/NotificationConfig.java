package com.itorix.apiwiz.notification.agent.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;
import org.sqlite.SQLiteOpenMode;

@Configuration
public class NotificationConfig {

	@Value("${datasource.url}")
	private String url;
	
	 @Bean
	 public SQLiteDataSource dataSource() {
	 SQLiteDataSource dataSource = new SQLiteDataSource();
	 dataSource.setUrl(url);
	 SQLiteConfig config = new SQLiteConfig();
	 config.setOpenMode(SQLiteOpenMode.FULLMUTEX);
	 dataSource.setConfig(config);
	 return dataSource;
	 }
}
