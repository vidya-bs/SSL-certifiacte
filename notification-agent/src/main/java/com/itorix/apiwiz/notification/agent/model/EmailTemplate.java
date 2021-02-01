package com.itorix.apiwiz.notification.agent.model;

import java.util.List;

public class EmailTemplate {

	public List<String> toMailId;

	public List<String> bccMailId;

	public String subject;

	public String body;

	public String fotter;

	public List<String> getToMailId() {
		return toMailId;
	}

	public void setToMailId(List<String> toMailId) {
		this.toMailId = toMailId;
	}

	public List<String> getBccMailId() {
		return bccMailId;
	}

	public void setBccMailId(List<String> bccMailId) {
		this.bccMailId = bccMailId;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getFotter() {
		return fotter;
	}

	public void setFotter(String fotter) {
		this.fotter = fotter;
	}

}
