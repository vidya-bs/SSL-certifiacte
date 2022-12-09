package com.itorix.apiwiz.marketing.service;

import com.itorix.apiwiz.identitymanagement.security.annotation.UnSecure;
import com.itorix.apiwiz.marketing.events.model.Event;
import com.itorix.apiwiz.marketing.events.model.EventRegistration;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@CrossOrigin
@RestController
@RequestMapping("/v1/marketing")
public interface EventsService {

		@UnSecure(useUpdateKey = true)
	@PostMapping(value = "/events", produces = {
			MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> createEvent(@RequestHeader(value = "x-apikey") String apikey,@RequestBody Event event) throws Exception;

	@UnSecure(useUpdateKey = true)
	@PatchMapping(value = "/events/{eventId}")
	public ResponseEntity<?> updateEvent(@RequestHeader(value = "x-apikey") String apikey,
			@RequestBody Event event,@PathVariable(value = "eventId") String eventId) throws Exception;

	@UnSecure(ignoreValidation = true)
	@GetMapping(value = "/events")
	public ResponseEntity<?> getAllEvents(@RequestHeader(value = "x-apikey") String apikey,
			@RequestParam(value = "status", required = false) String status,
			@RequestParam(value = "offset", required = false, defaultValue = "1") int offset,
			@RequestParam(value = "pagesize", required = false, defaultValue = "10") int pagesize)throws Exception;

	@UnSecure(ignoreValidation = true)
	@RequestMapping(method = RequestMethod.GET, value = "/events/{slug}")
	public ResponseEntity<?> getEventBySlug(@RequestHeader(value = "x-apikey") String apikey,
			@PathVariable("slug") String slug) throws Exception;

	@UnSecure(useUpdateKey = true)
	@RequestMapping(method = RequestMethod.DELETE, value = "/events/{eventId}")
	public ResponseEntity<?> deleteEvent(@RequestHeader(value = "x-apikey") String apikey,
										 @PathVariable("eventId") String eventId) throws Exception;

	@UnSecure(ignoreValidation = true)
	@RequestMapping(method = RequestMethod.POST, value = "/events-register/{eventId}")
	public ResponseEntity<?> registerEvent(@RequestHeader(value = "x-apikey") String apikey,
			@PathVariable("eventId") String eventId,
			@RequestBody EventRegistration eventRegistration) throws Exception;

	@UnSecure(ignoreValidation = true)
	@RequestMapping(method = RequestMethod.GET, value = "/events-register/{eventId}")
	public ResponseEntity<?> getEventRegistrations(@RequestHeader(value = "x-apikey") String apikey,
			@PathVariable("eventId") String eventId) throws Exception;
}
