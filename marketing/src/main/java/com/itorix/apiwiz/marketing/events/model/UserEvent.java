package com.itorix.apiwiz.marketing.events.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "Marketing.User.Events.List")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class UserEvent {
    @Indexed
    private String email;
    private String event;
    private String name;
    private String firstName;
    private String lastName;
    private String company;
    private String role;
    private String message;
    private String plan;
    private List<String> interestedFeatures;
    private long cts;
    private long mts;
}