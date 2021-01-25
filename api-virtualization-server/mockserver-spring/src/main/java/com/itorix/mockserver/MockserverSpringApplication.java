package com.itorix.mockserver;

import org.apache.coyote.AbstractProtocol;
import org.apache.coyote.ProtocolHandler;
import org.apache.coyote.http11.AbstractHttp11Protocol;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@SpringBootApplication
@ComponentScan
@EnableScheduling
@PropertySource(value = "file:${config.properties}", ignoreResourceNotFound = true)
public class MockserverSpringApplication {

	public static void main(String[] args) {
		SpringApplication.run(MockserverSpringApplication.class, args);
	}
	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurerAdapter() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/**")
				.allowedMethods("PUT", "DELETE","OPTIONS")
				.allowCredentials(false).maxAge(3600);
			}
		};
	}

	@SuppressWarnings({ "rawtypes", "deprecation" })
	@Bean
	public EmbeddedServletContainerCustomizer containerCustomizer() {
		return container -> {
			if (container instanceof TomcatEmbeddedServletContainerFactory) {
				TomcatEmbeddedServletContainerFactory tomcat = (TomcatEmbeddedServletContainerFactory)container;
				tomcat.addConnectorCustomizers((TomcatConnectorCustomizer)connector -> {
					ProtocolHandler protocolHandler = connector.getProtocolHandler();
					if (protocolHandler != null && protocolHandler instanceof AbstractHttp11Protocol) {
						AbstractProtocol protocol = (AbstractProtocol)protocolHandler;
						// fix tomcat 8.5 can't send reason phrase
						protocol.setSendReasonPhrase(true);
					}
				});
			}
		};
	}
}
