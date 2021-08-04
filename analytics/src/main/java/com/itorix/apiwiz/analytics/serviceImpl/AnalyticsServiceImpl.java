package com.itorix.apiwiz.analytics.serviceImpl;

import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.itorix.apiwiz.analytics.dao.AnalyticsDao;
import com.itorix.apiwiz.analytics.model.PerformanceMetrics;
import com.itorix.apiwiz.analytics.service.AnalyticsService;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.identitymanagement.model.Apigee;

@CrossOrigin
@RestController
public class AnalyticsServiceImpl implements AnalyticsService {

	@Autowired
	AnalyticsDao analyticsDao;

	public ResponseEntity<?> evaluateProxyPerformace(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody PerformanceMetrics performanceMetrics, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {
		Object apiProxyPerformanceTrafficResponse = analyticsDao.apiProxyPerformanceTraffic(performanceMetrics,
				jsessionid, interactionid);
		return new ResponseEntity<Object>(apiProxyPerformanceTrafficResponse, HttpStatus.OK);
	}

	public ResponseEntity<?> evaluateAverageResponseTime(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody PerformanceMetrics performanceMetrics, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {
		Object apiProxyPerformanceTrafficResponse = analyticsDao.averageResponseTime(performanceMetrics, jsessionid,
				interactionid);
		return new ResponseEntity<Object>(apiProxyPerformanceTrafficResponse, HttpStatus.OK);
	}

	public ResponseEntity<?> evaluateAverageResponseTimeAtProxy(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody PerformanceMetrics performanceMetrics, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {
		Object apiProxyPerformanceTrafficResponse = analyticsDao.averageResponseTimeAtProxy(performanceMetrics,
				jsessionid, interactionid);
		return new ResponseEntity<Object>(apiProxyPerformanceTrafficResponse, HttpStatus.OK);
	}

	public ResponseEntity<?> evaluateTrafficeByProxy(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody PerformanceMetrics performanceMetrics, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {
		Object apiProxyPerformanceTrafficResponse = analyticsDao.evaluateTrafficAtProxy(performanceMetrics, jsessionid,
				interactionid);
		return new ResponseEntity<Object>(apiProxyPerformanceTrafficResponse, HttpStatus.OK);
	}

	public ResponseEntity<?> evaluateTrafficeByTarget(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody PerformanceMetrics performanceMetrics, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {
		Object apiProxyPerformanceTrafficResponse = analyticsDao.evaluateTrafficByTarget(performanceMetrics, jsessionid,
				interactionid); // need
								// to
								// change
		return new ResponseEntity<Object>(apiProxyPerformanceTrafficResponse, HttpStatus.OK);
	}

	public ResponseEntity<?> evaluateTotalErrorComposition(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody PerformanceMetrics performanceMetrics, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {
		Object apiProxyPerformanceTrafficResponse = analyticsDao.evaluateTargetErrorComposition(performanceMetrics,
				jsessionid, interactionid);
		return new ResponseEntity<Object>(apiProxyPerformanceTrafficResponse, HttpStatus.OK);
	}

	public ResponseEntity<?> evaluateResponseTimeComposition(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody PerformanceMetrics performanceMetrics, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {
		Object apiProxyPerformanceTrafficResponse = analyticsDao.evaluateResponseTimeComposition(performanceMetrics,
				jsessionid, interactionid);
		return new ResponseEntity<Object>(apiProxyPerformanceTrafficResponse, HttpStatus.OK);
	}

	public ResponseEntity<?> evaluateTargetRequestPayLoadSize(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody PerformanceMetrics performanceMetrics, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {
		Object apiProxyPerformanceTrafficResponse = analyticsDao.evaluateTargetRequestPayLoadSize(performanceMetrics,
				jsessionid, interactionid);
		return new ResponseEntity<Object>(apiProxyPerformanceTrafficResponse, HttpStatus.OK);
	}

	public ResponseEntity<?> latencyAnalysis(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody PerformanceMetrics performanceMetrics, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {
		Object apiProxyPerformanceTrafficResponse = analyticsDao.evaluateLatencyAnalysis(performanceMetrics, jsessionid,
				interactionid);
		return new ResponseEntity<Object>(apiProxyPerformanceTrafficResponse, HttpStatus.OK);
	}

	public ResponseEntity<?> totalTrafficVsProxyErrorVsTargetError(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody PerformanceMetrics performanceMetrics, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {
		Object apiProxyPerformanceTrafficResponse = analyticsDao
				.totalTrafficVsProxyErrorVsTargetError(performanceMetrics, jsessionid, interactionid);
		return new ResponseEntity<Object>(apiProxyPerformanceTrafficResponse, HttpStatus.OK);
	}

	public ResponseEntity<?> proxyErrorVsResponseCode(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody PerformanceMetrics performanceMetrics, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {
		Object apiProxyPerformanceTrafficResponse = analyticsDao.proxyErrorVsResponseCode(performanceMetrics,
				jsessionid, interactionid);
		return new ResponseEntity<Object>(apiProxyPerformanceTrafficResponse, HttpStatus.OK);
	}

	public ResponseEntity<?> targetErrorVsResponseCode(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody PerformanceMetrics performanceMetrics, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {
		Object apiProxyPerformanceTrafficResponse = analyticsDao.TargetErrorVsResponseCode(performanceMetrics,
				jsessionid, interactionid);
		return new ResponseEntity<Object>(apiProxyPerformanceTrafficResponse, HttpStatus.OK);
	}

	public ResponseEntity<?> errorVsProxyName(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody PerformanceMetrics performanceMetrics, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {
		Object apiProxyPerformanceTrafficResponse = analyticsDao.evaluateErrorVsProxyName(performanceMetrics,
				jsessionid, interactionid);
		return new ResponseEntity<Object>(apiProxyPerformanceTrafficResponse, HttpStatus.OK);
	}

	public ResponseEntity<?> errorVsTargetName(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody PerformanceMetrics performanceMetrics, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {
		Object apiProxyPerformanceTrafficResponse = analyticsDao.evaluateErrorVsTargetName(performanceMetrics,
				jsessionid, interactionid);
		return new ResponseEntity<Object>(apiProxyPerformanceTrafficResponse, HttpStatus.OK);
	}

	public ResponseEntity<?> evaluateTrafficCountVsErrorCodes(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody PerformanceMetrics performanceMetrics, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {
		Object apiProxyPerformanceTrafficResponse = analyticsDao.evaluateTrafficCountVsErrorCodes(performanceMetrics,
				jsessionid, interactionid);
		return new ResponseEntity<Object>(apiProxyPerformanceTrafficResponse, HttpStatus.OK);
	}

	public ResponseEntity<?> evalAvgTotalResponseTimeVsAvgRequestSize(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody PerformanceMetrics performanceMetrics, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {
		Object apiProxyPerformanceTrafficResponse = analyticsDao
				.evalAvgTotalResponseTimeVsAvgRequestSize(performanceMetrics, jsessionid, interactionid);
		return new ResponseEntity<Object>(apiProxyPerformanceTrafficResponse, HttpStatus.OK);
	}

	public ResponseEntity<?> evalTotalTrafficSucessVsErrorCountByApp(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody PerformanceMetrics performanceMetrics, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {
		Object apiProxyPerformanceTrafficResponse = analyticsDao
				.evalTotalTrafficSucessVsErrorCountByApp(performanceMetrics, jsessionid, interactionid);
		return new ResponseEntity<Object>(apiProxyPerformanceTrafficResponse, HttpStatus.OK);
	}

	public ResponseEntity<?> evalAvgTotalResponseTimeVsAvgRequestSizeByApp(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody PerformanceMetrics performanceMetrics, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {
		Object apiProxyPerformanceTrafficResponse = analyticsDao
				.evalAvgTotalResponseTimeVsAvgRequestSizeByApp(performanceMetrics, jsessionid, interactionid);
		return new ResponseEntity<Object>(apiProxyPerformanceTrafficResponse, HttpStatus.OK);
	}

	public ResponseEntity<?> evalTotalSucessVsErrorAtProxyLevel(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody PerformanceMetrics performanceMetrics, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {
		Object apiProxyPerformanceTrafficResponse = analyticsDao.evalTotalSucessVsErrorAtProxyLevel(performanceMetrics,
				jsessionid, interactionid);
		return new ResponseEntity<Object>(apiProxyPerformanceTrafficResponse, HttpStatus.OK);
	}

	public ResponseEntity<?> evalTotalSucessVsErrorAtProduct(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody PerformanceMetrics performanceMetrics, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {
		Object apiProxyPerformanceTrafficResponse = analyticsDao.evalTotalSucessVsErrorAtProduct(performanceMetrics,
				jsessionid, interactionid);
		return new ResponseEntity<Object>(apiProxyPerformanceTrafficResponse, HttpStatus.OK);
	}

	public ResponseEntity<?> evalTotalSucessVsErrorAtProductVsApp(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody PerformanceMetrics performanceMetrics, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {
		Object apiProxyPerformanceTrafficResponse = analyticsDao
				.evalTotalSucessVsErrorAtProductVsApp(performanceMetrics, jsessionid, interactionid);
		return new ResponseEntity<Object>(apiProxyPerformanceTrafficResponse, HttpStatus.OK);
	}

	public ResponseEntity<?> evalAvgTotalResponseTimeVsAvgRequestSizeByProduct(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestBody PerformanceMetrics performanceMetrics, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {
		Object apiProxyPerformanceTrafficResponse = analyticsDao
				.evalAvgTotalResponseTimeVsAvgRequestSizeByProduct(performanceMetrics, jsessionid, interactionid);
		return new ResponseEntity<Object>(apiProxyPerformanceTrafficResponse, HttpStatus.OK);
	}

	public ResponseEntity<Object> getdashBoardOverview(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam("org") String org, @RequestParam("env") String env,
			@RequestParam(value = "type", required = false) String type, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid)
			throws ItorixException, InterruptedException, ExecutionException {

		return new ResponseEntity<Object>(analyticsDao.getOverview(org, env, interactionid, type), HttpStatus.OK);
	}

	public ResponseEntity<Object> getdashBoardTimeSeries(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestParam("org") String org, @RequestParam("env") String env,
			@RequestParam(value = "type", required = false) String type, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid)
			throws ItorixException, InterruptedException, ExecutionException {

		return new ResponseEntity<Object>(analyticsDao.getTimeSeriesData(org, env, interactionid, type), HttpStatus.OK);
	}

	public @ResponseBody ResponseEntity<Void> dashBoardSetUp(
			@RequestHeader(value = "interactionid", required = false) String interactionid, @RequestBody Apigee apigee,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid)
			throws Exception {

		analyticsDao.dashBoardSet(apigee, interactionid, jsessionid);
		return new ResponseEntity<Void>(headers, HttpStatus.CREATED);
	}

	public @ResponseBody ResponseEntity<Object> getDashBoardDetails(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid)
			throws Exception {

		Object obj = analyticsDao.getDashBoardSetUpDetails(interactionid, jsessionid);
		return new ResponseEntity<Object>(obj, HttpStatus.OK);
	}

	public @ResponseBody ResponseEntity<Void> updateDashBoardSetUp(
			@RequestHeader(value = "interactionid", required = false) String interactionid, @RequestBody Apigee apigee,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid)
			throws Exception {

		Object obj = analyticsDao.updateDashBoardDetails(apigee, jsessionid, interactionid);
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	public @ResponseBody ResponseEntity<Object> getOrganisationsList(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid)
			throws Exception {

		return new ResponseEntity<Object>(analyticsDao.getOrganisationsList(interactionid, jsessionid), HttpStatus.OK);
	}

	public @ResponseBody ResponseEntity<Object> getEnvListForOrganisation(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@PathVariable("org") String org, @RequestHeader HttpHeaders headers,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception {

		return new ResponseEntity<Object>(analyticsDao.getEnvironmentsForOrganisations(org, interactionid, jsessionid),
				HttpStatus.OK);
	}

	public @ResponseBody ResponseEntity<Object> refreshEnvironments(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader HttpHeaders headers, @RequestHeader(value = "JSESSIONID") String jsessionid)
			throws Exception {

		return new ResponseEntity<Object>(analyticsDao.refreshEnvironments(jsessionid, interactionid), HttpStatus.OK);
	}
}
