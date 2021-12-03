package com.itorix.consentserver.dao;

import com.itorix.consentserver.common.model.*;
import com.mongodb.client.DistinctIterable;
import com.mongodb.client.MongoCursor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
public class ConsentServerDao {

    @Autowired
    private MongoTemplate mongoTemplate;


    public void save(ScopeCategory scopeCategory) {
        scopeCategory.setCts(System.currentTimeMillis());
        scopeCategory.setMts(System.currentTimeMillis());
        mongoTemplate.save(scopeCategory);
    }

    @SneakyThrows
    public void update(ScopeCategory scopeCategory) {
        ScopeCategory existingScopeCategory = mongoTemplate.findById(scopeCategory.getName(), ScopeCategory.class);

        if(existingScopeCategory == null ) {
            throw new ItorixException("Sorry! Invalid request. Please correct the request and try again.");
        }

        existingScopeCategory.setMts(System.currentTimeMillis());
        existingScopeCategory.setDisplayName(scopeCategory.getDisplayName());
        existingScopeCategory.setDescription(scopeCategory.getDescription());
        existingScopeCategory.setSummary(scopeCategory.getSummary());
        existingScopeCategory.setExpiry(scopeCategory.getExpiry());
        existingScopeCategory.setScopes(scopeCategory.getScopes());

        mongoTemplate.save(existingScopeCategory);

    }

    public List<ScopeCategory> getAllScopeCategory() {
        return mongoTemplate.findAll(ScopeCategory.class);
    }

    public List<String> getScopeCategoryNames() {
        return findDistinctValuesByColumnName(ScopeCategory.class, "_id");
    }

    public ScopeCategory getScopeCategoryByName(String categoryName) {
        ScopeCategory scopeCategory = mongoTemplate.findOne(Query.query(Criteria.where("name").is(categoryName)), ScopeCategory.class);
        if(scopeCategory == null ) {
            scopeCategory = mongoTemplate.findOne(Query.query(Criteria.where("displayName").is(categoryName)), ScopeCategory.class);
        }
        return scopeCategory;
    }

    public void deleteScopeCategory(String name) {
        mongoTemplate.remove(Query.query(Criteria.where("name").is(name)), ScopeCategory.class);
    }


    public void createOrUpdateScopeCategoryColumns(ScopeCategoryColumns scopeCategoryColumns) {
        Query query = new Query();
        query.addCriteria(Criteria.where("name").exists(true));
        Update update = new Update();
        update.set("name", scopeCategoryColumns.getColumns());
        mongoTemplate.upsert(query, update, ScopeCategoryColumns.class);
    }

    public ScopeCategoryColumns getScopeCategoryColumns() {
        List<ScopeCategoryColumns> findAll = mongoTemplate.findAll(ScopeCategoryColumns.class);
        return findAll.isEmpty() ? null : findAll.get(0);
    }

    public void createConsent(Consent consent) {
        consent.setCts(System.currentTimeMillis());
        mongoTemplate.save(consent);
    }

    public Consent getConsentByPrimaryKey(String userId) {
        return mongoTemplate.findOne(Query.query(Criteria.where("userId").is(userId)), Consent.class);
    }

    public void revokeConsent(String consentId) {
        Query query = new Query(Criteria.where("_id").is(consentId));
        Update update = new Update();
        update.set("status", "REVOKED");
        mongoTemplate.upsert(query, update, Consent.class);
    }

    @SneakyThrows
    public ConsentStatus getConsentStatus(String userId) {
        Consent consent = mongoTemplate.findOne(Query.query(Criteria.where("userId").is(userId)), Consent.class);
        if(consent != null ) {
            return consent.getStatus();
        }
        throw new ItorixException("Sorry! Invalid request. Please correct the request and try again.");
    }

    public ConsentResponse getConsentsOverview(int offset, int pageSize, String consentStatus, String category) {

        List<Criteria> criteriaList = getCriteria(consentStatus, category);

        Criteria criteria = new Criteria().andOperator(criteriaList.toArray(new Criteria[criteriaList.size()]));

        Query query = criteriaList.size() > 0 ? Query.query(criteria) : new Query();

        List<Consent> consents = mongoTemplate.find(query.with(Sort.by(Sort.Direction.DESC, "cts"))
                .skip(offset > 0 ? ((offset - 1) * pageSize) : 0).limit(pageSize), Consent.class);

        Pagination pagination = getPagination(offset, pageSize);
        ConsentResponse consentResponse = new ConsentResponse();
        consentResponse.setPagination(pagination);
        consentResponse.setConsentList(consents);
        return consentResponse;
    }

    private List<Criteria> getCriteria(String consentStatus, String category) {
        HashMap<String, String> filterColumns = new HashMap<>();
        filterColumns.put("status", consentStatus);
        filterColumns.put("category", category);

        List<Criteria> criteriaList = new ArrayList<>();
        filterColumns.forEach((k, v) -> {
            if (null != v) {
                criteriaList.add(Criteria.where(k).is(v));
            }
        });
        return criteriaList;
    }

    private Pagination getPagination(int offset, int pageSize) {
        Pagination pagination = new Pagination();
        pagination.setPageSize(pageSize);
        pagination.setOffset(offset);
        pagination.setTotal(getTotal(Consent.class));
        return pagination;
    }

    private long getTotal(Class clazz) {
        return mongoTemplate.findAll(clazz).size();
    }



    private List<String> getList(DistinctIterable<String> iterable) {
        MongoCursor<String> cursor = iterable.iterator();
        List<String> list = new ArrayList<>();
        while (cursor.hasNext()) {
            list.add(cursor.next());
        }
        return list;
    }

    public <T> List<String> findDistinctValuesByColumnName(Class<T> clazz, String columnName) {
        return getList(
                mongoTemplate.getCollection(mongoTemplate.getCollectionName(clazz)).distinct(columnName, String.class));
    }

}
