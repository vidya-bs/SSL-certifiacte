package com.itorix.apiwiz.identitymanagement.dao;

import com.hazelcast.map.IMap;
import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.identitymanagement.model.TenantContext;
import com.itorix.apiwiz.identitymanagement.model.Workspace;
import com.itorix.apiwiz.ratelimit.configuration.HazelCastConfiguration;
import com.itorix.apiwiz.ratelimit.model.ApplicationUsageModel;
import com.itorix.apiwiz.ratelimit.model.RateLimitQuota;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.TokensInheritanceStrategy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.grid.hazelcast.HazelcastProxyManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
@EnableScheduling
public class RateLimitingDao {

    @Autowired
    private HazelCastConfiguration hazelCastConfiguration;

    @Autowired
    private IMap<String, byte[]> bucketCache;

    @Autowired
    private IMap<String, Map<String, Integer>> applicationUsage;

    @Qualifier("masterMongoTemplate")
    @Autowired
    private MongoTemplate masterMongoTemplate;

    @Autowired
    private MongoTemplate mongoTemplate;

    private static final String RATE_LIMIT_1000 = "rate-limiting-1000";


//    @Scheduled(fixedRate = 3600000)
    public void scheduledApplicationUsageTracking() {
        log.debug("Scheduled Application usage tracking ...");
        if (applicationUsage != null && !applicationUsage.isEmpty()) {
            for (Map.Entry<String, Map<String, Integer>> workspaceUsage : applicationUsage.entrySet()) {
                if (workspaceUsage.getKey() != null && workspaceUsage.getValue() != null && !workspaceUsage.getValue().isEmpty()) {
                    try {
                        Workspace workspace = checkTenant(workspaceUsage.getKey());
                        TenantContext.setCurrentTenant(workspace.getTenant());
                        mongoTemplate.save(new ApplicationUsageModel(workspace.getTenant(), workspaceUsage.getValue(), workspace.getPlanId()));
                    } catch (Exception e) {
                        log.error(" Exception occurred in tracking Application Usage - {}", e.getMessage());
                    }
                }
            }
        }
    }

    public void addTenantQuotas(String tenant, RateLimitQuota quota) throws ItorixException {
        log.info("Adding rate limit quota to tenant DB...");
        if (!hazelCastConfiguration.isHazelcastConnectionStatus()) {
            throw new ItorixException(ErrorCodes.errorMessage.get(RATE_LIMIT_1000), RATE_LIMIT_1000);
        }
        if (quota == null || quota.getPlan() == null || quota.getRequests() == null){
            log.error("Invalid Quota.");
            throw new ItorixException(ErrorCodes.errorMessage.get("rate-limiting-1001"), "rate-limiting-1001");
        }
        Workspace workspace = checkTenant(tenant);
        if (!workspace.getPlanId().equalsIgnoreCase(quota.getPlan())){
            throw new ItorixException(ErrorCodes.errorMessage.get("rate-limiting-1002"), "rate-limiting-1002");
        }
        TenantContext.setCurrentTenant(tenant);
        try {
            updateCache(tenant, quota);
        } catch (Exception e) {
            log.error("Error while loading Quotas in Cache - workspace {} \n Error : {}", tenant, e);
            throw new ItorixException(ErrorCodes.errorMessage.get("rate-limiting-1003"), "rate-limiting-1003");
        }
        log.info("Removing old tenant quota for workspace {}", tenant);
        removeTenantQuota();
        storeTenantQuotas(quota);
        log.info("Added rate limit quotas to tenant DB.");
    }

    public void addMasterQuotas(List<RateLimitQuota> quotas) throws ItorixException {
        log.info("Adding rate limit quotas to master DB...");
        if (quotas == null || quotas.isEmpty()) {
            log.error("Quotas list is empty.");
            throw new ItorixException(ErrorCodes.errorMessage.get("rate-limiting-1004"), "rate-limiting-1004");
        }

        log.info("Removing old master quotas");
        removeMasterQuota();

        for (RateLimitQuota quota : quotas) {
            quota.setCts(System.currentTimeMillis());
            quota.setMts(System.currentTimeMillis());
            if (quota.getPlan() != null) {
                masterMongoTemplate.save(quota);
            }
        }
        log.info("Added rate limit quotas to master DB.");
    }

    public List<ApplicationUsageModel> getApplicationUsage() {
        String workspace = TenantContext.getCurrentTenant();
        log.info("Getting application usage for workspace - {}...", workspace);
        return mongoTemplate.findAll(ApplicationUsageModel.class);
    }

    private void updateCache(String tenant, RateLimitQuota quota) throws ItorixException {
        log.info("Updating cache in hazelcast...");
        if (hazelCastConfiguration.isHazelcastConnectionStatus()) {
            if (bucketCache != null && !bucketCache.isEmpty()) {
                ProxyManager<String> proxyManager = new HazelcastProxyManager<>(bucketCache);
                Optional<BucketConfiguration> bucketConfiguration = proxyManager.getProxyConfiguration(tenant);
                if (bucketConfiguration.isPresent()) {
                    Bucket bucket = proxyManager.builder().build(tenant, bucketConfiguration.get());
                    BucketConfiguration newBucketConfiguration = BucketConfiguration.builder().addLimit(quota.getBandwidthLimit()).build();
                    bucket.replaceConfiguration(newBucketConfiguration, TokensInheritanceStrategy.RESET);
                    log.info("Updated Rate Limit Quotas for workspace {} in Cache", tenant);
                }
                log.info("Updated Rate Limit Quotas for workspace {} in Cache", tenant);
            }
        } else {
            throw new ItorixException(ErrorCodes.errorMessage.get(RATE_LIMIT_1000), RATE_LIMIT_1000);
        }
    }

    private void storeTenantQuotas(RateLimitQuota quota) {
        quota.setCts(System.currentTimeMillis());
        quota.setMts(System.currentTimeMillis());
        mongoTemplate.save(quota);
    }

    private void removeTenantQuota() {
        mongoTemplate.dropCollection(RateLimitQuota.class);
    }

    private void removeMasterQuota(){
        masterMongoTemplate.dropCollection(RateLimitQuota.class);
    }

    private Workspace checkTenant(String tenant) throws ItorixException {
        Query query = new Query();
        query.addCriteria(Criteria.where("tenant").is(tenant));
        Workspace workspace = masterMongoTemplate.findOne(query, Workspace.class);
        if (workspace == null) {
            String errorMessage = String.format(ErrorCodes.errorMessage.get("Identity-1011"));
            log.error("{} - Workspace : {}", errorMessage, tenant);
            throw new ItorixException(errorMessage, "Identity-1011");
        }
        return workspace;
    }
}
