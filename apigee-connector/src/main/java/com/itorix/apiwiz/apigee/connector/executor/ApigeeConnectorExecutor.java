package com.itorix.apiwiz.apigee.connector.executor;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.itorix.apiwiz.apigee.connector.dao.ApigeeConnectorDao;
import com.itorix.apiwiz.apigee.connector.dao.ApigeeSQLDao;
import com.itorix.apiwiz.apigee.connector.db.ExecutorEntity;
import com.itorix.apiwiz.apigee.connector.model.ItorixException;
import com.itorix.apiwiz.apigee.connector.model.TenantContext;
import com.itorix.apiwiz.apigee.connector.model.apigee.ApigeeEnvProxies;

@Configuration
@EnableAutoConfiguration
@Component
@EnableScheduling
public class ApigeeConnectorExecutor {
	
	private static final Logger log = LoggerFactory.getLogger(ApigeeConnectorExecutor.class);
	
	@Autowired
	private ApigeeSQLDao sqlDao;
	
	@Autowired
	private ApigeeConnectorDao apigeeConnectorDao;
	
	@Autowired
	private ListProxyNames listProxyNames;
	
	@Scheduled(fixedRate = 10000)
    public void fetchAndRunTasks() {
		List<ExecutorEntity> progressEntities = sqlDao.getExecutorEntityByColumn("status",
                ExecutorEntity.STATUSES.IN_PROGRESS.getValue() );
		if(CollectionUtils.isEmpty(progressEntities))
		{
			List<ExecutorEntity> executorEntities = sqlDao.getExecutorEntityByColumn("status",
	                ExecutorEntity.STATUSES.SCHEDULED.getValue() );
			for (ExecutorEntity executorEntity : executorEntities) {
				executorEntity.setStatus(ExecutorEntity.STATUSES.IN_PROGRESS.getValue());
	            sqlDao.updateField(executorEntity.getId(), "status",
	            		ExecutorEntity.STATUSES.IN_PROGRESS.getValue());
	            TenantContext.setCurrentTenant(executorEntity.getTenant());
	            String event = executorEntity.getEvent();
	            switch(event.toUpperCase()){  
	            	case "PROXYLIST":{
	            		log.info("executing get proxy list");
	            		try {
	            			List<String> proxies = listProxyNames.getEnvironmentDepolyedProxies(executorEntity);
	            			ApigeeEnvProxies apigeeEnvProxies = new ApigeeEnvProxies();
	            			apigeeEnvProxies.setOrg(executorEntity.getOrg());
	            			apigeeEnvProxies.setEnv(executorEntity.getEnv());
	            			apigeeEnvProxies.setType(executorEntity.getType());
	            			apigeeEnvProxies.setProxies(proxies);
	            			apigeeConnectorDao.SaveProxies(apigeeEnvProxies);
	            		} catch (ItorixException e) {
	            			log.error(e.getMessage(),e);
							e.printStackTrace();
						} finally{
							sqlDao.updateField(executorEntity.getId(), "status",ExecutorEntity.STATUSES.COMPLETED.getValue());
						}
	            		break;
	            	}
	            	case "PROXYDETAILS":{
	            		log.info("executing update proxy details");
	            		break;
	            	}
	            	case "CODECOVERAGE":{
	            		log.info("executing codecoverage");
	            		break;
	            	}
	            	default : System.out.println("");
	            }
			}
			
		}
		
		 
		 
	}

}
