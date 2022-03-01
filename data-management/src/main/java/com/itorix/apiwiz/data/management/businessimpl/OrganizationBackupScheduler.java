package com.itorix.apiwiz.data.management.businessimpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.itorix.apiwiz.common.model.Constants;
import com.itorix.apiwiz.data.management.business.OrganizationBusiness;
import com.itorix.apiwiz.data.management.model.BackupEvent;
import com.itorix.apiwiz.identitymanagement.model.TenantContext;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCursor;

@Component
public class OrganizationBackupScheduler {

	@Autowired
	private MongoProperties mongoProperties;
	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private OrganizationBusiness organizationBusiness;

	@Scheduled(fixedRate = 10000)
	public void executeBackupEvent(){

		try (MongoClient mongoClient = new MongoClient(new MongoClientURI(mongoProperties.getUri()));) {
			MongoCursor<String> dbsCursor = mongoClient.listDatabaseNames().iterator();
			while (dbsCursor.hasNext()) {
				String workSpace = dbsCursor.next();
				TenantContext.setCurrentTenant(workSpace);
				Query query = new Query(Criteria.where("status").is(Constants.STATUS_SCHEDULED));
				BackupEvent backupEvent = mongoTemplate.findOne(query, BackupEvent.class);
				if(null != backupEvent){
					backupEvent.setStatus(Constants.STATUS_PROCESSED);
					mongoTemplate.save(backupEvent);
					if(backupEvent != null){
						switch(backupEvent.getEvent().toUpperCase()){  
							case "BACKUPPROXIES":{
								organizationBusiness.backupProxies(backupEvent.getCfg(), backupEvent.getEventId());
								break;
							}
							case "BACKUPSHAREDFLOW":{
								organizationBusiness.backupSharedflows(backupEvent.getCfg(), backupEvent.getEventId());
								break;
							}
							case "BACKUPAPPS":{
								organizationBusiness.backUpApps(backupEvent.getCfg(), backupEvent.getEventId());
								break;
							}
							case "BACKUPPRODUCTS":{
								organizationBusiness.backupProducts(backupEvent.getCfg(), backupEvent.getEventId());
								break;
							}
							case "BACKUPDEVELOPERS":{
								organizationBusiness.backupDevelopers(backupEvent.getCfg(), backupEvent.getEventId());
								break;
							}
							case "BACKUPRESOURCES":{
								organizationBusiness.backupResources(backupEvent.getCfg(), backupEvent.getEventId());
								break;
							}
							case "BACKUPORGANIZATION":{
								organizationBusiness.backUpOrganization(backupEvent.getCfg(), backupEvent.getEventId());
								break;
							}
							case "BACKUPCACHES":{
								organizationBusiness.backupCaches(backupEvent.getCfg(), backupEvent.getEventId());
								break;
							}
							case "BACKUPKVM":{
								organizationBusiness.backupKVM(backupEvent.getDelete(), backupEvent.getCfg(), backupEvent.getEventId());
								break;
							}
							case "BACKUPTARGETSERVERS":{
								organizationBusiness.backupTargetServers(backupEvent.getCfg(), backupEvent.getEventId());
								break;
							}
							
						}
					}
				}
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	

}
