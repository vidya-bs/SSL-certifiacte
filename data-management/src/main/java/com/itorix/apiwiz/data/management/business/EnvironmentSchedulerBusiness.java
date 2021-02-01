package com.itorix.apiwiz.data.management.business;

import java.util.List;

import org.springframework.stereotype.Service;

import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.data.management.model.ScheduleModel;
@Service
public interface EnvironmentSchedulerBusiness {


	/**
	 * createEnvironmentSchedule
	 * @param scheduleModel
	 * @throws ItorixException
	 */
	public void createEnvironmentSchedule(ScheduleModel scheduleModel) throws ItorixException;

	/**
	 * updateEnvironmentSchedule
	 * @param scheduleModel
	 * @throws ItorixException
	 */
	public void updateEnvironmentSchedule(ScheduleModel scheduleModel) throws ItorixException ;

	/**
	 * deleteEnvironmentSchedule
	 * @param scheduleModel
	 * @throws ItorixException
	 */
	public void deleteEnvironmentSchedule(ScheduleModel scheduleModel) throws ItorixException;

	/**
	 * getEnvironmentSchedule
	 * @param interactionid
	 * @return
	 * @throws ItorixException
	 */
	public List<ScheduleModel> getEnvironmentSchedule(String interactionid) throws ItorixException;

}
