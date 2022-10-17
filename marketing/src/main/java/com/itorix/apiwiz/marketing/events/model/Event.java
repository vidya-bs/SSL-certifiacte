package com.itorix.apiwiz.marketing.events.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Marketing.Events.List")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class Event {
	@Id
	private String id;
	@JsonProperty("meta")
	private EventMeta meta;
	private Object content;

}
