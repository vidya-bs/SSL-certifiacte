package com.itorix.apiwiz.data.management.businessimpl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.data.management.business.EnvironmentSchedulerBusiness;
import com.itorix.apiwiz.data.management.model.ScheduleModel;
import com.itorix.apiwiz.datamanagement.service.PolicyMappingService;
import com.itorix.apiwiz.identitymanagement.dao.BaseRepository;

@Service
public class EnvironmentSchedulerBusinessImpl implements EnvironmentSchedulerBusiness {

	private static final Logger logger = LoggerFactory.getLogger(PolicyMappingService.class);
	@Autowired
	BaseRepository baseRepository;

	/**
	 * createEnvironmentSchedule
	 *
	 * @param scheduleModel
	 * 
	 * @throws ItorixException
	 */
	public void createEnvironmentSchedule(ScheduleModel scheduleModel) throws ItorixException {
		logger.info("EnvironmentSchedulerService.createEnvironmentSchedule : CorelationId="
				+ scheduleModel.getInteractionid() + " : scheduleModel=" + scheduleModel);
		ScheduleModel model1 = baseRepository.findOne(ScheduleModel.LABEL_ORGANAIZATION,
				scheduleModel.getOrganization(), ScheduleModel.LABEL_SELECTED_ENVIRONMENTS,
				scheduleModel.getSelectedEnvironments(), ScheduleModel.LABEL_PERIODICITY,
				scheduleModel.getPeriodicity(), ScheduleModel.class);
		if (model1 != null) {
			throw new ItorixException(ErrorCodes.errorMessage.get("DataBackup-1000"), "DataBackup-1000");
		} else {
			scheduleModel = baseRepository.save(scheduleModel);
		}
	}

	/**
	 * updateEnvironmentSchedule
	 *
	 * @param scheduleModel
	 * 
	 * @throws ItorixException
	 */
	public void updateEnvironmentSchedule(ScheduleModel scheduleModel) throws ItorixException {
		logger.info("EnvironmentSchedulerService.updateEnvironmentSchedule : CorelationId="
				+ scheduleModel.getInteractionid() + " : scheduleModel=" + scheduleModel);
		ScheduleModel model1 = baseRepository.findById(scheduleModel.getId(), ScheduleModel.class);
		if (model1 == null) {
			throw new ItorixException(ErrorCodes.errorMessage.get("DataBackup-1001"), "DataBackup-1001");
		} else {
			model1.setSelectedEnvironments(scheduleModel.getSelectedEnvironments());
			model1.setOrganization(scheduleModel.getOrganization());
			model1.setPeriodicity(scheduleModel.getPeriodicity());
			scheduleModel = baseRepository.save(model1);
		}
	}

	/**
	 * deleteEnvironmentSchedule
	 *
	 * @param scheduleModel
	 * 
	 * @throws ItorixException
	 */
	public void deleteEnvironmentSchedule(ScheduleModel scheduleModel) throws ItorixException {
		logger.info("EnvironmentSchedulerService.deleteEnvironmentSchedule : CorelationId="
				+ scheduleModel.getInteractionid() + " : scheduleModel=" + scheduleModel);
		ScheduleModel model1 = baseRepository.findById(scheduleModel.getId(), ScheduleModel.class);
		if (model1 == null) {
			throw new ItorixException(ErrorCodes.errorMessage.get("DataBackup-1001"), "DataBackup-1001");
		} else {
			baseRepository.delete(scheduleModel.getId(), ScheduleModel.class);
		}
	}

	/**
	 * getEnvironmentSchedule
	 *
	 * @param interactionid
	 * 
	 * @return
	 * 
	 * @throws ItorixException
	 */
	public List<ScheduleModel> getEnvironmentSchedule(String interactionid) throws ItorixException {
		logger.info("EnvironmentSchedulerService.getEnvironmentSchedule : CorelationId=" + interactionid);
		List<ScheduleModel> list = baseRepository.findAll(ScheduleModel.class);
		logger.info("EnvironmentSchedulerService.getEnvironmentSchedule : CorelationId=" + interactionid + ": list="
				+ list);
		return list;
	}
}
