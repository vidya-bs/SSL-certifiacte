package com.itorix.apiwiz.analytics.service;

import java.util.concurrent.ExecutionException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.itorix.apiwiz.analytics.model.PerformanceMetrics;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.identitymanagement.model.Apigee;

@CrossOrigin
@RestController
public interface AnalyticsService {
	@RequestMapping(method = RequestMethod.POST, value = "/v1/analyze/proxy-performance/level-1")
	public ResponseEntity<?> evaluateProxyPerformace(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody PerformanceMetrics performanceMetrics, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@RequestMapping(method = RequestMethod.POST, value = "/v1/analyze/proxy-performance/level-2")
	public ResponseEntity<?> evaluateAverageResponseTime(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody PerformanceMetrics performanceMetrics, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@RequestMapping(method = RequestMethod.POST, value = "/v1/analyze/proxy-performance/level-3")
	public ResponseEntity<?> evaluateAverageResponseTimeAtProxy(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody PerformanceMetrics performanceMetrics, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@RequestMapping(method = RequestMethod.POST, value = "/v1/analyze/proxy-performance/level-4")
	public ResponseEntity<?> evaluateTrafficeByProxy(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody PerformanceMetrics performanceMetrics, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@RequestMapping(method = RequestMethod.POST, value = "/v1/analyze/target-performance/level-1")
	public ResponseEntity<?> evaluateTrafficeByTarget(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody PerformanceMetrics performanceMetrics, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@RequestMapping(method = RequestMethod.POST, value = "/v1/analyze/target-performance/level-2")
	public ResponseEntity<?> evaluateTotalErrorComposition(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody PerformanceMetrics performanceMetrics, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@RequestMapping(method = RequestMethod.POST, value = "/v1/analyze/target-performance/level-3")
	public ResponseEntity<?> evaluateResponseTimeComposition(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody PerformanceMetrics performanceMetrics, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@RequestMapping(method = RequestMethod.POST, value = "/v1/analyze/target-performance/level-4")
	public ResponseEntity<?> evaluateTargetRequestPayLoadSize(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody PerformanceMetrics performanceMetrics, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@RequestMapping(method = RequestMethod.POST, value = "/v1/analyze/latency")
	public ResponseEntity<?> latencyAnalysis(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody PerformanceMetrics performanceMetrics, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@RequestMapping(method = RequestMethod.POST, value = "/v1/analyze/error-analysis/level-1")
	public ResponseEntity<?> totalTrafficVsProxyErrorVsTargetError(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody PerformanceMetrics performanceMetrics, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@RequestMapping(method = RequestMethod.POST, value = "/v1/analyze/error-analysis/level-2")
	public ResponseEntity<?> proxyErrorVsResponseCode(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody PerformanceMetrics performanceMetrics, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@RequestMapping(method = RequestMethod.POST, value = "/v1/analyze/error-analysis/level-3")
	public ResponseEntity<?> targetErrorVsResponseCode(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody PerformanceMetrics performanceMetrics, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@RequestMapping(method = RequestMethod.POST, value = "/v1/analyze/error-analysis/level-4")
	public ResponseEntity<?> errorVsProxyName(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody PerformanceMetrics performanceMetrics, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@RequestMapping(method = RequestMethod.POST, value = "/v1/analyze/error-analysis/level-5")
	public ResponseEntity<?> errorVsTargetName(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody PerformanceMetrics performanceMetrics, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@RequestMapping(method = RequestMethod.POST, value = "/v1/analyze/developer-analysis/level-1")
	public ResponseEntity<?> evaluateTrafficCountVsErrorCodes(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody PerformanceMetrics performanceMetrics, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@RequestMapping(method = RequestMethod.POST, value = "/v1/analyze/developer-analysis/level-2")
	public ResponseEntity<?> evalAvgTotalResponseTimeVsAvgRequestSize(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody PerformanceMetrics performanceMetrics, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@RequestMapping(method = RequestMethod.POST, value = "/v1/analyze/developer-analysis/level-3")
	public ResponseEntity<?> evalTotalTrafficSucessVsErrorCountByApp(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody PerformanceMetrics performanceMetrics, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@RequestMapping(method = RequestMethod.POST, value = "/v1/analyze/developer-analysis/level-4")
	public ResponseEntity<?> evalAvgTotalResponseTimeVsAvgRequestSizeByApp(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody PerformanceMetrics performanceMetrics, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@RequestMapping(method = RequestMethod.POST, value = "/v1/analyze/developer-analysis/level-5")
	public ResponseEntity<?> evalTotalSucessVsErrorAtProxyLevel(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody PerformanceMetrics performanceMetrics, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@RequestMapping(method = RequestMethod.POST, value = "/v1/analyze/developer-analysis/level-6")
	public ResponseEntity<?> evalTotalSucessVsErrorAtProduct(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody PerformanceMetrics performanceMetrics, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@RequestMapping(method = RequestMethod.POST, value = "/v1/analyze/developer-analysis/level-7")
	public ResponseEntity<?> evalTotalSucessVsErrorAtProductVsApp(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody PerformanceMetrics performanceMetrics, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@RequestMapping(method = RequestMethod.POST, value = "/v1/analyze/developer-analysis/level-8")
	public ResponseEntity<?> evalAvgTotalResponseTimeVsAvgRequestSizeByProduct(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody PerformanceMetrics performanceMetrics, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@RequestMapping(method = RequestMethod.GET, value = "/v1/analytics/dashboard", produces = {"application/json"})
	public ResponseEntity<Object> getdashBoardOverview(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam("org") String org, @RequestParam("env") String env,
			@RequestParam(value = "type", required = false) String type, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid)
			throws ItorixException, InterruptedException, ExecutionException;

	@RequestMapping(method = RequestMethod.GET, value = "/v1/analytics/timeseries", produces = {"application/json"})
	public ResponseEntity<Object> getdashBoardTimeSeries(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam("org") String org, @RequestParam("env") String env,
			@RequestParam(value = "type", required = false) String type, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid)
			throws ItorixException, InterruptedException, ExecutionException;

	@RequestMapping(method = RequestMethod.POST, value = "/v1/analytics/dashboard/setup", consumes = {
			"application/json"}, produces = {"application/json"})
	public @ResponseBody ResponseEntity<Void> dashBoardSetUp(
			@RequestHeader(value = "interactionid", required = false) String interactionid, @RequestBody Apigee apigee,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid)
			throws Exception;

	@RequestMapping(method = RequestMethod.GET, value = "/v1/analytics/dashboard/setup", produces = {
			"application/json"})
	public @ResponseBody ResponseEntity<Object> getDashBoardDetails(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid)
			throws Exception;

	@RequestMapping(method = RequestMethod.PUT, value = "/v1/analytics/dashboard/setup", consumes = {
			"application/json"}, produces = {"application/json"})
	public @ResponseBody ResponseEntity<Void> updateDashBoardSetUp(
			@RequestHeader(value = "interactionid", required = false) String interactionid, @RequestBody Apigee apigee,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid)
			throws Exception;

	@RequestMapping(method = RequestMethod.GET, value = "/v1/analytics/dashboard/organisations", produces = {
			"application/json"})
	public @ResponseBody ResponseEntity<Object> getOrganisationsList(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid)
			throws Exception;

	@RequestMapping(method = RequestMethod.GET, value = "/v1/analytics/dashboard/{org}/environments", produces = {
			"application/json"})
	public @ResponseBody ResponseEntity<Object> getEnvListForOrganisation(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@PathVariable("org") String org, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@RequestMapping(method = RequestMethod.GET, value = "/v1/analytics/dashboard/refresh/environments", produces = {
			"application/json"})
	public @ResponseBody ResponseEntity<Object> refreshEnvironments(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid)
			throws Exception;
}
