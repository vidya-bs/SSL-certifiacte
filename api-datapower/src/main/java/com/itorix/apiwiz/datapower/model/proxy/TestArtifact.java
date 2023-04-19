package com.itorix.apiwiz.datapower.model.proxy;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class TestArtifact implements Serializable {
	private String id;

	private String environmentId;
}
