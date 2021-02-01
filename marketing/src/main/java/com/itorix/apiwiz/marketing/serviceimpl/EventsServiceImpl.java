package com.itorix.apiwiz.marketing.serviceimpl;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.itorix.apiwiz.identitymanagement.security.annotation.UnSecure;
import com.itorix.apiwiz.marketing.dao.EventsDao;
import com.itorix.apiwiz.marketing.events.model.Event;
import com.itorix.apiwiz.marketing.events.model.EventRegistration;
import com.itorix.apiwiz.marketing.service.EventsService;

@CrossOrigin
@RestController
public class EventsServiceImpl implements EventsService {
	
	@Autowired
	private EventsDao eventsDao;
	
	@Override
	@UnSecure(ignoreValidation=true)
	public ResponseEntity<?> createEvent(String interactionid, String jsessionid, String apikey, MultipartFile image, String name,
			String eventDate, String location, String category, String summary, String description, String bannerImage) throws Exception {
		String filePath = null;
		if(image != null){
			byte[] bytes = image.getBytes();
			String filename = image.getOriginalFilename();
			filePath = eventsDao.updateEventFile(name, filename, bytes);
		}
		Date date=new SimpleDateFormat("MM/dd/yyyy").parse(eventDate); 
		Event event = new Event(name, date, location, category, summary,description, bannerImage);
		event.setImage(filePath);
		eventsDao.createUpdateEvent(event);
		return new ResponseEntity<>(HttpStatus.CREATED);
	}

	@Override
	@UnSecure(ignoreValidation=true)
	public ResponseEntity<?> updateEvent(String interactionid, String jsessionid, String apikey, MultipartFile image, String name,
			String eventDate, String location, String category, String summary, String description, String bannerImage, String eventId) throws Exception {
		String filePath = null;
		if(image != null){
			byte[] bytes = image.getBytes();
			String filename = image.getOriginalFilename();
			filePath = eventsDao.updateEventFile(name, filename, bytes);
		}
		Date date=new SimpleDateFormat("MM/dd/yyyy").parse(eventDate); 
		Event event = new Event(name, date, location, category, summary, description, bannerImage);
		event.setImage(filePath);
		event.setId(eventId);
		eventsDao.createUpdateEvent(event);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	@UnSecure(ignoreValidation=true)
	public ResponseEntity<?> getAllEvents(String interactionid, String jsessionid, String apikey, String status) throws Exception {
		return new ResponseEntity<>(eventsDao.getAllEvents(status), HttpStatus.OK);
	}

	@Override
	@UnSecure(ignoreValidation=true)
	public ResponseEntity<?> getEvent(String interactionid, String jsessionid, String apikey, String eventId) throws Exception {
		return new ResponseEntity<>(eventsDao.getEvent(eventId), HttpStatus.OK);
	}

	@Override
	@UnSecure(ignoreValidation=true)
	public ResponseEntity<?> deleteEvent(String interactionid, String jsessionid, String apikey, String eventId) throws Exception {
		eventsDao.deleteEvent(eventId);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	@UnSecure(ignoreValidation=true)
	public ResponseEntity<?> registerEvent(String interactionid, String jsessionid, String apikey, String eventId, EventRegistration eventRegistration)
			throws Exception {
		eventRegistration.setEventId(eventId);
		eventsDao.createRegistration(eventRegistration);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	@UnSecure(ignoreValidation=true)
	public ResponseEntity<?> getEventRegistrations(String interactionid, String jsessionid, String apikey, String eventId)
			throws Exception {
		return new ResponseEntity<>(eventsDao.getEventRegistrations(eventId), HttpStatus.OK);
	}

}
