package com.itorix.apiwiz.analytics.beans.monitor;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CollectionVariables {

	String collectionID;
	List<RequestVariable> requests = new ArrayList();
}
