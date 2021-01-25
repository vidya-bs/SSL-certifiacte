package com.itorix.apiwiz.marketing.serviceimpl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.itorix.apiwiz.identitymanagement.security.annotation.UnSecure;
import com.itorix.apiwiz.marketing.careers.model.JobApplication;
import com.itorix.apiwiz.marketing.careers.model.JobPosting;
import com.itorix.apiwiz.marketing.dao.CareersDao;
import com.itorix.apiwiz.marketing.service.CareersService;

@CrossOrigin
@RestController
public class CareersServiceImpl implements CareersService {
	@Autowired
	private CareersDao careersDao;
	
	@Override
	@UnSecure(ignoreValidation=true)
	public ResponseEntity<?> createJobPosting(String interactionid, String jsessionid, String apikey, JobPosting jobPosting)
			throws Exception {
		careersDao.createUpdatePosting(jobPosting);
		return new ResponseEntity<>(HttpStatus.CREATED);
	}

	@Override
	@UnSecure(ignoreValidation=true)
	public ResponseEntity<?> updateJobPosting(String interactionid, String jsessionid, String apikey, String jobId,
			JobPosting jobPosting) throws Exception {
		jobPosting.setId(jobId);
		careersDao.createUpdatePosting(jobPosting);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	@UnSecure(ignoreValidation=true)
	public ResponseEntity<?> deleteJobPosting(String interactionid, String jsessionid, String apikey, String jobId) throws Exception {
		careersDao.deletePosting(jobId);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	@UnSecure(ignoreValidation=true)
	public ResponseEntity<?> getJobPosting(String interactionid, String jsessionid, String apikey, String jobId) throws Exception {
		return new ResponseEntity<>(careersDao.getPosting(jobId), HttpStatus.OK);
	}

	@Override
	@UnSecure(ignoreValidation=true)
	public ResponseEntity<?> getJobPostings(String interactionid, String jsessionid, String apikey,
			String category, String location, String roll) throws Exception {
		List<String> categoryList = null;
		List<String> locationList = null;
		List<String> rollList = null;
		if(category != null){
			categoryList = Arrays.asList(category.split(","));
		}
		if(location != null){
			locationList = Arrays.asList(location.split(","));
		}
		if(roll != null){
			rollList = Arrays.asList(roll.split(","));
		}
		return new ResponseEntity<>(careersDao.getAllPostings(categoryList, locationList, rollList), HttpStatus.OK);
	}

	@Override
	@UnSecure(ignoreValidation=true)
	public ResponseEntity<?> createJobApplication(String interactionid, String jsessionid, String apikey, String jobId,
			String firstName, String lastName, String emailId, String contactNumber, 
			MultipartFile profile) throws Exception {
		JobApplication jobApplication = new JobApplication(jobId, firstName, lastName, emailId, contactNumber);
		JobApplication dbJobApplication =  careersDao.getJobApplication(emailId);
		if(dbJobApplication!= null){
			jobApplication.setId(dbJobApplication.getId());
		}
		else{
			String id = UUID.randomUUID().toString();
			id.replaceAll("-", "");
			jobApplication.setId(id);
		}
		if(profile != null){
			byte[] bytes = profile.getBytes();
			String filename = profile.getOriginalFilename();
			String profileURL = careersDao.updateProfileFile(jobApplication.getId(), filename, bytes);
			jobApplication.setProfile(profileURL);
		}
		careersDao.createUpdateJobApplication(jobApplication);
		Map<String,String> response = new HashMap<>();
		response.put("message", "Thanks for taking time and posting your information. We will review the details and get back to you shortly.");
		response.put("posting_id",jobApplication.getId());
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@Override
	@UnSecure(ignoreValidation=true)
	public ResponseEntity<?> getCategories(String interactionid, String jsessionid, String apikey) throws Exception {
		return new ResponseEntity<>(careersDao.getCategories(), HttpStatus.OK);
	}

}
