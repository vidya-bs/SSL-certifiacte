import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itorix.apiwiz.apimonitor.model.collection.MonitorCollections;
import com.itorix.apiwiz.apimonitor.model.request.MonitorRequest;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.Arrays;
@Slf4j
public class MonitorRequestTest {

	@Test
	public void checkDeserialization() throws JsonProcessingException {

		MonitorRequest request = new MonitorRequest();
		request.setId("12345");

		MonitorCollections collections = new MonitorCollections();
		collections.setMonitorRequest(Arrays.asList(request));

		ObjectMapper objectMapper = new ObjectMapper();
		log.info(objectMapper.writeValueAsString(collections));
	}
}
