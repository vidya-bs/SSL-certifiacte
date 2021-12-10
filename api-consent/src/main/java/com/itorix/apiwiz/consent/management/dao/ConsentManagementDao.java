package com.itorix.apiwiz.consent.management.dao;

import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.consent.management.model.*;
import com.itorix.apiwiz.identitymanagement.dao.BaseRepository;
import com.itorix.apiwiz.identitymanagement.model.Pagination;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class ConsentManagementDao {

	@Autowired
	private BaseRepository baseRepository;

	@Autowired
	private MongoTemplate mongoTemplate;

	public void save(ScopeCategory scopeCategory) throws ItorixException {
		ScopeCategory existingScopeCategory = baseRepository.findById(scopeCategory.getName(), ScopeCategory.class);

		if (existingScopeCategory != null) {
			throw new ItorixException(String.format(ErrorCodes.errorMessage.get("ScopeCategory-001"), scopeCategory.getName()), "ScopeCategory-001");
		}
		scopeCategory.setCts(System.currentTimeMillis());
		scopeCategory.setMts(System.currentTimeMillis());
		mongoTemplate.save(scopeCategory);
	}

	@SneakyThrows
	public void update(ScopeCategory scopeCategory) {
		ScopeCategory existingScopeCategory = baseRepository.findById(scopeCategory.getName(), ScopeCategory.class);

		if (existingScopeCategory == null) {
			throw new ItorixException(String.format(ErrorCodes.errorMessage.get("ScopeCategory-002"), scopeCategory.getName()), "ScopeCategory-002");
		}

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
		List<Criteria> searchCriteria = getCriteria(searchParams);

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
}
