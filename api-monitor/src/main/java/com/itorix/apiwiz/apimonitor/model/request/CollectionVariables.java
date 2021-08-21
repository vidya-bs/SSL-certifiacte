package com.itorix.apiwiz.apimonitor.model.request;

import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CollectionVariables {

	String collectionID;
	List<RequestVariable> requests = new ArrayList();
}
