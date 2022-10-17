package com.itorix.apiwiz.marketing.dao;

import com.itorix.apiwiz.common.factory.IntegrationHelper;
import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.util.StorageIntegration;
import com.itorix.apiwiz.common.util.artifatory.JfrogUtilImpl;
import com.itorix.apiwiz.common.util.s3.S3Connection;
import com.itorix.apiwiz.common.util.s3.S3Utils;
import com.itorix.apiwiz.marketing.common.Pagination;
import com.itorix.apiwiz.marketing.events.model.Event;
import com.itorix.apiwiz.marketing.events.model.EventMeta;
import com.itorix.apiwiz.marketing.events.model.EventRegistration;
import com.itorix.apiwiz.marketing.news.model.News;
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

	public String createUpdateEvent(Event event) {
		Query query = new Query().addCriteria(Criteria.where("_id").is(event.getId()));
		Event dbEvent = masterMongoTemplate.findOne(query, Event.class);
		if (dbEvent != null) {
			event.setId(dbEvent.getId());
		}
		masterMongoTemplate.save(event);
		return event.getId();
	}

	public List<Event> getAllEvents() {
		return masterMongoTemplate.findAll(Event.class);
	}

	public List<Event> getAllEvents(String status) {
		if (status == null) {
			return masterMongoTemplate.findAll(Event.class);
		} else {
			if (status.equalsIgnoreCase("active")) {
				Query query = new Query();
				query.with(Sort.by(Direction.DESC, "eventDate"));
				List<Event> events = masterMongoTemplate.find(query, Event.class);
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
				List<Event> events = masterMongoTemplate.findAll(Event.class);
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
			else
				return masterMongoTemplate.findAll(Event.class);
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

	public Pagination getPagination(int offset, int pagesize,int count) {
		Pagination pagination = new Pagination();
		pagination.setOffset(offset);
		pagination.setPageSize(pagesize);
		pagination.setTotal((long)count);
		return pagination;
	}
}
