package com.itorix.apiwiz.datapower.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PipelineResponse implements Serializable {

	String id;
	Projects projects;
}
