package com.itorix.apiwiz.design.studio.service;

import com.itorix.apiwiz.design.studio.model.NotificationDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/v1/notify")
public interface NotificationService {

	@GetMapping(value="/user/{userId}", produces = {"application/json"})
	public ResponseEntity<List<NotificationDetails>> retrieveNotifications(
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionId", required = false) String interactionId,
			@PathVariable("userId") String userId,
			@RequestParam(value = "offset", required = false, defaultValue = "1") int offset,
			@RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) throws Exception;

	@PostMapping(consumes = {"application/json"}, produces = {"application/json"})
	public ResponseEntity<?> createNotification(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestBody NotificationDetails notificationDetails) throws Exception;

	@DeleteMapping(value="/{id}", consumes = {"application/json"}, produces = {"application/json"})
	public ResponseEntity<?> removeNotification(@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionId", required = false) String interactionId, @PathVariable("id") String id)
			throws Exception;

	@PutMapping(value = "/{id}", consumes = {"application/json"}, produces = {"application/json"})
	public ResponseEntity<NotificationDetails> updateNotifications(
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionId", required = false) String interactionId, @PathVariable("id") String id,
			@RequestBody NotificationDetails notificationDetails) throws Exception;

	@GetMapping(value="/{notificationId}", produces = {"application/json"})
	public ResponseEntity<Object> retrieveNotificationsByNotificationId(
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionId", required = false) String interactionId,
			@PathVariable("notificationId") String notificationId,
			@RequestParam(value = "offset", required = false, defaultValue = "1") int offset,
			@RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) throws Exception;
	@PostMapping(value="/read/{userId}", produces = {"application/json"})
	public ResponseEntity<?> updateUserNotifications(
			@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionId", required = false) String interactionId,
			@PathVariable("userId") String userId) throws Exception;

}
