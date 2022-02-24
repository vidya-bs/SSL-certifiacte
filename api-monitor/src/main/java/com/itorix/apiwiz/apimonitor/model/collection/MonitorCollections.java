package com.itorix.apiwiz.apimonitor.model.collection;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.itorix.apiwiz.apimonitor.model.request.MonitorRequest;
import com.itorix.apiwiz.common.model.AbstractObject;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component("monitorCollection")
@Document(collection = "Monitor.Collections.List")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Getter
@Setter
public class MonitorCollections extends AbstractObject {
	private String name;
	private String summary;
	private String description;
	private List<Notifications> notifications;
	private List<Schedulers> schedulers = new ArrayList<>();
	private List<MonitorRequest> monitorRequest = new ArrayList<>();
	private List<String> sequence;
}
