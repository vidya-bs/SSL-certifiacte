package com.itorix.apiwiz.apimonitor.model.stats.logs;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LogEvent {
	public Request request;
    public Response response;
}
