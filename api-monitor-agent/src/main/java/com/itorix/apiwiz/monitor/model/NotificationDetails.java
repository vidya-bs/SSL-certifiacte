package com.itorix.apiwiz.monitor.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.itorix.apiwiz.monitor.model.collection.Notifications;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationDetails {
	private String WorkspaceName;
	private String Collectionname;
	private String environmentName;
	private String date;
	private String schedulerId;
	private long dailyLatency;
	private int dailyUptime;
	private long avgLatency;
	private int avgUptime;

	@JsonIgnore
	private List<Notifications> notifications;
}