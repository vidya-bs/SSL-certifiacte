package com.itorix.apiwiz.common.model.apigee;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Environment {
	private Revision[] revision;

	private String name;

	private String state;

	private Server[] server;

	private Configuration configuration;

	public Revision[] getRevision() {
		return revision;
	}

	public void setRevision(Revision[] revision) {
		this.revision = revision;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public Server[] getServer() {
		return server;
	}

	public void setServer(Server[] server) {
		this.server = server;
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}
}
