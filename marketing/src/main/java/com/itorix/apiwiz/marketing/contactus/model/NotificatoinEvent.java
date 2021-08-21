package com.itorix.apiwiz.marketing.contactus.model;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonInclude;

@Document(collection = "Marketing.Contactus.Events")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificatoinEvent {
	@Id
	private String name;
	private String subject;
	private List<String> email;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public List<String> getEmail() {
		return email;
	}

	public void setEmail(List<String> email) {
		this.email = email;
	}
}
