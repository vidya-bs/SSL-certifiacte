package com.itorix.apiwiz.data.management.businessimpl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.itorix.apiwiz.common.model.apigee.CommonConfiguration;
import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.properties.ApplicationProperties;
import com.itorix.apiwiz.common.util.scheduler.Schedule;
import com.itorix.apiwiz.data.management.business.OrganizationSchedulerBusiness;
import com.itorix.apiwiz.data.management.model.ScheduleModel;
import com.itorix.apiwiz.identitymanagement.dao.BaseRepository;
import com.itorix.apiwiz.identitymanagement.model.User;
import com.itorix.apiwiz.identitymanagement.model.UserSession;

@Component
public class OrganizationSchedulerBusinessImpl implements OrganizationSchedulerBusiness {

	private static final Logger logger = LoggerFactory.getLogger(OrganizationSchedulerBusinessImpl.class);
	@Autowired
	BaseRepository baseRepository;

	@Autowired
	OrganizationBusinessImpl organizationService;

	@Value("${itorix.core.scheduler.enable:false}")
	private String scheduleEnable;

	@Value("${itorix.core.scheduler.primary:null}")
	private String primary;

	@Value("${itorix.core.scheduler.primary.host:null}")
	private String primaryHost;

	@Autowired
	ApplicationProperties applicationProperties;

	/**
	 * createOrganizationSchedule
	 *
	 * @param scheduleModel
	 * 
	 * @throws ItorixException
	 */
	public void createOrganizationSchedule(ScheduleModel scheduleModel) throws ItorixException {
		logger.debug("OrganizationSchedulerService.createOrganizationSchedule : CorelationId="
				+ scheduleModel.getInteractionid() + " : scheduleModel=" + scheduleModel);
		ScheduleModel model1 = baseRepository.findOne(ScheduleModel.LABEL_ORGANAIZATION,
				scheduleModel.getOrganization(), ScheduleModel.LABEL_PERIODICITY, scheduleModel.getPeriodicity(),
				ScheduleModel.class);
		if (model1 != null) {
			throw new ItorixException(ErrorCodes.errorMessage.get("DataBackup-1000"), "DataBackup-1000");
		} else {
			scheduleModel = baseRepository.save(scheduleModel);
		}
	}

	/**
	 * updateOrganizationSchedule
	 *
	 * @param scheduleModel
	 * 
	 * @throws ItorixException
	 */
	public void updateOrganizationSchedule(ScheduleModel scheduleModel) throws ItorixException {
		logger.debug("OrganizationSchedulerService.updateOrganizationSchedule : CorelationId="
				+ scheduleModel.getInteractionid() + " : scheduleModel=" + scheduleModel);
		ScheduleModel model1 = baseRepository.findById(scheduleModel.getId(), ScheduleModel.class);
		if (model1 == null) {
			throw new ItorixException(ErrorCodes.errorMessage.get("DataBackup-1001"), "DataBackup-1001");
		} else {
			model1.setOrganization(scheduleModel.getOrganization());
			model1.setPeriodicity(scheduleModel.getPeriodicity());
			scheduleModel = baseRepository.save(model1);
		}
	}

	/**
	 * deleteOrganizationSchedule
	 *
	 * @param scheduleModel
	 * 
	 * @throws ItorixException
	 */
	public void deleteOrganizationSchedule(ScheduleModel scheduleModel) throws ItorixException {
		logger.debug("OrganizationSchedulerService.deleteOrganizationSchedule : CorelationId="
				+ scheduleModel.getInteractionid() + " : scheduleModel=" + scheduleModel);
		ScheduleModel model1 = baseRepository.findById(scheduleModel.getId(), ScheduleModel.class);
		if (model1 == null) {
			throw new ItorixException(ErrorCodes.errorMessage.get("DataBackup-1001"), "DataBackup-1001");
		} else {
			baseRepository.delete(scheduleModel.getId(), ScheduleModel.class);
		}
	}

	/**
	 * getOrganizationSchedule
	 *
	 * @param interactionid
	 * 
	 * @return
	 * 
	 * @throws ItorixException
	 */
	public List<ScheduleModel> getOrganizationSchedule(String interactionid) throws ItorixException {
		logger.debug("OrganizationSchedulerService.getOrganizationSchedule : CorelationId=" + interactionid);
		List<ScheduleModel> list = baseRepository.findAll(ScheduleModel.class);
		logger.debug("OrganizationSchedulerService.getOrganizationSchedule : CorelationId=" + interactionid + ": list="
				+ list);
		return list;
	}

	@Scheduled(cron = "0 1 1 * * ?")
	public void doDaily() throws Exception {
		if (Schedule.isSchedulable(scheduleEnable, primary, primaryHost)) {
			List<ScheduleModel> list = baseRepository.findAll(ScheduleModel.class);
			for (ScheduleModel model : list) {
				User user = getUser(model.getUserId());
				if (user != null) {
					CommonConfiguration cfg = new CommonConfiguration();
					cfg.setIsCleanUpAreBackUp(false);
					// Apigee apigee=user.getApigee();
					// if(apigee!=null){
					/*
					 * cfg.setApigeeEmail(apigee.getUserName());
					 * cfg.setApigeePassword(apigee.getDecryptedPassword());
					 */

					cfg.setApigeeEmail(applicationProperties.getApigeeServiceUsername());
					cfg.setApigeePassword(applicationProperties.getApigeeServicePassword());
					// }
					organizationService.scheduleBackupOrganization(cfg);
				}
			}
		}
	}

	@Scheduled(cron = "1 0 0 ? * WED")
	public void doWeekly() throws Exception {
		if (Schedule.isSchedulable(scheduleEnable, primary, primaryHost)) {
			List<ScheduleModel> list = baseRepository.findAll(ScheduleModel.class);
			for (ScheduleModel model : list) {
				User user = getUser(model.getUserId());
				if (user != null) {
					CommonConfiguration cfg = new CommonConfiguration();
					cfg.setIsCleanUpAreBackUp(false);
					// Apigee apigee=user.getApigee();
					// if(apigee!=null){
					/*
					 * cfg.setApigeeEmail(apigee.getUserName());
					 * cfg.setApigeePassword(apigee.getDecryptedPassword());
					 */
					cfg.setApigeeEmail(applicationProperties.getApigeeServiceUsername());
					cfg.setApigeePassword(applicationProperties.getApigeeServicePassword());
					// }
					organizationService.scheduleBackupOrganization(cfg);
				}
			}
		}
	}

	private User getUser(String userId) {
		return baseRepository.findById(userId, User.class);
	}

	// public Apigee getApigeeCredential(String jsessionid) {
	// UserSession userSessionToken = baseRepository.findById(jsessionid,
	// UserSession.class);
	// User user = baseRepository.findById(userSessionToken.getUserId(),
	// User.class);
	// if (user != null) {
	// Apigee apigee = user.getApigee();
	// return apigee;
	// } else {
	// return null;
	// }
	// }

	public String getUserId(String jsessionid) {
		UserSession userSessionToken = baseRepository.findById(jsessionid, UserSession.class);
		User user = baseRepository.findById(userSessionToken.getUserId(), User.class);
		if (user != null) {
			return user.getId();
		} else {
			return null;
		}
	}
}
