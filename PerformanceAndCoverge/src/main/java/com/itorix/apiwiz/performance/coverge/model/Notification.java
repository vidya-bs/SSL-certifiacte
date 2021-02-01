package com.itorix.apiwiz.performance.coverge.model;

import java.util.List;

public class Notification {
	private List<String> contacts;
	private List<Alert> alert;

	public List<String> getContacts() {
		return contacts;
	}

	public void setContacts(List<String> contacts) {
		this.contacts = contacts;
	}

	public List<Alert> getAlert() {
		return alert;
	}

	public void setAlert(List<Alert> alert) {
		this.alert = alert;
	}

}