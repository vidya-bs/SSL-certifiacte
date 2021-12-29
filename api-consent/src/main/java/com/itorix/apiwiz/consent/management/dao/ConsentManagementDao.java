package com.itorix.apiwiz.consent.management.dao;

import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.model.integrations.workspace.WorkspaceIntegration;
import com.itorix.apiwiz.common.util.Date.DateUtil;
import com.itorix.apiwiz.consent.management.crypto.RSAKeyGenerator;
import com.itorix.apiwiz.consent.management.model.*;
import com.itorix.apiwiz.consent.management.serviceImpl.XlsService;
import com.itorix.apiwiz.identitymanagement.dao.BaseRepository;
import com.itorix.apiwiz.identitymanagement.model.Pagination;
import com.itorix.apiwiz.identitymanagement.model.UserSession;
import com.itorix.apiwiz.identitymanagement.model.Workspace;
import com.mongodb.client.DistinctIterable;
import com.mongodb.client.MongoCursor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Component
@Slf4j
public class ConsentManagementDao {

	@Autowired
	private BaseRepository baseRepository;

	@Autowired
	private MongoTemplate mongoTemplate;

	@Qualifier("masterMongoTemplate")
	@Autowired
	private MongoTemplate masterMongoTemplate;

	@Autowired
	private RSAKeyGenerator rsaKeyGenerator;

	@Autowired
	private XlsService xlsService;

	public void save(ScopeCategory scopeCategory) throws ItorixException {
		ScopeCategory existingScopeCategory = baseRepository.findById(scopeCategory.getName(), ScopeCategory.class);

		if (existingScopeCategory != null) {
			throw new ItorixException(String.format(ErrorCodes.errorMessage.get("ScopeCategory-001"), scopeCategory.getName()), "ScopeCategory-001");
		}

		populateAuditDetails(scopeCategory);

		mongoTemplate.save(scopeCategory);

	}

	private void populateAuditDetails(ScopeCategory scopeCategory) {
		String userId = null;
		String username = null;
		try {
			UserSession userSession = UserSession.getCurrentSessionToken();
			userId = userSession.getUserId();
			username = userSession.getUsername();
		} catch (Exception e) {
		}

		scopeCategory.setModifiedBy(userId);
		scopeCategory.setModifiedUserName(username);
		scopeCategory.setCreatedBy(userId);
		scopeCategory.setCreatedUserName(username);

		scopeCategory.setCts(System.currentTimeMillis());
		scopeCategory.setMts(System.currentTimeMillis());
	}

	@SneakyThrows
	public void update(ScopeCategory scopeCategory) {
		ScopeCategory existingScopeCategory = baseRepository.findById(scopeCategory.getName(), ScopeCategory.class);

		if (existingScopeCategory == null) {
			throw new ItorixException(String.format(ErrorCodes.errorMessage.get("ScopeCategory-002"), scopeCategory.getName()), "ScopeCategory-002");
		}

		String userId = null;
		String username = null;
		try {
			UserSession userSession = UserSession.getCurrentSessionToken();
			userId = userSession.getUserId();
			username = userSession.getUsername();
		} catch (Exception e) {

		}

		existingScopeCategory.setModifiedBy(userId);
		existingScopeCategory.setModifiedUserName(username);

		existingScopeCategory.setMts(System.currentTimeMillis());
		existingScopeCategory.setDisplayName(scopeCategory.getDisplayName());
		existingScopeCategory.setDescription(scopeCategory.getDescription());
		existingScopeCategory.setSummary(scopeCategory.getSummary());
		existingScopeCategory.setExpiry(scopeCategory.getExpiry());
		existingScopeCategory.setScopes(scopeCategory.getScopes());

		mongoTemplate.save(existingScopeCategory);

	}

