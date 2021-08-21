package com.itorix.apiwiz.common.model.projectmanagement;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.itorix.apiwiz.common.model.AbstractObject;

@Component("project")
@Document(collection = "Plan.Projects")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Project extends AbstractObject {

	public static final String LABEL_NAME = "name";

	private String name;
	private String status;
	private String inProd;
	private String description;
	private String organization;
	private String teamOwner;
	private String ownerEmail;

	private List<Contacts> contacts;
	private List<Proxies> proxies;
	private List<String> swaggers;
	private List<SharedFlow> sharedflow;
	private List<String> teams;

	public String getOrganization() {
		return organization;
	}

	public void setOrganization(String organization) {
		this.organization = organization;
	}

	public String getTeamOwner() {
		return teamOwner;
	}

	public void setTeamOwner(String teamOwner) {
		this.teamOwner = teamOwner;
	}

	public String getOwnerEmail() {
		return ownerEmail;
	}

	public void setOwnerEmail(String ownerEmail) {
		this.ownerEmail = ownerEmail;
	}

	public List<String> getTeams() {
		return teams;
	}

	public void setTeams(List<String> teams) {
		this.teams = teams;
	}

	public List<String> getSwaggers() {
		return swaggers;
	}

	public void setSwaggers(List<String> swaggers) {
		this.swaggers = swaggers;
	}

	public List<SharedFlow> getSharedflow() {
		return sharedflow;
	}

	public void setSharedflow(List<SharedFlow> sharedflow) {
		this.sharedflow = sharedflow;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getInProd() {
		return inProd;
	}

	public void setInProd(String inProd) {
		this.inProd = inProd;
	}

	public List<Contacts> getContacts() {
		return contacts;
	}

	public void setContacts(List<Contacts> contacts) {
		this.contacts = contacts;
	}

	public List<Proxies> getProxies() {
		return proxies;
	}

	public void setProxies(List<Proxies> proxies) {
		this.proxies = proxies;
	}

	@JsonIgnore
	public Proxies getProxyByName(String proxyName) {
		if (this.proxies != null)
			for (Proxies proxy : this.proxies)
				if (proxy.getName().contentEquals(proxyName))
					return proxy;
		return null;
	}

	@Override
	public String toString() {
		return "Project [name=" + name + ", status=" + status + ", inProd=" + inProd + ", description=" + description
				+ ", organization=" + organization + ", teamOwner=" + teamOwner + ", ownerEmail=" + ownerEmail
				+ ", contacts=" + contacts + ", proxies=" + proxies + ", swaggers=" + swaggers + ", sharedflow="
				+ sharedflow + ", teams=" + teams + "]";
	}
}
