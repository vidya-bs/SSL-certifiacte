package com.itorix.apiwiz.analytics.businessImpl;

import com.itorix.apiwiz.analytics.model.MetricForAdminUser;
import com.itorix.apiwiz.analytics.model.MetricsForOperationsUser;
import com.itorix.apiwiz.analytics.model.OtherMetric;
import com.itorix.apiwiz.analytics.model.UserRoleMetrics;
import com.itorix.apiwiz.common.model.SwaggerTeam;
import com.itorix.apiwiz.identitymanagement.model.ServiceRequestContextHolder;
import com.itorix.apiwiz.identitymanagement.model.User;
import com.itorix.apiwiz.identitymanagement.model.UserSession;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserRoleMetricsImpl {

	@Qualifier("masterMongoTemplate")
	@Autowired
	private MongoTemplate masterMongoTemplate;

	@Autowired
	private MongoTemplate mongoTemplate;

	public UserRoleMetrics createUserRoleMetrics() {
		UserRoleMetrics userRoleMetrics = new UserRoleMetrics();

		MetricForAdminUser metricForAdminUser = generateMetricsForAdminUser();
		userRoleMetrics.setMetricForAdminUser(metricForAdminUser);

		MetricsForOperationsUser metricsForOperationsUser = generateMetricsForOperationsUser();
		userRoleMetrics.setMetricsForOperationsUser(metricsForOperationsUser);

		userRoleMetrics.setOtherMetric(generateOtherMetrics());

		return userRoleMetrics;
	}

	private OtherMetric generateOtherMetrics() {
		OtherMetric otherUserMetric = new OtherMetric();
		Map<String, Integer> oas2CountByStatus = getOasCountByStatus("2.0");
		otherUserMetric.setOas2CountByStatus(oas2CountByStatus);

		Map<String, Integer> oas3CountByStatus = getOasCountByStatus("3.0");
		otherUserMetric.setOas3CountByStatus(oas3CountByStatus);
		otherUserMetric.setVirtualizationRequestsWithoutMatch(virtualizationRequestWithoutMatch());
		otherUserMetric
				.setNoOfTestsWithLessThanFiftyPercentCoverage(getNumberOfTestsWithLessThanFiftyPercentCoverage());

		return otherUserMetric;
	}

	private Integer getNumberOfTestsWithLessThanFiftyPercentCoverage() {
		return mongoTemplate
				.find(Query.query(Criteria.where("successRatio").lte(50)), Document.class, "Test.Collections.List")
				.size();
	}

	private Integer virtualizationRequestWithoutMatch() {
		return mongoTemplate.find(Query.query(Criteria.where("wasMatched").is(Boolean.FALSE)), Document.class,
				"Mock.Execution.Logs").size();
	}

	private Map<String, Integer> getOasCountByStatus(String version) {
		String collectionName = version.equals("2.0") ? "Design.Swagger.List" : "Design.Swagger3.List";
		GroupOperation groupOperation = Aggregation.group("status").count().as("count");
		List<Document> mappedResults = mongoTemplate
				.aggregate(Aggregation.newAggregation(groupOperation), collectionName, Document.class)
				.getMappedResults();
		Map<String, Integer> oas2CountByStatus = mappedResults.stream()
				.collect(Collectors.toMap(d -> d.getString("_id"), d -> d.getInteger("count")));
		return oas2CountByStatus;
	}

	private MetricsForOperationsUser generateMetricsForOperationsUser() {
		MetricsForOperationsUser metricsForOperationsUser = new MetricsForOperationsUser();
		GroupOperation groupOperation = Aggregation.group("state").count().as("count");
		Aggregation aggregation = Aggregation.newAggregation(groupOperation);
		List<Document> mappedResults = mongoTemplate.aggregate(aggregation, "CICD.Release.Package.List", Document.class)
				.getMappedResults();
		Map<String, Integer> releasePackageCountByState = mappedResults.stream()
				.collect(Collectors.toMap(d -> d.getString("_id"), d -> d.getInteger("count")));

		metricsForOperationsUser.setReleasePackageCountByStatus(releasePackageCountByState);

		GroupOperation groupSR = Aggregation.group("type", "status").count().as("count");
		Aggregation aggregationSR = Aggregation.newAggregation(groupSR);

		List<Document> serviceRequestByStatus = mongoTemplate
				.aggregate(aggregationSR, "Connectors.Apigee.ServiceRequest.Lists", Document.class).getMappedResults();

		metricsForOperationsUser.setServiceRequestByStatus(serviceRequestByStatus);

		return metricsForOperationsUser;

	}

	private MetricForAdminUser generateMetricsForAdminUser() {
		MetricForAdminUser metricForAdminUser = new MetricForAdminUser();

		generateUserMetrics(metricForAdminUser);

		generateTeamMetrics(metricForAdminUser);

		return metricForAdminUser;
	}

	private void generateUserMetrics(MetricForAdminUser metricForAdminUser) {
		UserSession authentication = ServiceRequestContextHolder.getContext().getUserSessionToken();
		String currentTenant = authentication.getTenant() != null
				? mongoTemplate.getDb().getName()
				: authentication.getTenant();

		List<User> all = masterMongoTemplate.findAll(User.class);

		Set<User> usersFilterByWS = all.stream().filter(u -> u.containsWorkspace(currentTenant))
				.collect(Collectors.toSet());
		metricForAdminUser.setNumberOfUsers(usersFilterByWS.stream().count());

		long numberOfLockedUsers = usersFilterByWS.stream().filter(u -> u.getUserWorkspace(currentTenant).getActive())
				.count();
		metricForAdminUser.setNumberOfLockedUsers(numberOfLockedUsers);

		long unixEpoch = LocalDateTime.now().minusDays(5).toInstant(ZoneOffset.UTC).toEpochMilli();

		long numberOfNewUsersCreated = usersFilterByWS.stream()
				.filter(u -> u.getCts() != null && u.getCts() >= unixEpoch).count();

		metricForAdminUser.setNumberOfNewUsers(numberOfNewUsersCreated);
	}

	private void generateTeamMetrics(MetricForAdminUser metricForAdminUser) {
		long unixEpoch = LocalDateTime.now().minusDays(5).toInstant(ZoneOffset.UTC).toEpochMilli();
		List<SwaggerTeam> listOfTeams = mongoTemplate.findAll(SwaggerTeam.class);
		int totalTeams = listOfTeams.size();
		metricForAdminUser.setTotalTeams(totalTeams);

		long numberOfNewTeams = listOfTeams.stream().filter(t -> t.getCts() >= unixEpoch).count();
		metricForAdminUser.setNumberOfNewTeams(numberOfNewTeams);
	}
}
