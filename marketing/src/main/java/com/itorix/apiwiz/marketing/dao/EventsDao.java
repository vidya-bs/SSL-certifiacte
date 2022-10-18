package com.itorix.apiwiz.marketing.dao;

import com.itorix.apiwiz.common.factory.IntegrationHelper;
import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.util.StorageIntegration;
import com.itorix.apiwiz.common.util.artifatory.JfrogUtilImpl;
import com.itorix.apiwiz.common.util.s3.S3Connection;
import com.itorix.apiwiz.common.util.s3.S3Utils;
import com.itorix.apiwiz.identitymanagement.model.Pagination;
import com.itorix.apiwiz.marketing.events.model.Event;
import com.itorix.apiwiz.marketing.events.model.EventRegistration;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Component
@Slf4j
public class EventsDao {
	private static final Logger logger = LoggerFactory.getLogger(EventsDao.class);
	@Qualifier("masterMongoTemplate")
	@Autowired
	private MongoTemplate masterMongoTemplate;

	@Autowired
	JfrogUtilImpl jfrogUtilImpl;

	@Autowired
	private S3Connection s3Connection;

	@Autowired
	private S3Utils s3Utils;

	@Autowired
	private IntegrationHelper integrationHelper;

	public String createEvent(Event event) {
		log.info("Events {}",event);
		List<Event> allEvents = getAllEvents();
		String title = event.getMeta().getTitle();
		String slug = title.toLowerCase().replace(" ", "-").replace(":","-");
		if(allEvents.stream().anyMatch(r->r.getMeta().getSlug().equals(slug))){
			return null;
		}
		event.getMeta().setSlug(slug);
		event.setCts(System.currentTimeMillis());
		masterMongoTemplate.save(event);
		return event.getId();
	}

	public List<Event> getAllEvents() {
		return masterMongoTemplate.findAll(Event.class);
	}

	public List<Event> getAllEvents(int offset,int pagesize,String status) {
		if (status == null) {
			Query query = new Query().with(Sort.by(Sort.Direction.ASC, "meta.eventDate"))
					.skip(offset > 0 ? ((offset - 1) * pagesize) : 0).limit(pagesize);
			return masterMongoTemplate.find(query,Event.class);
		} else {
			if (status.equalsIgnoreCase("active")) {
				Query query = new Query();
				query.with(Sort.by(Direction.DESC, "meta.eventDate"))
						.skip(offset > 0 ? ((offset - 1) * pagesize) : 0).limit(pagesize);
				List<Event> events = masterMongoTemplate.find(query, Event.class);
				log.debug("Active Events {}",events);
				CollectionUtils.filter(events, o -> {
					try {
						return ((Event) o).getMeta().getStatus().equalsIgnoreCase("active");
					} catch (ParseException e) {
						throw new RuntimeException(e);
					}
				});

				events.sort(Comparator.comparing(o -> {
					try {
						return o.getMeta().getEventDateOn();
					} catch (ParseException e) {
						throw new RuntimeException(e);
					}
				}));
				Collections.reverse(events);
				return events;
			}
			else if (status.equalsIgnoreCase("expired")) {
				Query query = new Query().with(Sort.by(Sort.Direction.ASC, "cts"))
						.skip(offset > 0 ? ((offset - 1) * pagesize) : 0).limit(pagesize);
				List<Event> events = masterMongoTemplate.find(query,Event.class);
				log.debug("Expired events {}",events);
				CollectionUtils.filter(events, o -> {
					try {
						return ((Event) o).getMeta().getStatus().equalsIgnoreCase("expired");
					} catch (ParseException e) {
						throw new RuntimeException(e);
					}
				});
				events.sort(Comparator.comparing(o -> {
					try {
						return o.getMeta().getEventDateOn();
					} catch (ParseException e) {
						throw new RuntimeException(e);
					}
				}));
				Collections.reverse(events);
				return events;
			}
			else{
				Query query = new Query().with(Sort.by(Sort.Direction.ASC, "meta.eventDate"))
						.skip(offset > 0 ? ((offset - 1) * pagesize) : 0).limit(pagesize);
				return masterMongoTemplate.find(query,Event.class);
			}

		}
	}

	public Event getEvent(String eventId) {
		Query query = new Query().addCriteria(Criteria.where("id").is(eventId));
		return masterMongoTemplate.findOne(query, Event.class);
	}

	public void deleteEvent(String eventId) {
		Query query = new Query().addCriteria(Criteria.where("id").is(eventId));
		masterMongoTemplate.remove(query, Event.class);
	}

	public String updateEventFile(String eventName, String filename, byte[] bytes) throws ItorixException {
		return updateToJfrog(eventName + "/" + filename, bytes);
	}

	public void deleteEventFile(String eventName, String imagePath) throws ItorixException {
		deleteFileJfrogFile("/marketing/events/" + eventName);
	}

	public void createRegistration(EventRegistration eventRegistration) {
		masterMongoTemplate.save(eventRegistration);
	}

	public List<EventRegistration> getEventRegistrations(String eventId) {
		Query query = new Query().addCriteria(Criteria.where("eventId").is(eventId));
		List<EventRegistration> registrations = masterMongoTemplate.find(query, EventRegistration.class);
		if (registrations != null)
			return registrations;
		else
			return new ArrayList<EventRegistration>();
	}

	private String updateToJfrog(String folderPath, byte[] bytes) throws ItorixException {
		try {
			String downloadURI = null;
			StorageIntegration storageIntegration = integrationHelper.getIntegration();
			downloadURI = storageIntegration.uploadFile("marketing/events/" + folderPath, new ByteArrayInputStream(bytes));
			return downloadURI;
		} catch (Exception e) {
			logger.error("Exception occurred", e);
			throw new ItorixException(ErrorCodes.errorMessage.get("Marketing-1000"), "Marketing-1000");
		}
	}

	private void deleteFileJfrogFile(String folderPath) throws ItorixException {
		try {
			jfrogUtilImpl.deleteFileIgnore404(folderPath);
		} catch (Exception e) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Marketing-1000"), "Marketing-1000");
		}
	}

	public Pagination getPagination(int offset, int pagesize, int count) {
		Pagination pagination = new Pagination();
		pagination.setOffset(offset);
		pagination.setPageSize(pagesize);
		pagination.setTotal((long)count);
		return pagination;
	}

	public String updateEvent(Event event) {
		log.info("Event {}",event);
		Query query = new Query().addCriteria(Criteria.where("_id").is(event.getId()));
		Event dbEvent = masterMongoTemplate.findOne(query, Event.class);
		log.debug("Existing Event {}",event);
		if (dbEvent != null) {
			event.setMts(System.currentTimeMillis());
			event.getMeta().setSlug(dbEvent.getMeta().getSlug());
		}
		masterMongoTemplate.save(event);
		return event.getId();
	}
}
