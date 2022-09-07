package com.itorix.apiwiz.analytics.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MetricsForOperationsUser {
	private Map<String, Integer> releasePackageCountByStatus = new HashMap<>();
	private List<Document> serviceRequestByStatus = new ArrayList<>();
}
