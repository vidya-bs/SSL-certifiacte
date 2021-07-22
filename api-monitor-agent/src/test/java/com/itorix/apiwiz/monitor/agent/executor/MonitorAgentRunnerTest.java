package com.itorix.apiwiz.monitor.agent.executor;

import com.itorix.apiwiz.monitor.agent.config.MonitorAgentConfigTest;
import com.itorix.apiwiz.monitor.agent.dao.MonitorAgentExecutorDao;
import com.itorix.apiwiz.monitor.agent.dao.MonitorAgentExecutorSQLDao;
import com.itorix.apiwiz.monitor.agent.logging.LoggerService;
import com.itorix.apiwiz.monitor.agent.util.MonitorAgentConstants;
import com.itorix.apiwiz.monitor.model.ExecutionContext;
import com.itorix.apiwiz.monitor.model.NotificationDetails;
import com.itorix.apiwiz.monitor.model.Variables;
import com.itorix.apiwiz.monitor.model.collection.MonitorCollections;
import com.itorix.apiwiz.monitor.model.collection.Notifications;
import com.itorix.apiwiz.monitor.model.collection.Schedulers;
import com.itorix.apiwiz.monitor.model.request.Body;
import com.itorix.apiwiz.monitor.model.request.Header;
import com.itorix.apiwiz.monitor.model.request.MonitorRequest;
import com.itorix.apiwiz.monitor.model.request.Response;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.message.BasicStatusLine;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = MonitorAgentConfigTest.class)
public class MonitorAgentRunnerTest {

    @Autowired
    MonitorAgentRunner monitorAgentRunner;

    @MockBean
    private MonitorAgentExecutorDao dao;

    @MockBean
    private MonitorAgentExecutorSQLDao sqlDao;

    @MockBean
    private MonitorAgentHelper helper;

    @MockBean
    private EmailContentParser emailContentParser;

    @MockBean
    LoggerService loggerService;

    @Before
    public void setupMock() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void checkNotificationAgentInvokedForLatencyBreach() throws Exception {
        ExecutionContext ctx = spy(ExecutionContext.class);
        ctx.setCollectionId("12345");
        ctx.setTenant("testTenant");
        ctx.setSchedulerId("12345");
        MonitorCollections collection = spy(MonitorCollections.class);
        Schedulers scheduler = spy(Schedulers.class);
        scheduler.setTimeout(60000);
        scheduler.setEnvironmentId("UnitTest");
        List<Schedulers> schedulersList = new ArrayList();
        schedulersList.add(scheduler);
        List<MonitorRequest> monitorRequests = new ArrayList<>();
        MonitorRequest monitorRequest = spy(MonitorRequest.class);
        monitorRequest.setVerb(MonitorAgentRunner.API.GET.toString());
        monitorRequest.setLatencyAlert(true);
        monitorRequest.setId("12345");
        Response monitorResponse = new Response();
        Body body = new Body();
        body.setData("testData");
        monitorResponse.setBody(body);
        monitorRequest.setResponse(monitorResponse);
        monitorRequests.add(monitorRequest);
        Variables vars = new Variables();
        vars.setName("Test");
        vars.setDescription("Test Desc");
        Header header = new Header();
        header.setName("testHeader");
        header.setValue("testHeaderValue");
        List<Header> headers = new ArrayList<>();
        headers.add(header);
        vars.setVariables(headers);
        List<String> requestSequence = new ArrayList<>();
        requestSequence.add("12345");
        collection.setSchedulers(schedulersList);
        collection.setMonitorRequest(monitorRequests);
        HttpResponse response = spy(HttpResponse.class);
        response.setStatusCode(200);
        StatusLine statusLine = mock(BasicStatusLine.class);
        NotificationDetails notificationDetails = getNotificationDetails();
        Notifications notifications = getNotifications();
        List<Notifications> notificationsList = new ArrayList<>();
        notificationsList.add(notifications);
        notificationDetails.setNotifications(notificationsList);
        List<NotificationDetails> notificationDetailsList = new ArrayList<>();
        notificationDetailsList.add(notificationDetails);
        when(dao.getNotificationDetails(Mockito.anyString(), Mockito.anyString())).thenReturn(notificationDetailsList);

        response.setStatusLine(statusLine);
        when(response.getStatusLine()).thenReturn(statusLine);
        when(statusLine.getStatusCode()).thenReturn(200);
        when(dao.getScheduler(Mockito.anyString(), Mockito.anyString())).thenReturn(scheduler);
        when(dao.getMonitorCollections(Mockito.anyString(), Mockito.anyString())).thenReturn(collection);
        when(dao.getVariablesById(Mockito.anyString())).thenReturn(vars);
        when(dao.getMonitorRequests(Mockito.anyString())).thenReturn(monitorRequests);
        when(dao.getRequestSequence(Mockito.eq(ctx.getCollectionId()))).thenReturn(requestSequence);
        when(helper.invokeMonitorApi(Mockito.any(), Mockito.anyMap(), Mockito.anyMap(), Mockito.anyMap(), Mockito.any(),
                Mockito.anyInt())).thenReturn(response);

        monitorAgentRunner.executeMonitorRequests(ctx, 3000);

        Mockito.verify(emailContentParser, times(1)).getRelevantEmailContent(Mockito.any(), Mockito.anyMap());

    }

    private Notifications getNotifications() {
        Notifications notifications = new Notifications();
        notifications.setType("EMAIL");
        notifications.setEmails(Arrays.asList("test@itorix.com"));
        return notifications;
    }

    private NotificationDetails getNotificationDetails() {
        NotificationDetails notificationDetails = spy(NotificationDetails.class);
        notificationDetails.setEnvironmentName("testEnv");
        notificationDetails.setWorkspaceName("testWorkspace");
        notificationDetails.setDate("5-Jun-2019");
        return notificationDetails;
    }

}