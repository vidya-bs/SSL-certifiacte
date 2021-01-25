
package com.itorix.apiwiz.apimonitor.model.collection;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Notifications {
	private String type;
	private List<String> emails;
	private List<String> slackChannels;
}
