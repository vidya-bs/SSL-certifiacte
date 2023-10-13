package com.itorix.apiwiz.marketing.events.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Marketing.User.Events.List")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class UserEvent {
    private String email;
    private String event;
}
