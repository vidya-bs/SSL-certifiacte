package com.itorix.apiwiz.marketing.contactus.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ContactUsNotification {

	public enum Type {
		@JsonProperty("EMAIL")
		email, @JsonProperty("SLACK")
		slack;
	}

	private Type type;
	private EmailContent emailContent;

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public EmailContent getEmailContent() {
		return emailContent;
	}

	public void setEmailContent(EmailContent emailContent) {
		this.emailContent = emailContent;
	}
}
