package com.itorix.apiwiz.consent.management.serviceImpl;

import com.itorix.apiwiz.common.properties.ApplicationProperties;
import com.itorix.apiwiz.consent.management.model.Consent;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClients;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class XlsServiceTest {

    private static MongoTemplate mongoTemplate = null;

    @InjectMocks
    private XlsService xlsService;

    @Mock
    private ApplicationProperties applicationProperties;

    @BeforeClass
    public static void init() {
        ConnectionString connectionString = new ConnectionString("mongodb://localhost:27017");
        MongoClientSettings mongoClientSettings = MongoClientSettings.builder().applyConnectionString(connectionString)
                .build();
        MongoClients.create(mongoClientSettings);
        mongoTemplate = new MongoTemplate(MongoClients.create(mongoClientSettings), "acme-team-dev");
    }

    @Test
    public void createConsentAuditXsl() throws IOException {

        List<Consent> all = mongoTemplate.findAll(Consent.class);

        when(applicationProperties.getBackupDir()).thenReturn("/Users/balajivijayan/Projects/apiwiz/consent-management/temp");

        xlsService.createConsentAuditXsl("consent-audit", all, Arrays.asList("lastName", "category", "userId", "firstName", "UserID", "status"));

    }
}