package com.itorix.apiwiz.datapower.model.proxy;

import java.io.Serializable;
import java.util.List;

import lombok.Data;
import org.springframework.data.annotation.Id;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class Proxies implements Serializable {
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
