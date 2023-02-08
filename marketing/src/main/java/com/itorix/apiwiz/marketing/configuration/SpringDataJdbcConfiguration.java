package com.itorix.apiwiz.marketing.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;
import org.springframework.data.relational.core.dialect.AnsiDialect;
import org.springframework.data.relational.core.dialect.Dialect;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;


@Configuration
public class SpringDataJdbcConfiguration extends AbstractJdbcConfiguration {

    @Override
    public Dialect jdbcDialect(NamedParameterJdbcOperations operations) {
        return AnsiDialect.INSTANCE;
    }
}