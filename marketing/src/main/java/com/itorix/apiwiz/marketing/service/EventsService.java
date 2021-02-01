package com.itorix.apiwiz.marketing.service;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.itorix.apiwiz.identitymanagement.security.annotation.UnSecure;
import com.itorix.apiwiz.marketing.events.model.EventRegistration;

@CrossOrigin
@RestController
public interface EventsService {
	
	@UnSecure(useUpdateKey=true)
	@RequestMapping(method = RequestMethod.POST, value = "/v1/marketing/events", 
			produces = {MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<?> createEvent(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID", required=false) String jsessionid,
			@RequestHeader(value="x-apikey")String apikey,
			@RequestParam(value = "image",required = false ) MultipartFile image,
			@RequestParam("name") String name, 
			@RequestParam("eventDate") String eventDate, 
			@RequestParam("location") String location,
			@RequestParam("category") String category, 
			@RequestParam("summary") String summary,
			@RequestParam("description") String description,
			@RequestParam("bannerImage") String bannerImage) throws Exception;
	
	@UnSecure(useUpdateKey=true)
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/marketing/events/{eventId}")
	public ResponseEntity<?> updateEvent(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID", required=false) String jsessionid,
			@RequestHeader(value="x-apikey")String apikey,
			@RequestParam(value = "image",required = false ) MultipartFile image,
			@RequestParam("name") String name, 
			@RequestParam("eventDate") String eventDate, 
			@RequestParam("location") String location,
			@RequestParam("category") String category, 
			@RequestParam("summary") String summary,
			@RequestParam("description") String description,
			@RequestParam("bannerImage") String bannerImage,
			@PathVariable("eventId") String eventId) throws Exception;
	
	@UnSecure(ignoreValidation=true)
	@RequestMapping(method = RequestMethod.GET, value = "/v1/marketing/events")
	public ResponseEntity<?> getAllEvents(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID", required=false) String jsessionid,
			@RequestHeader(value="x-apikey")String apikey,
			@RequestParam(value = "status", required = false) String status) throws Exception ;
	
	@UnSecure(ignoreValidation=true)
	@RequestMapping(method = RequestMethod.GET, value = "/v1/marketing/events/{eventId}")
	public ResponseEntity<?> getEvent(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID", required=false) String jsessionid,
			@RequestHeader(value="x-apikey")String apikey,
			@PathVariable("eventId") String eventId)
			throws Exception ;
	
	@UnSecure(useUpdateKey=true)
	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/marketing/events/{eventId}")
	public ResponseEntity<?> deleteEvent(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID", required=false) String jsessionid,
			@RequestHeader(value="x-apikey")String apikey, 
			@PathVariable("eventId") String eventId)throws Exception;
	
	@UnSecure(ignoreValidation=true)
	@RequestMapping(method = RequestMethod.POST, value = "/v1/marketing/events-register/{eventId}")
	public ResponseEntity<?> registerEvent(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID", required=false) String jsessionid,
			@RequestHeader(value="x-apikey")String apikey,
			@PathVariable("eventId") String eventId,
			@RequestBody EventRegistration eventRegistration) throws Exception ;
	
	@UnSecure(ignoreValidation=true)
	@RequestMapping(method = RequestMethod.GET, value = "/v1/marketing/events-register/{eventId}")
	public ResponseEntity<?> getEventRegistrations(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID", required=false) String jsessionid,
			@RequestHeader(value="x-apikey")String apikey, 
			@PathVariable("eventId") String eventId)
			throws Exception ;
	
}
