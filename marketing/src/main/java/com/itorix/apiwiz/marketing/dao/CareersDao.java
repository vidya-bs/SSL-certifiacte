package com.itorix.apiwiz.marketing.dao;

import com.itorix.apiwiz.common.factory.IntegrationHelper;
import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.properties.ApplicationProperties;
import com.itorix.apiwiz.common.util.StorageIntegration;
import com.itorix.apiwiz.common.util.artifatory.JfrogUtilImpl;
import com.itorix.apiwiz.common.util.encryption.RSAEncryption;
import com.itorix.apiwiz.common.util.mail.EmailTemplate;
import com.itorix.apiwiz.common.util.s3.S3Connection;
import com.itorix.apiwiz.common.util.s3.S3Utils;
import com.itorix.apiwiz.marketing.careers.model.EmailContentParser;
import com.itorix.apiwiz.marketing.careers.model.JobApplication;
import com.itorix.apiwiz.marketing.careers.model.JobPosting;
import com.itorix.apiwiz.marketing.careers.model.JobStatus;
import com.itorix.apiwiz.marketing.contactus.model.NotificationExecutionEvent;
import com.itorix.apiwiz.marketing.contactus.model.RequestModel;
import com.itorix.apiwiz.marketing.db.NotificationExecutorEntity;
import com.itorix.apiwiz.marketing.db.NotificationExecutorSql;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.*;

@Component
@Slf4j
public class CareersDao {
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

	@Autowired
	private ApplicationProperties applicationProperties;

	@Autowired
	private EmailContentParser emailContentParser;

	@Autowired
	private RSAEncryption rsaEncryption;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private NotificationExecutorSql sqlDao;

	private static final String API_KEY_NAME = "x-apikey";
	private static final String NOTIFICATION_AGENT_NOTIFY = "/v1/notification/";

	@Value("${itorix.notification.agent:null}")
	private String notificationAgentPath;

	@Value("${itorix.notification.agent.contextPath:null}")
	private String notificationContextPath;

	@Value("${itorix.app.careers.job.emailId:null}")
	private String hiringMailId;

	public String createUpdatePosting(JobPosting jobPosting) {
		Query query = new Query().addCriteria(
				Criteria.where("name").is(jobPosting.getName()).and("location").is(jobPosting.getLocation()));
		JobPosting dbJobPosting = masterMongoTemplate.findOne(query, JobPosting.class);
		if (dbJobPosting != null) {
			jobPosting.setId(dbJobPosting.getId());
			masterMongoTemplate.save(jobPosting);
		} else {
			if(jobPosting.getStatus()==null)jobPosting.setStatus(JobStatus.ACTIVE);
			masterMongoTemplate.save(jobPosting);
		}
		return jobPosting.getId();
	}

	public void deletePosting(String jobId) {
		Query query = new Query().addCriteria(Criteria.where("id").is(jobId));
		masterMongoTemplate.remove(query, JobPosting.class);
	}

	public List<JobPosting> getAllPostings() {
		List<JobPosting> postings = masterMongoTemplate.findAll(JobPosting.class);
		for (JobPosting posting : postings) {
			posting.setAboutUs(null);
			posting.setDetail(null);
		}
		return postings;
	}

	public List<JobPosting> getAllPostings(List<String> categoryList, List<String> locationList,
			List<String> rollList) {
		Query query = new Query();
		query.addCriteria(Criteria.where("status").is(JobStatus.ACTIVE));
		if (categoryList != null)
			query.addCriteria(Criteria.where("category").in(categoryList));
		if (locationList != null)
			query.addCriteria(Criteria.where("location").in(locationList));
		if (rollList != null)
			query.addCriteria(Criteria.where("role").in(rollList));
		List<JobPosting> postings = masterMongoTemplate.find(query, JobPosting.class);
		for (JobPosting posting : postings) {
			posting.setAboutUs(null);
			posting.setDetail(null);
		}
		return postings;
	}

