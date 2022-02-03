package com.itorix.apiwiz.analytics.beans.monitor;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Notifications {
	private String type;
	private List<String> emails;
	private List<String> slackChannels;
}
