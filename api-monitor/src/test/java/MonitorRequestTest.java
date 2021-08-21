import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itorix.apiwiz.apimonitor.model.collection.MonitorCollections;
import com.itorix.apiwiz.apimonitor.model.request.MonitorRequest;
import org.junit.Test;

import java.util.Arrays;

public class MonitorRequestTest {

	@Test
	public void checkDeserialization() throws JsonProcessingException {

		MonitorRequest request = new MonitorRequest();
		request.setId("12345");

		MonitorCollections collections = new MonitorCollections();
		collections.setMonitorRequest(Arrays.asList(request));

		ObjectMapper objectMapper = new ObjectMapper();
		System.out.println(objectMapper.writeValueAsString(collections));
	}
}
