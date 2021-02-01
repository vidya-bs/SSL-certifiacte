package com.itorix.apiwiz.design.studio.model;

import java.util.List;
import java.util.Set;

import org.springframework.data.mongodb.core.mapping.Document;

import com.itorix.apiwiz.identitymanagement.model.AbstractObject;



@Document(collection = "Design.Swagger.Teams")
public class SwaggerTeam extends AbstractObject {
	private String name;
	private String displayName;
	private String discription;
	private List<SwaggerContacts> contacts;
	private Set<String> swaggers;
	private Set<String> swagger3;
	private Set<String> projects;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public String getDiscription() {
		return discription;
	}
	public void setDiscription(String discription) {
		this.discription = discription;
	}
	public List<SwaggerContacts> getContacts() {
		return contacts;
	}
	public void setContacts(List<SwaggerContacts> contacts) {
		this.contacts = contacts;
	}
	public Set<String> getSwaggers() {
		return swaggers;
	}
	public void setSwaggers(Set<String> swaggers) {
		this.swaggers = swaggers;
	}
	public Set<String> getProjects() {
		return projects;
	}
	public void setProjects(Set<String> projects) {
		this.projects = projects;
	}
	public Set<String> getSwagger3() {
		return swagger3;
	}
	public void setSwagger3(Set<String> swagger3) {
		this.swagger3 = swagger3;
	}

}
