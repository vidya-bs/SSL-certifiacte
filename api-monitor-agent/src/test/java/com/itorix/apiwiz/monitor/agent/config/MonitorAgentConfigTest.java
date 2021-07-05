package com.itorix.apiwiz.monitor.agent.config;

import com.itorix.apiwiz.monitor.agent.dao.MonitorAgentExecutorDao;
import com.itorix.apiwiz.monitor.agent.dao.MonitorAgentExecutorSQLDao;
import com.itorix.apiwiz.monitor.agent.executor.EmailContentParser;
import com.itorix.apiwiz.monitor.agent.executor.MonitorAgentHelper;
import com.itorix.apiwiz.monitor.agent.executor.MonitorAgentRunner;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import java.util.Properties;

@Configuration
public class MonitorAgentConfigTest {

    @Bean
    public static PropertySourcesPlaceholderConfigurer properties() throws Exception {
        final PropertySourcesPlaceholderConfigurer pspc = new PropertySourcesPlaceholderConfigurer();
        Properties properties = new Properties();

        properties.setProperty("notificationAgentPath", "testValue");
        properties.setProperty("http.timeout", "100000");
        properties.setProperty("itorix.notification.agent.contextPath", "/notification");
        pspc.setProperties(properties);
        return pspc;
    }

    @Bean
    public MonitorAgentRunner monitorAgentRunner () {
        return new MonitorAgentRunner();
    }
}
