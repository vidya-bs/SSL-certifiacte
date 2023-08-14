package com.itorix.apiwiz.common.beans;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;
import org.springframework.data.relational.core.dialect.AnsiDialect;
import org.springframework.data.relational.core.dialect.Dialect;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
@ConditionalOnProperty(
        prefix = "postgres",
        name = {"enabled"},
        havingValue = "true",
        matchIfMissing = false
)
public class PostgresConfig extends AbstractJdbcConfiguration {

  @Value("${datasource.url:null}")
  private String url;
  @Value("${datasource.username:null}")
  private String userName;
  @Value("${datasource.password:null}")
  private String password;

  @Bean
  public DataSource dataSource() {
    DriverManagerDataSource driverManagerDataSource = new DriverManagerDataSource();
    driverManagerDataSource.setUrl(url);
    driverManagerDataSource.setUsername(userName);
    driverManagerDataSource.setPassword(password);
    return driverManagerDataSource;
  }
  @Bean
  public JdbcTemplate jdbcTemplate(DataSource dataSource) {
    return new JdbcTemplate(dataSource);
  }

  @Override
  public Dialect jdbcDialect(NamedParameterJdbcOperations operations) {
    return AnsiDialect.INSTANCE;
  }
}
