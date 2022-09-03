import static org.junit.Assert.*;

import org.apache.poi.ss.formula.functions.T;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.itorix.apiwiz.apimonitor.model.Variables;
import com.itorix.apiwiz.apimonitor.serviceimpl.ApiMonitorServiceImpl;
import com.itorix.apiwiz.common.model.AbstractObject;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.identitymanagement.dao.IdentityManagementDao;
import com.itorix.apiwiz.identitymanagement.model.User;
import com.itorix.apiwiz.apimonitor.dao.ApiMonitorDAO;
import com.itorix.apiwiz.apimonitor.model.Header;
@RunWith(MockitoJUnitRunner.class)

public class ApiMonitorServiceImplTest {
	@Mock
	private IdentityManagementDao commonServices;

	@Mock
	private ApiMonitorDAO apiMonitorDAO;

	@InjectMocks
	private ApiMonitorServiceImpl serviceImple;

	@Test
	public void testMtsSetter() throws JsonProcessingException, ItorixException, NullPointerException {

		String interactionId = "12345";
		HttpHeaders header = mock(HttpHeaders.class);
		Variables variable1 = new Variables();
		Variables variable = spy(variable1);
		List<Header> headerList = new ArrayList<Header>();
		String jsessionid = "12345";
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		Mockito.when(commonServices.getUserDetailsFromSessionID(Mockito.eq(jsessionid))).thenReturn(mock(User.class));
		variable.setVariables(headerList);
		ResponseEntity<?> responseEntity = serviceImple.createVariables(interactionId, header, variable, jsessionid,
				request, response);
		Long mts = variable.getMts();
		assertNotNull(mts);

	}

}
