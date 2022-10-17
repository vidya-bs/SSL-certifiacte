package com.itorix.apiwiz.marketing.serviceimpl;

import java.util.Arrays;
import java.util.List;

import com.itorix.apiwiz.marketing.common.PaginatedResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

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
	@UnSecure(ignoreValidation = true)
	public ResponseEntity<?> createEvent(Event event) throws Exception {
		eventsDao.createUpdateEvent(event);
		return new ResponseEntity<>(HttpStatus.CREATED);
	}

	@Override
	@UnSecure(ignoreValidation = true)
	public ResponseEntity<?> updateEvent(Event event, String eventId) throws Exception {
		event.setId(eventId);
		eventsDao.createUpdateEvent(event);
		return ResponseEntity.ok().build();
	}

	@Override
	@UnSecure(ignoreValidation = true)
	public ResponseEntity<?> getAllEvents(String status,int offset,int pagesize) throws Exception {
		List<Event>allEvents = eventsDao.getAllEvents(status);
		PaginatedResponse paginatedResponse = new PaginatedResponse();
		paginatedResponse.setPagination(eventsDao.getPagination(offset,pagesize,allEvents.size()));
		paginatedResponse.setData(allEvents);
		return new ResponseEntity<>(paginatedResponse,HttpStatus.OK);
	}

	@Override
	@UnSecure(ignoreValidation = true)
	public ResponseEntity<?> getEvent(String eventId)
			throws Exception {
		return new ResponseEntity<>(eventsDao.getEvent(eventId), HttpStatus.OK);
	}

	@Override
	@UnSecure(ignoreValidation = true)
	public ResponseEntity<?> deleteEvent(String eventId)
			throws Exception {
		eventsDao.deleteEvent(eventId);
		return ResponseEntity.noContent().build();
	}

	@Override
	@UnSecure(ignoreValidation = true)
	public ResponseEntity<?> registerEvent(String eventId,
			EventRegistration eventRegistration) throws Exception {
		eventRegistration.setEventId(eventId);
		eventsDao.createRegistration(eventRegistration);
		return new ResponseEntity<>(HttpStatus.CREATED);
	}

	@Override
	@UnSecure(ignoreValidation = true)
	public ResponseEntity<?> getEventRegistrations(
			String eventId) throws Exception {
		return new ResponseEntity<>(eventsDao.getEventRegistrations(eventId), HttpStatus.OK);
	}
}
