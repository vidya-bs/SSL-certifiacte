package com.itorix.apiwiz.data.management.model;

import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.itorix.apiwiz.common.model.apigee.CommonConfiguration;
import com.itorix.apiwiz.identitymanagement.model.AbstractObject;

@Document(collection = "DataBackUpEvent")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BackupEvent extends AbstractObject {
	private CommonConfiguration cfg;

	private String event;
	private String eventId;
	private String status;
	private boolean delete = false;

	public CommonConfiguration getCfg() {
		return cfg;
	}
	public void setCfg(CommonConfiguration cfg) {
		this.cfg = cfg;
	}
	public String getEvent() {
		return event;
	}
	public void setEvent(String event) {
		this.event = event;
	}
	public String getEventId() {
		return eventId;
	}
	public void setEventId(String eventId) {
		this.eventId = eventId;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public boolean getDelete() {
		return delete;
	}
	public void setDelete(boolean delete) {
		this.delete = delete;
	}

}
