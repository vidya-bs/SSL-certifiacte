package com.itorix.apiwiz.devportal.model;

import io.swagger.models.properties.Property;

public class ElProperty {

	private String el;

	private Property property;

	public Property getProperty() {
		return property;
	}

	public void setProperty(Property property) {
		this.property = property;
	}

	public String getEl() {
		return el;
	}

	public void setEl(String el) {
		this.el = el;
	}

}
