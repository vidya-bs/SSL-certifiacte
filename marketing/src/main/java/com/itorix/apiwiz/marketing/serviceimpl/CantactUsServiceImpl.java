package com.itorix.apiwiz.marketing.serviceimpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.itorix.apiwiz.marketing.contactus.model.ContactUsNotification;
import com.itorix.apiwiz.marketing.dao.ContactUsDao;
import com.itorix.apiwiz.marketing.service.CantactUsService;

public class CantactUsServiceImpl implements CantactUsService {

	@Autowired
	private ContactUsDao contactUsDao;
	
	@Override
	public ResponseEntity<?> createJobPosting(String interactionid, String apikey,
			ContactUsNotification contactUsNotification) throws Exception {
		contactUsDao.invokeNotificationAgent(contactUsNotification);
		return new ResponseEntity<>(HttpStatus.CREATED);
	}

}
