package com.itorix.apiwiz.analytics.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MetricForAdminUser {

	private Long numberOfUsers;
	private Long numberOfLockedUsers;
	private Long numberOfNewUsers;
	private Integer totalTeams;
	private Long numberOfNewTeams;
}
