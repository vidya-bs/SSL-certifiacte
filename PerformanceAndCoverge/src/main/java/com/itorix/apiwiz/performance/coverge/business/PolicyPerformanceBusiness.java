package com.itorix.apiwiz.performance.coverge.business;

import java.util.List;

import org.springframework.stereotype.Component;

import com.itorix.apiwiz.common.model.apigee.CommonConfiguration;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.identitymanagement.model.Apigee;
import com.itorix.apiwiz.identitymanagement.model.User;
import com.itorix.apiwiz.performance.coverge.model.Debug;
import com.itorix.apiwiz.performance.coverge.model.History;
import com.itorix.apiwiz.performance.coverge.model.PolicyPerformanceBackUpInfo;

@Component
public interface PolicyPerformanceBusiness {

	public Object executePolicyPerformance(CommonConfiguration cfg) throws ItorixException, Exception;

	public Apigee getApigeeCredential(String jsessionid);

	public User getUserDetailsFromSessionID(String jsessionid);

	/**
	 * getPolicyTimes
	 *
	 * @param trace
	 * 
	 * @return
	 */
	public Debug getPolicyTimes(String trace);

	/**
	 * getPolicyPerformanceList
	 *
	 * @param interactionid
	 * 
	 * @return
	 */
	public List<History> getPolicyPerformanceList(String interactionid) throws Exception;

	public List<History> getPolicyPerformanceList(String interactionid, boolean filter, String proxy, String org,
			String env, String daterange, boolean expand, Integer offset, Integer pageSize) throws Exception;

	/**
	 * getPolicyPerformanceOnId
	 *
	 * @param id
	 * @param interactionid
	 * 
	 * @return
	 */
	public PolicyPerformanceBackUpInfo getPolicyPerformanceOnId(String id, String interactionid);

	/**
	 * deletePolicyPerformanceOnId
	 *
	 * @param id
	 * @param interactionid
	 */
	public void deletePolicyPerformanceOnId(String id, String interactionid);
}
