package com.itorix.consentserver.dao;

import com.itorix.consentserver.model.*;
import com.mongodb.client.DistinctIterable;
import com.mongodb.client.MongoCursor;
import lombok.SneakyThrows;
import org.bson.types.ObjectId;
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
public class ConsentServerDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    private static final String SCOPE_CATEGORY_COLLECTION = "Consent.ScopeCategory.List";

    public List<String> getScopeCategoryNames() {
        return findDistinctValuesByColumnName(ScopeCategory.class, "_id");
    }


    @SneakyThrows
    public void createConsent(Consent consent) {
        String category = consent.getConsent().get("category");
        Query query = Query.query(Criteria.where("_id").is(category));
        ScopeCategory scopeCategory = mongoTemplate.findOne(query, ScopeCategory.class);
        if(scopeCategory != null) {
            long currentTime = System.currentTimeMillis();
            long expiryTimeInMillis = currentTime + (scopeCategory.getExpiry() * 60 * 1000);
            consent.setCts(System.currentTimeMillis());
            consent.setExpirationTimeInMillis(expiryTimeInMillis);
            mongoTemplate.save(consent);
        } else {
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Consent-001"), category), "Consent-001");
        }

    }

    public Consent getConsentById(String consentId) {
        return mongoTemplate.findById(new ObjectId(consentId), Consent.class);
    }

    public void revokeConsent(String consentId) {
        Query query = new Query(Criteria.where("_id").is(consentId));
        Update update = new Update();
        update.set("status", ConsentStatus.Revoked.name());
        mongoTemplate.upsert(query, update, Consent.class);
    }

    @SneakyThrows
    public ConsentStatus getConsentStatus(String consentId) {
        Consent consent = mongoTemplate.findOne(Query.query(Criteria.where("_id").is(consentId)), Consent.class);
        if(consent != null ) {
            return ConsentStatus.valueOf((consent.getConsent().get("status")));
        }
        throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Consent-002"), consentId), "Consent-002");
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

    public ConsentResponse getConsentsBySearch(int offset, int pageSize, Map<String, String> searchParams) {
        List<Criteria> searchCriteria = getSearchCriteria(searchParams);

        Criteria criteria = new Criteria().andOperator(searchCriteria.toArray(new Criteria[searchCriteria.size()]));

        Query query = searchCriteria.size() > 0 ? Query.query(criteria) : new Query();

        List<Consent> consents = mongoTemplate.find(query.with(Sort.by(Sort.Direction.DESC, "cts"))
                .skip(offset > 0 ? ((offset - 1) * pageSize) : 0).limit(pageSize), Consent.class);

        Pagination pagination = getPagination(offset, pageSize);
        ConsentResponse consentResponse = new ConsentResponse();
        consentResponse.setPagination(pagination);
        consentResponse.setConsentList(consents);
        return consentResponse;

    }

    private List<Criteria> getSearchCriteria(Map<String, String> searchCriteria) {
        List<Criteria> criteriaList = new ArrayList<>();
        searchCriteria.forEach((k, v) -> {
            if (null != v) {
                criteriaList.add(Criteria.where("consent." + k).is(v));
            }
        });
        return criteriaList;
    }


    @SneakyThrows
    public void updateConsentScope(String consentId, List<String> scopes) {
        Consent consent = mongoTemplate.findOne(Query.query(Criteria.where("_id").is(consentId)), Consent.class);
        if(consent != null ) {
            consent.setScopes(scopes);
            mongoTemplate.save(consent);
            return;
        }
        throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Consent-002"), consentId), "Consent-002");
    }
}
