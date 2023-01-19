package com.itorix.apiwiz.data.management.businessimpl;

import com.itorix.apiwiz.identitymanagement.model.Workspace;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
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
@Slf4j
@Component
public class OrganizationBackupScheduler {

	@Autowired
	private MongoProperties mongoProperties;
	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private OrganizationBusiness organizationBusiness;

	@Scheduled(fixedRate = 10000)
	public void executeBackupEvent() {

		try (MongoClient mongoClient = new MongoClient(new MongoClientURI(mongoProperties.getUri()));) {
			MongoDatabase mongoDatabase = mongoClient.getDatabase(mongoProperties.getDatabase());

			MongoCollection collection = mongoDatabase.getCollection("Users.Workspace.List");
			MongoCursor<Document> dbsCursor = collection.find().iterator();
			while (dbsCursor.hasNext()) {
				Document workSpace = dbsCursor.next();
				TenantContext.setCurrentTenant(workSpace.getString("tenant"));
				String tenant = TenantContext.getCurrentTenant();
				log.info("Tenant : {}", tenant);
				Query query = new Query(Criteria.where("status").is(Constants.STATUS_SCHEDULED));
				BackupEvent backupEvent = mongoTemplate.findOne(query, BackupEvent.class);
				if (null != backupEvent) {
					backupEvent.setStatus(Constants.STATUS_PROCESSED);
					mongoTemplate.save(backupEvent);
					if (backupEvent != null) {
						switch (backupEvent.getEvent().toUpperCase()) {
							case "BACKUPPROXIES" : {
								organizationBusiness.backupProxies(backupEvent.getCfg(), backupEvent.getEventId());
								break;
							}
							case "BACKUPSHAREDFLOW" : {
								organizationBusiness.backupSharedflows(backupEvent.getCfg(), backupEvent.getEventId());
								break;
							}
							case "BACKUPAPPS" : {
								organizationBusiness.backUpApps(backupEvent.getCfg(), backupEvent.getEventId());
								break;
							}
							case "BACKUPPRODUCTS" : {
								organizationBusiness.backupProducts(backupEvent.getCfg(), backupEvent.getEventId());
								break;
							}
							case "BACKUPDEVELOPERS" : {
								organizationBusiness.backupDevelopers(backupEvent.getCfg(), backupEvent.getEventId());
								break;
							}
							case "BACKUPRESOURCES" : {
								organizationBusiness.backupResources(backupEvent.getCfg(), backupEvent.getEventId());
								break;
							}
							case "BACKUPORGANIZATION" : {
								organizationBusiness.backUpOrganization(backupEvent.getCfg(), backupEvent.getEventId());
								break;
							}
							case "BACKUPCACHES" : {
								organizationBusiness.backupCaches(backupEvent.getCfg(), backupEvent.getEventId());
								break;
							}
							case "BACKUPKVM" : {
								organizationBusiness.backupKVM(backupEvent.getDelete(), backupEvent.getCfg(),
										backupEvent.getEventId());
								break;
							}
							case "BACKUPTARGETSERVERS" : {
								organizationBusiness.backupTargetServers(backupEvent.getCfg(),
										backupEvent.getEventId());
								break;
							}
							case "RESTOREAPIPROXIES" : {
								organizationBusiness.restoreApiProxies(backupEvent.getCfg(), backupEvent.getEventId());
								break;
							}
							case "RESTORESHAREDFLOWS" : {
								organizationBusiness.restoreSharedflows(backupEvent.getCfg(), backupEvent.getEventId());
								break;
							}
							case "RESTOREAPPS" : {
								organizationBusiness.restoreAPPs(backupEvent.getCfg(), backupEvent.getEventId());
								break;
							}
							case "RESTOREPRODUCTS" : {
								organizationBusiness.restoreAPIProducts1(backupEvent.getCfg(),
										backupEvent.getEventId());
								break;
							}
							case "RESTOREDEVELOPERS" : {
								organizationBusiness.restoreAppDevelopers1(backupEvent.getCfg(),
										backupEvent.getEventId());
								break;
							}
							case "RESTOREORGANIZATION" : {
								organizationBusiness.restoreOrganization(backupEvent.getCfg(),
										backupEvent.getEventId());
								break;
							}
							case "RESTORERESOURCES" : {
								organizationBusiness.restoreResources(backupEvent.getCfg(), backupEvent.getEventId());
								break;
							}
							case "MIGRATEORGANIZATION" : {
								organizationBusiness.migrateOrganization(backupEvent.getCfg(),
										backupEvent.getEventId());
								break;
							}

						}
					}
				}
			}
		} catch (Exception e) {
			log.error("Exception occurred", e);
		}
	}

}
