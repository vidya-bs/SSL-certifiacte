package com.itorix.apiwiz.marketing.serviceimpl;

import java.util.List;
import java.util.stream.Collectors;

import com.itorix.apiwiz.marketing.events.model.UserEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import com.itorix.apiwiz.identitymanagement.security.annotation.UnSecure;
import com.itorix.apiwiz.marketing.contactus.model.ContactUsNotification;
import com.itorix.apiwiz.marketing.contactus.model.NotificatoinEvent;
import com.itorix.apiwiz.marketing.dao.ContactUsDao;
import com.itorix.apiwiz.marketing.service.CantactUsService;

@CrossOrigin
@RestController
public class CantactUsServiceImpl implements CantactUsService {

	@Autowired
	private ContactUsDao contactUsDao;

	@Override
	@UnSecure(ignoreValidation = true)
	public ResponseEntity<?> createJobPosting(String interactionid, String apikey,
			ContactUsNotification contactUsNotification) throws Exception {
		contactUsDao.invokeNotificationAgent(contactUsNotification);
		return new ResponseEntity<>(HttpStatus.CREATED);
	}

	@Override
	@UnSecure(ignoreValidation = true)
	public ResponseEntity<?> createNotificatonConfig(String interactionid, String apikey,
			List<NotificatoinEvent> notificatoinEvents) throws Exception {
		contactUsDao.updateNotificationConfigs(notificatoinEvents);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	@UnSecure(ignoreValidation = true)
	public ResponseEntity<?> getNotificatonConfig(String interactionid, String apikey) throws Exception {
		List<String> configs = contactUsDao.getNotificationConfigs().stream().filter(o -> !o.getName().isEmpty())
				.map(o -> o.getName()).collect(Collectors.toList());
		return new ResponseEntity<>(configs, HttpStatus.OK);
	}

	@Override
	@UnSecure(ignoreValidation = true)
	public ResponseEntity<?> storeUserEvent(String interactionid, String apikey, UserEvent userEvent) {
		contactUsDao.saveUserEvent(userEvent);
		return new ResponseEntity<>(HttpStatus.ACCEPTED);
	}
}
