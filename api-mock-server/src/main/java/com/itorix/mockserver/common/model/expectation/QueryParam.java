package com.itorix.mockserver.common.model.expectation;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
class QueryParam {
	Name name;
	Value value;
}
