package com.itorix.apiwiz.monitor.agent.executor;

import com.itorix.apiwiz.monitor.agent.dao.MonitorAgentExecutorDao;
import com.itorix.apiwiz.monitor.agent.dao.MonitorAgentExecutorSQLDao;
import com.itorix.apiwiz.monitor.model.ExecutionContext;
import com.itorix.apiwiz.monitor.model.Variables;
import com.itorix.apiwiz.monitor.model.collection.MonitorCollections;
import com.itorix.apiwiz.monitor.model.collection.Schedulers;
import com.itorix.apiwiz.monitor.model.request.Header;
import com.itorix.apiwiz.monitor.model.request.MonitorRequest;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.message.BasicStatusLine;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

import static org.junit.Assert.*;

public class MonitorAgentRunnerTest {

    @InjectMocks
    MonitorAgentRunner monitorAgentRunner = new MonitorAgentRunner();

    @Mock
    private MonitorAgentExecutorDao dao;

    @Mock
    private MonitorAgentExecutorSQLDao sqlDao;

    @Mock
    private MonitorAgentHelper helper;


    @Before
    public void setupMock() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void checkNotificationAgentInvoked() throws Exception {
        ExecutionContext ctx = spy(ExecutionContext.class);
        ctx.setCollectionId("12345");
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
        monitorRequest.setId("12345");
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
        response.setStatusLine(statusLine);
        when(response.getStatusLine()).thenReturn(statusLine);
        when(statusLine.getStatusCode()).thenReturn(200);


        when(dao.getScheduler(Mockito.anyString(), Mockito.anyString())).thenReturn(scheduler);
        when(dao.getMonitorCollections(Mockito.anyString(), Mockito.anyString())).thenReturn(collection);
        when(dao.getVariablesById(Mockito.anyString())).thenReturn(vars);
        when(dao.getMonitorRequests(Mockito.anyString())).thenReturn(monitorRequests);
        when(dao.getRequestSequence(Mockito.eq(ctx.getCollectionId()))).thenReturn(requestSequence);
        when(helper.invokeMonitorApi(Mockito.any(), Mockito.anyMap(), Mockito.anyMap(), Mockito.anyMap(), Mockito.any(), Mockito.anyInt())).thenReturn(response);


        monitorAgentRunner.executeMonitorRequests(ctx, 3000);


    }

}