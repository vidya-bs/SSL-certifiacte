package com.itorix.apiwiz.design.studio.model;

import java.util.Set;
import java.util.HashSet;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.itorix.apiwiz.common.model.AbstractObject;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Component;


@Component("subscribers")
@Document(collection = "Design.Subscribers.List")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)

public class SwaggerSubscription extends AbstractObject {

	@JsonProperty("swaggerName")
	String swaggerName;
	
	@JsonProperty("oas")
	String oas;
	
	@JsonProperty("swaggerId")
	String swaggerId;
	
	@JsonProperty("subscribers")
	Set<Subscriber> subscribers = new HashSet<Subscriber>();
	
	public void setSubscribers(Subscriber subscriber) {
		subscribers.add(subscriber);
	}
	
	public Set<Subscriber> getSubscribers() {
		Set<Subscriber> swaggerSubscribers = new HashSet<Subscriber>();
		for (Subscriber subscriber : subscribers) {
			Subscriber temp = new Subscriber();
			temp.setName(subscriber.getName());
			temp.setEmailId(subscriber.getEmailId());
			swaggerSubscribers.add(temp);
		}
		return swaggerSubscribers;
	}
	
	public void removeSubscribers(String emailId) {
		subscribers.removeIf(subscriber -> (subscriber.getEmailId().equals(emailId)));
	}
}