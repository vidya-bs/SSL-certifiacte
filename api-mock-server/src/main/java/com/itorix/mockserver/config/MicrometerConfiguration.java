package com.itorix.mockserver.config;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.micrometer.core.instrument.MeterRegistry;


@Configuration
public class MicrometerConfiguration {

	@Bean MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
		return registry -> registry.config();
	}
}