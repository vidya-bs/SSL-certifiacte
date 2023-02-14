package com.itorix.apiwiz.marketing.careers.model;

import com.itorix.apiwiz.marketing.dao.CareersDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.Map;


@Component
@Slf4j
public class EmailContentParser {

  @Autowired
  private Environment env;

  @Autowired
  private CareersDao careersDao;

  private Map<String, String> notificationEmailTemplates;

  private static final String JOB_APPLICATION = "JOB_APPLICATION";

  private static final String JOB_APPLICATION_SUBJECT = "itorix.app.careers.job.email.subject";

  private static final String JOB_APPLICATION_BODY = "itorix.app.careers.job.email.body";


  public String getEmailSubject(Object... contentToReplace) {
    return MessageFormat.format(env.getProperty(JOB_APPLICATION_SUBJECT), contentToReplace);
  }

  public String getEmailBody(Object... contentToReplace) {
    return MessageFormat.format(env.getProperty(JOB_APPLICATION_BODY), contentToReplace);
  }

  public String[] getRelevantEmailContent(JobApplication jobApplication) {

    JobPosting jobPosting = careersDao.getPosting(jobApplication.getJobId());
    String jobTitle = jobPosting.getName();
    String firstName = jobApplication.getFirstName();
    String lastName = jobApplication.getLastName();
    String emailId = jobApplication.getEmailId();
    String contactNumber = jobApplication.getContactNumber();
    String profileUrl = jobApplication.getProfile();

    return new String[] { jobTitle, firstName, lastName, emailId, contactNumber, profileUrl };
  }

}