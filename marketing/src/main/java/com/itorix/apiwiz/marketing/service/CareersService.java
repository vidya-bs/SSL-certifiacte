package com.itorix.apiwiz.marketing.service;

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
import com.itorix.apiwiz.marketing.careers.model.JobPosting;

@CrossOrigin
@RestController
public interface CareersService {

	@UnSecure(useUpdateKey=true)
	@RequestMapping(method = RequestMethod.POST, value = "/v1/marketing/careers")
	public ResponseEntity<?> createJobPosting(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID",required=false) String jsessionid,
			@RequestHeader(value="x-apikey")String apikey,
			@RequestBody JobPosting jobPosting) throws Exception;
	
	@UnSecure(useUpdateKey=true)
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/marketing/careers/{jobId}")
	public ResponseEntity<?> updateJobPosting(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID", required=false) String jsessionid,
			@RequestHeader(value="x-apikey")String apikey,
			@PathVariable("jobId") String jobId,
			@RequestBody JobPosting jobPosting) throws Exception;
	
	@UnSecure(useUpdateKey=true)
	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/marketing/careers/{jobId}")
	public ResponseEntity<?> deleteJobPosting(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID", required=false) String jsessionid, 
			@RequestHeader(value="x-apikey")String apikey,
			@PathVariable("jobId") String jobId)throws Exception;
	
	@UnSecure(ignoreValidation=true)
	@RequestMapping(method = RequestMethod.GET, value = "/v1/marketing/careers/{jobId}")
	public ResponseEntity<?> getJobPosting(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID", required=false) String jsessionid,
			@RequestHeader(value="x-apikey", required=false )String apikey,
			@PathVariable("jobId") String jobId)throws Exception;
	
	@UnSecure(ignoreValidation=true)
	@RequestMapping(method = RequestMethod.GET, value = "/v1/marketing/careers/categories")
	public ResponseEntity<?> getCategories(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID", required=false) String jsessionid,
			@RequestHeader(value="x-apikey", required=false )String apikey)throws Exception;
	
	@UnSecure(ignoreValidation=true)
	@RequestMapping(method = RequestMethod.GET, value = "/v1/marketing/careers")
	public ResponseEntity<?> getJobPostings(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID", required=false) String jsessionid,
			@RequestHeader(value="x-apikey")String apikey,
			@RequestParam(value = "category", required=false) String category,
			@RequestParam(value = "location", required=false) String location,
			@RequestParam(value = "role", required=false) String roll)throws Exception;
	
	@UnSecure(ignoreValidation=true)
	@RequestMapping(method = RequestMethod.POST, value = "/v1/marketing/careers-apply/{jobId}")
	public ResponseEntity<?> createJobApplication(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID", required=false) String jsessionid,
			@RequestHeader(value="x-apikey")String apikey,
			@PathVariable("jobId") String jobId,
			@RequestParam("firstName") String firstName, 
			@RequestParam("lastName") String lastName, 
			@RequestParam("emailId") String emailId, 
			@RequestParam("contactNumber") String contactNumber, 
			@RequestParam("profile") MultipartFile profile) throws Exception;
	
}
