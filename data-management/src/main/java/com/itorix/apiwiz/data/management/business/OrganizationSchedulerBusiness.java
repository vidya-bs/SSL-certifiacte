package com.itorix.apiwiz.data.management.business;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.itorix.apiwiz.common.model.apigee.CommonConfiguration;
import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.data.management.business.OrganizationBusiness;
import com.itorix.apiwiz.data.management.model.ScheduleModel;
import com.itorix.apiwiz.identitymanagement.dao.BaseRepository;
import com.itorix.apiwiz.identitymanagement.model.User;
import com.itorix.apiwiz.identitymanagement.model.UserSession;


@Service
public interface OrganizationSchedulerBusiness  {

	

	/**
	 * createOrganizationSchedule
	 * @param scheduleModel
	 * @throws ItorixException
	 */
	public void createOrganizationSchedule(ScheduleModel scheduleModel) throws ItorixException ;

	/**
	 * updateOrganizationSchedule
	 * @param scheduleModel
	 * @throws ItorixException
	 */
	public void updateOrganizationSchedule(ScheduleModel scheduleModel) throws ItorixException;

	/**
	 * deleteOrganizationSchedule
	 * @param scheduleModel
	 * @throws ItorixException
	 */
	public void deleteOrganizationSchedule(ScheduleModel scheduleModel) throws ItorixException;

	/**
	 * getOrganizationSchedule
	 * @param interactionid
	 * @return
	 * @throws ItorixException
	 */
	public List<ScheduleModel> getOrganizationSchedule(String interactionid) throws ItorixException ;
	
	public String getUserId(String jsessionid) ;


}