	public ScopeCategoryResponse getScopeCategories(int offset, int pageSize, Map<String, String> searchParams) {
		List<Criteria> searchCriteria = getCriteria(searchParams);

		Criteria criteria = new Criteria().andOperator(searchCriteria.toArray(new Criteria[searchCriteria.size()]));

		Query query = searchCriteria.size() > 0 ? Query.query(criteria) : new Query();

		List<ScopeCategory> scopeCategories = mongoTemplate.find(query.with(Sort.by(Sort.Direction.DESC, "cts"))
				.skip(offset > 0 ? ((offset - 1) * pageSize) : 0).limit(pageSize), ScopeCategory.class);

		Pagination pagination = getPagination(offset, pageSize, ScopeCategory.class);
		ScopeCategoryResponse scopeCategoryResponse = new ScopeCategoryResponse();
		scopeCategoryResponse.setPagination(pagination);
		scopeCategoryResponse.setScopeCategories(scopeCategories);
		return scopeCategoryResponse;

	}


	public ScopeCategory getScopeCategoryByName(String name) {
		ScopeCategory scopeCategory = baseRepository.findOne("name", name, ScopeCategory.class);
		if (scopeCategory == null) {
			scopeCategory = baseRepository.findOne("displayName", name, ScopeCategory.class);
		}
		return scopeCategory;
	}

	public void deleteScopeCategory(String name) {
		baseRepository.delete("name", name, ScopeCategory.class);
	}

	public void createOrUpdateScopeCategoryColumns(ScopeCategoryColumns scopeCategoryColumns) {
		Query query = new Query();
		query.addCriteria(Criteria.where("columns").exists(true));
		Update update = new Update();
		update.set("columns", scopeCategoryColumns.getColumns());
		mongoTemplate.upsert(query, update, ScopeCategoryColumns.class);
	}

	public ScopeCategoryColumns getScopeCategoryColumns() {
		List<ScopeCategoryColumns> findAll = baseRepository.findAll(ScopeCategoryColumns.class);
		return findAll.isEmpty() ? null : findAll.get(0);
	}


	public ConsentResponse getConsentsOverview(int offset, int pageSize, Map<String, String> searchParams) {
		List<Criteria> searchCriteria = getConsentCriteria(searchParams);

		Criteria criteria = new Criteria().andOperator(searchCriteria.toArray(new Criteria[searchCriteria.size()]));

		Query query = searchCriteria.size() > 0 ? Query.query(criteria) : new Query();

		List<Consent> consents = mongoTemplate.find(query.with(Sort.by(Sort.Direction.DESC, "cts"))
				.skip(offset > 0 ? ((offset - 1) * pageSize) : 0).limit(pageSize), Consent.class);

		Pagination pagination = getPagination(offset, pageSize, Consent.class);
		ConsentResponse consentResponse = new ConsentResponse();
		consentResponse.setPagination(pagination);
		consentResponse.setConsentList(consents);
		return consentResponse;

	}

	private List<Criteria> getConsentCriteria(Map<String, String> searchCriteria) {
		List<Criteria> criteriaList = new ArrayList<>();
		searchCriteria.forEach((k, v) -> {
			if (null != v) {
				criteriaList.add(Criteria.where("consent." + k).is(v));
			}
		});
		return criteriaList;
	}

	private List<Criteria> getCriteria(Map<String, String> searchCriteria) {
		List<Criteria> criteriaList = new ArrayList<>();
		searchCriteria.forEach((k, v) -> {
			if (null != v) {
				criteriaList.add(Criteria.where(k).is(v));
			}
		});
		return criteriaList;
	}

	private Pagination getPagination(int offset, int pageSize, Class clazz) {
		Pagination pagination = new Pagination();
		pagination.setPageSize(pageSize);
		pagination.setOffset(offset);
		pagination.setTotal(getTotal(clazz));
		return pagination;
	}

	private long getTotal(Class clazz) {
		return baseRepository.findAll(clazz).size();
	}

	public String getConsentPublicKey() {
		String tenantId = mongoTemplate.getDb().getName();
		log.info("db Name {} ", tenantId);
		ConsentKeyPair consentKeyPair = mongoTemplate.findOne(Query.query(Criteria.where("tenantId").is(tenantId)), ConsentKeyPair.class);
		if(consentKeyPair != null ) {
			return consentKeyPair.getPublicKey();
		}
		return "";
	}

