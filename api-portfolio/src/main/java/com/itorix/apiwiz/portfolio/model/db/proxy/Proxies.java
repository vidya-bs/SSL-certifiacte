package com.itorix.apiwiz.portfolio.model.db.proxy;

import java.util.List;

import org.springframework.data.annotation.Id;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class Proxies {
	@Id
	private String id;
	private String name;

	private String summary;

	private String proxyVersion;

	private List<String> basePaths;

	private boolean deprecate;

	private String gwProvider;

	private ApigeeConfig apigeeConfig;
}
