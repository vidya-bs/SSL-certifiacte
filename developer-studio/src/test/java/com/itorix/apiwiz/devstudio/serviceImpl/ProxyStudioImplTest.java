package com.itorix.apiwiz.devstudio.serviceImpl;

import com.itorix.apiwiz.common.model.proxystudio.CodeGenHistory;
import com.itorix.apiwiz.devstudio.businessImpl.CodeGenService;
import com.itorix.apiwiz.devstudio.model.Operations;
import com.itorix.apiwiz.devstudio.model.ProxyGenResponse;
import com.itorix.apiwiz.devstudio.serviceImpl.ProxyStudioImpl;
import com.itorix.apiwiz.identitymanagement.dao.IdentityManagementDao;
import com.itorix.apiwiz.identitymanagement.model.User;
import com.itorix.apiwiz.performance.coverge.businessimpl.CommonServices;
import javax.servlet.ServletContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class ProxyStudioImplTest {

  @Mock
  private CodeGenService codeGenService;

  @Mock
  private IdentityManagementDao commonServices;

  @InjectMocks
  private ProxyStudioImpl proxyStudio;

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @Mock
  private ServletContext servletContext;

  @BeforeEach
  public void setup() {
    MockitoAnnotations.openMocks(this);


  }

  @Test
  public void testUpdateProxyDetailsSuccess() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    when(request.getServletContext()).thenReturn(servletContext);
    when(servletContext.getRealPath(anyString())).thenReturn("/");

    CodeGenHistory codeGen = new CodeGenHistory();
    HttpHeaders headers = new HttpHeaders();

    User user = new User();
    when(commonServices.getUserDetailsFromSessionID(anyString())).thenReturn(user);

    ProxyGenResponse ProxyGenResponse = new ProxyGenResponse();
    when(codeGenService.processCodeGen(any(CodeGenHistory.class), any(Operations.class), isNull()))
        .thenReturn(ProxyGenResponse);

    ResponseEntity<?> responseEntity = proxyStudio.updateProxyDetails("interactionId", "jsessionid",
        codeGen, headers, request, response);

    verify(request).getServletContext();
    verify(response).setContentType("application/json");
    verify(commonServices).getUserDetailsFromSessionID("jsessionid");
    verify(codeGenService).processCodeGen(eq(codeGen), any(Operations.class), isNull());

    assert responseEntity != null;
    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    assertEquals(ProxyGenResponse, responseEntity.getBody());
  }

  @Test
  public void testUpdateProxyDetailsFailure() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    when(request.getServletContext()).thenReturn(servletContext);
    when(servletContext.getRealPath(anyString())).thenReturn("/");

    CodeGenHistory codeGen = new CodeGenHistory();
    HttpHeaders headers = new HttpHeaders();

    User user = new User();
    when(commonServices.getUserDetailsFromSessionID(anyString())).thenReturn(user);

    ProxyGenResponse ProxyGenResponse = new ProxyGenResponse();
    when(codeGenService.processCodeGen(any(CodeGenHistory.class), any(Operations.class), isNull()))
        .thenReturn(ProxyGenResponse);

    ResponseEntity<?> responseEntity = proxyStudio.updateProxyDetails("interactionId", "jsessionid",
        codeGen, headers, request, response);

    verify(request).getServletContext();
    verify(response).setContentType("application/json");
    verify(commonServices).getUserDetailsFromSessionID("jsessionid");
    verify(codeGenService).processCodeGen(eq(codeGen), any(Operations.class), isNotNull());

    assert responseEntity != null;
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
    assertEquals(ProxyGenResponse, responseEntity.getBody());
  }
}
