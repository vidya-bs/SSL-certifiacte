package com.itorix.mockserver.common.model.expectation;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Request {

	Method method;
	Path path;
	@JsonProperty("queryParams")
	List<NameMultiValue> queryParams = new ArrayList<>();;
	@JsonProperty("headers")
	List<NameMultiValue> headers = new ArrayList<>();;
	Body body;
	List<Variable> variables = new ArrayList<>();
	@JsonProperty("cookies")
	List<NameSingleValue> cookies = new ArrayList<>();;
}