	public JobPosting getPosting(String jobId) {
		Query query = new Query().addCriteria(Criteria.where("id").is(jobId));
		JobPosting posting = masterMongoTemplate.findOne(query, JobPosting.class);
		return posting;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public Map getCategories() {
		Set<String> categoryList = new HashSet<>();
		Set<String> rollList = new HashSet<>();
		Set<String> locationList = new HashSet<>();
		try {
			Query query=new Query().addCriteria(Criteria.where("status").is(JobStatus.ACTIVE));
			List<JobPosting> allJob=masterMongoTemplate.find(query, JobPosting.class);
			for(JobPosting i:allJob){
				categoryList.add(i.getCategory());
				rollList.add(i.getRole());
				locationList.add(i.getLocation());
			}
		}catch (Exception ex) {
		log.error("Exception occurred", ex);
		}
		Map categories = new HashMap();
		categories.put("category", categoryList);
		categories.put("location", locationList);
		categories.put("role", rollList);
		return categories;
	}

	public String createUpdateJobApplication(JobApplication jobApplication) {
		masterMongoTemplate.save(jobApplication);
		return jobApplication.getId();
	}

	public JobApplication getJobApplication(String emailId) {
		Query query = new Query().addCriteria(Criteria.where("emailId").is(emailId));
		JobApplication application = masterMongoTemplate.findOne(query, JobApplication.class);
		return application;
	}

	public String updateProfileFile(String userId, String filename, byte[] bytes) throws ItorixException {
		return updateToJfrog(userId + "/" + filename, bytes);
	}

	public void deleteProfileFile(String userId, String imagePath) throws ItorixException {
		deleteFileJfrogFile("/marketing/careers/" + userId);
	}

	private String updateToJfrog(String folderPath, byte[] bytes) throws ItorixException {
		try {
			String downloadURI = null;
			StorageIntegration storageIntegration = integrationHelper.getIntegration();
			downloadURI = storageIntegration.uploadFile("marketing/careers/" + folderPath, new ByteArrayInputStream(bytes));
			return downloadURI;

		} catch (Exception e) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Portfolio-1009"), "Marketing-1000");
		}
	}

	private void deleteFileJfrogFile(String folderPath) throws ItorixException {
		try {
			jfrogUtilImpl.deleteFileIgnore404(folderPath);
		} catch (Exception e) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Portfolio-1016"), "Marketing-3");
		}
	}
	public void invokeNotificationAgent(JobApplication jobApplication, MultipartFile profile) {
		try {
			if (jobApplication != null) {
				RequestModel requestModel = new RequestModel();
				EmailTemplate emailTemplate = new EmailTemplate();
				String[] emailContentToReplace = emailContentParser.getRelevantEmailContent(jobApplication);
				String mailBody = emailContentParser.getEmailBody(emailContentToReplace);
				String mailSubject = emailContentParser.getEmailSubject(emailContentToReplace);
				emailTemplate.setBody(mailBody);
				List<String> mailId = new ArrayList<>();
				mailId.add(hiringMailId);
				emailTemplate.setToMailId(mailId);
				emailTemplate.setSubject(mailSubject);
				try {
					emailTemplate.setAttachmentName(profile.getOriginalFilename());
					emailTemplate.setAttachment(profile.getBytes());
				} catch (Exception ex){
					log.error("Exception Occured {}", ex.getMessage());
				}
				requestModel.setEmailContent(emailTemplate);
				requestModel.setType(RequestModel.Type.email);
				String notificationExecutionEventId = createNotificationEvent(requestModel);
				sqlDao.insertIntoNotificationEntity(null,
								notificationExecutionEventId, NotificationExecutorEntity.STATUSES.SCHEDULED.getValue(), null);
			}
		} catch (Exception e) {
			log.error("error returned from notification agent", e);
		}
	}

	public String createNotificationEvent(RequestModel requestModel) {
		NotificationExecutionEvent notificationExecutionEvent = new NotificationExecutionEvent();
		notificationExecutionEvent.setRequestModel(requestModel);
		notificationExecutionEvent.setCts(System.currentTimeMillis());
		notificationExecutionEvent.setStatus(NotificationExecutionEvent.STATUSES.SCHEDULED.getValue());
		masterMongoTemplate.save(notificationExecutionEvent);
		return notificationExecutionEvent.getId();
	}
}
