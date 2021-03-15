package com.itorix.apiwiz.marketing.contactus.model;

import java.util.ArrayList;
import java.util.List;

public class EmailContent {
	private List<String> toMailId;
	private String event;
	private String subject;
	private EmailBody body;
	
	public List<String> getToMailId() {
		if(toMailId == null)
			return new ArrayList<String>();
		return toMailId;
	}
	public void setToMailId(List<String> toMailId) {
		this.toMailId = toMailId;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public EmailBody getBody() {
		return body;
	}
	public void setBody(EmailBody body) {
		this.body = body;
	}
	public String getEvent() {
		return event;
	}
	public void setEvent(String event) {
		this.event = event;
	}
}
