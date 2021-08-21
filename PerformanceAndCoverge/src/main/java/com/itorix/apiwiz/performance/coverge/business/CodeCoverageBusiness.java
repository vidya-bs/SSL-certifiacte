package com.itorix.apiwiz.performance.coverge.business;

import java.util.List;

import javax.xml.bind.JAXBException;

import org.springframework.stereotype.Component;

import com.itorix.apiwiz.common.model.apigee.CommonConfiguration;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.model.policyperformance.ExecutedFlowAndPolicies;
import com.itorix.apiwiz.common.model.policyperformance.proxy.endpoint.ProxyEndpoint;
import com.itorix.apiwiz.common.model.policyperformance.target.endpoint.TargetEndpoint;
import com.itorix.apiwiz.identitymanagement.model.Apigee;
import com.itorix.apiwiz.identitymanagement.model.User;
import com.itorix.apiwiz.performance.coverge.model.CodeCoverageBackUpInfo;
import com.itorix.apiwiz.performance.coverge.model.CodeCoverageVO;
import com.itorix.apiwiz.performance.coverge.model.EndpointStatVO;
import com.itorix.apiwiz.performance.coverge.model.History;

@Component
public interface CodeCoverageBusiness {
	/**
	 * executeCodeCoverage
	 *
	 * @param cfg
	 * 
	 * @return
	 * 
	 * @throws Exception
	 */
	public CodeCoverageBackUpInfo executeCodeCoverage(CommonConfiguration cfg) throws Exception;

	/**
	 * markExecutedPoliciesForProxy
	 *
	 * @param endpoint
	 * @param executedFlowAndPolicies
	 * 
	 * @return
	 */
	public ProxyEndpoint markExecutedPoliciesForProxy(ProxyEndpoint endpoint,
			ExecutedFlowAndPolicies executedFlowAndPolicies);

	/**
	 * markExecutedPoliciesForTarget
	 *
	 * @param endpoint
	 * @param executedFlowAndPolicies
	 * 
	 * @return
	 */
	public TargetEndpoint markExecutedPoliciesForTarget(TargetEndpoint endpoint,
			ExecutedFlowAndPolicies executedFlowAndPolicies);

	/**
	 * doAnalyticsForProxyEndpoint
	 *
	 * @param updatedEndpoint
	 * 
	 * @return
	 * 
	 * @throws JAXBException
	 */
	public EndpointStatVO doAnalyticsForProxyEndpoint(ProxyEndpoint updatedEndpoint) throws JAXBException;

	/**
	 * doAnalyticsForTargetEndpoint
	 *
	 * @param updatedEndpoint
	 * 
	 * @return
	 * 
	 * @throws JAXBException
	 */
	public EndpointStatVO doAnalyticsForTargetEndpoint(TargetEndpoint updatedEndpoint) throws JAXBException;

	/**
	 * getCodeCoverageList
	 *
	 * @param interactionid
	 * 
	 * @return
	 */
	public List<History> getCodeCoverageList(String interactionid) throws ItorixException;

	/**
	 * getCodeCoverageList
	 *
	 * @param interactionid
	 * 
	 * @return
	 * 
	 * @throws Exception
	 */
	public List<History> getCodeCoverageList(String interactionid, boolean filter, String proxy, String org, String env,
			String daterange) throws Exception;

	/**
	 * getCodeCoverageOnId
	 *
	 * @param id
	 * @param interactionid
	 * 
	 * @return
	 */
	public CodeCoverageBackUpInfo getCodeCoverageOnId(String id, String interactionid) throws ItorixException;

	/**
	 * deleteCodeCoverageOnId
	 *
	 * @param id
	 * @param interactionid
	 */
	public void deleteCodeCoverageOnId(String id, String interactionid);

	/**
	 * executeUnitTests
	 *
	 * @param postman
	 * @param env
	 * 
	 * @return
	 * 
	 * @throws ItorixException
	 */
	@SuppressWarnings("unchecked")
	public Object executeUnitTests(String postman, String env) throws ItorixException;

	/**
	 * codeCoverageTest
	 *
	 * @param codeCoverageVO
	 * 
	 * @return
	 * 
	 * @throws Exception
	 */
	public CodeCoverageBackUpInfo codeCoverageTest(CodeCoverageVO codeCoverageVO) throws Exception;

	public Apigee getApigeeCredential(String jsessionid);

	public User getUserDetailsFromSessionID(String jsessionid);
}
