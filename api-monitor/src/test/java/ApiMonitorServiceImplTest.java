import static org.junit.Assert.*;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.itorix.apiwiz.apimonitor.model.Variables;
import com.itorix.apiwiz.apimonitor.serviceimpl.ApiMonitorServiceImpl;
import com.itorix.apiwiz.common.model.AbstractObject;
import com.itorix.apiwiz.common.model.exception.ItorixException;

public class ApiMonitorServiceImplTest {

	@Test
	public void testMtsSetter() throws JsonProcessingException, ItorixException {

		Variables variable=new Variables();
		variable.setMts(System.currentTimeMillis());
		long a=System.currentTimeMillis();
		long b=variable.getMts();
		assertEquals(a,b);

		
		
		
	}

}
