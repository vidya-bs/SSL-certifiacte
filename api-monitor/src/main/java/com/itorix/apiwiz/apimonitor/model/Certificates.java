package com.itorix.apiwiz.apimonitor.model;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.itorix.apiwiz.common.model.AbstractObject;

import lombok.Getter;
import lombok.Setter;

/**
 * The following configuration is used to store customer certificates.
 *
 * @author kishan
 *
 */
@Component("MonitorCertificates")
@Document(collection = "Monitor.SSL.List")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class Certificates extends AbstractObject{

	@JsonProperty("name")
	private String name;

	@JsonProperty("content")
	private byte[] content;

	@JsonProperty("description")
	private String description;

	@JsonProperty("password")
	private String password;

	@JsonProperty("alias")
	private String alias;

}
