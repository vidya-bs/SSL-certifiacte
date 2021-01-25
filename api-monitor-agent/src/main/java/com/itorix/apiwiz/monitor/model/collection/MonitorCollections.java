package com.itorix.apiwiz.monitor.model.collection;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.itorix.apiwiz.monitor.model.AbstractObject;
import com.itorix.apiwiz.monitor.model.request.MonitorRequest;

import lombok.Getter;
import lombok.Setter;

@Component("monitorCollection")
@Document(collection = "Monitor.Collections.List")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class MonitorCollections extends AbstractObject {
	private String name;
	private String summary;
	private String description;
	private List<Notifications> notifications;
	private List<Schedulers> schedulers;
	private List<MonitorRequest> monitorRequest ;
	private List<String> sequence;
}