	public Integer getConsentExpirationInterval() {
		WorkspaceIntegration workspaceIntegration = mongoTemplate.findById("itorix.core.consent.expiry.interval", WorkspaceIntegration.class);
		if(workspaceIntegration != null ) {
			String propertyValue = workspaceIntegration.getPropertyValue();
			if(propertyValue != null && !"".equals(propertyValue)) {
				return Integer.valueOf(propertyValue);
			}
		}
		return null;
	}

	@SneakyThrows
	public String generateKeyPairs() {
		String tenantId = getTenantId();
		return rsaKeyGenerator.generateKeyPair(tenantId);
	}


	public Workspace getWorkspace(String tenantName) {
		return masterMongoTemplate.findOne(Query.query(Criteria.where("tenant").is(tenantName)), Workspace.class);
	}

	public String getWorkspaceKey(String tenantId) {
		Workspace workspace = getWorkspace(tenantId);
		if(workspace != null) {
			return workspace.getKey();
		}
		return "";
	}

	@SneakyThrows
	public String getToken() {
		ConsentKeyPair key = mongoTemplate.findOne(Query.query(Criteria.where("tenantId").is(getTenantId())), ConsentKeyPair.class);
		if(key != null ) {
			return key.getPublicKey();
		}
		throw new ItorixException(ErrorCodes.errorMessage.get("Consent-003"), "Consent-003");
	}

	public List<String> getScopeCategoryNames() {
		return findDistinctValuesByColumnName(Consent.class, "consent.category");
	}

	public <T> List<String> findDistinctValuesByColumnName(Class<T> clazz, String columnName) {
		return getList(
				mongoTemplate.getCollection(mongoTemplate.getCollectionName(clazz)).distinct(columnName, String.class));
	}

	private List<String> getList(DistinctIterable<String> iterable) {
		MongoCursor<String> cursor = iterable.iterator();
		List<String> list = new ArrayList<>();
		while (cursor.hasNext()) {
			list.add(cursor.next());
		}
		return list;
	}

	public String getTenantId(){
		return mongoTemplate.getDb().getName();
	}

    @SneakyThrows
	public ConsentAuditExportResponse generateExcelReport(String timeRange) {
		SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
		String timeRanges[] = timeRange.split("~");
		Date startDate = format.parse(timeRanges[0]);
		Date endDate = format.parse(timeRanges[1]);
		long startTime = DateUtil.getStartOfDay(startDate).getTime();
		long endDateTime = DateUtil.getEndOfDay(endDate).getTime();
		long currentDate = DateUtil.getEndOfDay(new Date()).getTime();
		if (endDateTime > currentDate) {
			endDateTime = currentDate;
		}

		long diff = endDateTime - startTime;

		if(TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS) > 30) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Consent-004"), "Consent-004");
		}

		Query query = new Query(Criteria.where("cts")
				.gte(startTime).lte(endDateTime)).with(Sort.by(Sort.Direction.DESC, "_id"));


		List<Consent> consents = mongoTemplate.find(query, Consent.class);

		if(consents.size() == 0 ) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Consent-005"), "Consent-005");
		}


		return xlsService.createConsentAuditXsl("consent-audit", consents, getConsentColumnNames());

	}

	private List<String> getConsentColumnNames() {
		ProjectionOperation projectionOperation = project().and(ObjectOperators.valueOf("consent").toArray()).as("consent");

		UnwindOperation unwindOperation = unwind("consent");

		GroupOperation groupOperation = group("consent.k");

		Aggregation aggregation = newAggregation(projectionOperation, unwindOperation, groupOperation);


		AggregationResults<Document> aggregationResult = mongoTemplate.aggregate(aggregation, Consent.class, Document.class);

		return aggregationResult.getMappedResults().stream().map(d -> d.getString("_id")).collect(Collectors.toList());
	}
}
