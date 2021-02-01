package com.itorix.apiwiz.marketing.dao;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.util.artifatory.JfrogUtilImpl;
import com.itorix.apiwiz.marketing.events.model.Event;
import com.itorix.apiwiz.marketing.events.model.EventRegistration;

@Component
public class EventsDao {
	
	@Qualifier("masterMongoTemplate")
	@Autowired
	private MongoTemplate masterMongoTemplate;
	
	@Autowired
	JfrogUtilImpl jfrogUtilImpl;

	public String createUpdateEvent(Event event){
		Query query = new Query().addCriteria(Criteria.where("name").is(event.getName()));
		Event dbEvent = masterMongoTemplate.findOne(query, Event.class);
		if(dbEvent != null){
			event.setId(dbEvent.getId());
		}
		else{
			masterMongoTemplate.save(event);
		}
		return event.getId();
	}
	
	public List<Event> getAllEvents(){
		return masterMongoTemplate.findAll(Event.class);
	}
	
	public List<Event> getAllEvents(String status){
		if(status == null)
			return masterMongoTemplate.findAll(Event.class);
		else{
			if(status.equalsIgnoreCase("active")){
				List<Event> events = masterMongoTemplate.findAll(Event.class);
				for(Event event :events){
					if(!event.getStatus().equalsIgnoreCase("active")){
						events.remove(event);
					}
				}
				return events;
			}else if(status.equalsIgnoreCase("expired")){
				List<Event> events = masterMongoTemplate.findAll(Event.class);
				for(Event event :events){
					if(!event.getStatus().equalsIgnoreCase("expired")){
						events.remove(event);
					}
				}
				return events;
			}else
				return masterMongoTemplate.findAll(Event.class);
		}
	}
	
	public Event getEvent(String eventId){
		Query query = new Query().addCriteria(Criteria.where("id").is(eventId));
		return masterMongoTemplate.findOne(query, Event.class);
	}
	
	public void deleteEvent(String eventId){
		Query query = new Query().addCriteria(Criteria.where("id").is(eventId));
		masterMongoTemplate.remove(query, Event.class);
	}
	
	public String updateEventFile(String eventName, String filename, byte[] bytes) throws ItorixException{
		return updateToJfrog(eventName + "/" + filename, bytes);
	}
	
	public void deleteEventFile(String eventName, String imagePath) throws ItorixException{
		 deleteFileJfrogFile("/marketing/events/" + eventName);
	}
	
	public void createRegistration(EventRegistration eventRegistration){
		masterMongoTemplate.save(eventRegistration);
	}
	
	public List<EventRegistration> getEventRegistrations(String eventId){
		Query query = new Query().addCriteria(Criteria.where("eventId").is(eventId));
		List<EventRegistration> registrations = masterMongoTemplate.find(query, EventRegistration.class);
		if(registrations != null)
			return registrations;
		else 
			return new ArrayList<EventRegistration>();
	}
	
	private String updateToJfrog(String folderPath, byte[] bytes) throws ItorixException {
		try {
			JSONObject uploadFiles = jfrogUtilImpl.uploadFiles(new ByteArrayInputStream(bytes), "/marketing/events/" + folderPath);
			return uploadFiles.getString("downloadURI");
		} catch (Exception e) {
			e.printStackTrace();
			throw new ItorixException(ErrorCodes.errorMessage.get("Marketing-2"), "Marketing-2");
		}
	}

	private void deleteFileJfrogFile(String folderPath) throws ItorixException {
		try {
			jfrogUtilImpl.deleteFileIgnore404(folderPath);
		} catch (Exception e) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Marketing-2"), "Marketing-2");
		}
	}

}
