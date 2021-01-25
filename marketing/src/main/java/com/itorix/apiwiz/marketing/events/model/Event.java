package com.itorix.apiwiz.marketing.events.model;

import java.util.Date;

import org.apache.commons.lang.time.DateFormatUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonInclude;

@Document(collection = "Marketing.Events.List")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Event {
	@Id
	private String id;
	private String name;
	private Date eventDate;
	private String location;
	private String category;
	private String summary;
	private String description;
	private String image;
	public Event(){
		super();
	}
	public Event(String name, Date eventDate, String location, String category, String summary, String description) {
		super();
		this.name = name;
		this.eventDate = eventDate;
		this.location = location;
		this.category = category;
		this.summary = summary;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEventDate() {
		if(eventDate != null)
			return DateFormatUtils.format(eventDate, "MM/dd/yyyy");
		return null;
	}
	public void setEventDate(Date eventDate) {
		this.eventDate = eventDate;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getSummary() {
		return summary;
	}
	public void setSummary(String summary) {
		this.summary = summary;
	}
	public String getImage() {
		return image;
	}
	public void setImage(String image) {
		this.image = image;
	}
	public String getStatus() {
		if(eventDate!= null){
			Date CurrentDate = new Date();
			if(CurrentDate.compareTo(this.eventDate) < 0)
				return "active";
		}
		return "expired";
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
}