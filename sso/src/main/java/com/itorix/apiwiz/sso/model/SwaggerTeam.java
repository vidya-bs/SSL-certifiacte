package com.itorix.apiwiz.sso.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;


@JsonInclude(JsonInclude.Include.NON_NULL)
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

	@JsonIgnore
	public void updateContact(SwaggerContacts swaggerContact){
		if(contacts != null){
			boolean contains = false;
			for(SwaggerContacts contact : contacts){
				if(swaggerContact.getEmail().equals(contact.getEmail())){
					contact.setRole(swaggerContact.getRole());
					contains = true;
				}
			}
			if(!contains){
				contacts.add(swaggerContact);
			}
		}else
		{
			this.contacts = new ArrayList<SwaggerContacts>();
			contacts.add(swaggerContact);
		}
	}

	@JsonIgnore
	public void removeContact(SwaggerContacts swaggerContact){
		if(this.contacts != null){
			SwaggerContacts removeContact = null;
			for(SwaggerContacts contact : this.contacts){
				if(swaggerContact.getEmail().equals(contact.getEmail()))
					removeContact = contact;
			}
			if(removeContact != null)
				this.contacts.remove(removeContact);
		}
	}

}
