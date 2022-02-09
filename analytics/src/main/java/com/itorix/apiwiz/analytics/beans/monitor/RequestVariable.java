package com.itorix.apiwiz.analytics.beans.monitor;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RequestVariable {

	String requestId;
	String requestName;
	List<Variable> variables;
}
