package com.itorix.apiwiz.apimonitor.model.request;

import java.util.List;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RequestVariable {

	String requestId;
	String requestName;
	List<Variable> variables;
}
