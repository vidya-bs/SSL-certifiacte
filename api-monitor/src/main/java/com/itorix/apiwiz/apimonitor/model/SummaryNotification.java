package com.itorix.apiwiz.apimonitor.model;

import java.util.Date;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Component("SummaryNotification")
@Document(collection = "Monitor.Notification.Summary")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class SummaryNotification {
	@JsonProperty("date")
	private Date date;
}
