package io.swagger.generator.util;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;

import static org.junit.Assert.assertTrue;
public class SwaggerUtilTest {

	public void removeResponseSchemaTag() throws IOException {

		ObjectMapper objectMapper = new ObjectMapper();

		Map<?, ?> map = objectMapper.readValue(Paths.get("/Users/balajivijayan/IdeaProjects/apiwiz-core-platform-api/design-studio/src/test/resources/swagger.json").toFile(), Map.class);


		String output = SwaggerUtil.removeResponseSchemaTag(objectMapper.writeValueAsString(map));

		assertTrue(output.contains("responseSchema"));

	}

	@Test
	public void checkReplaceURLWithPort() throws ItorixException {

		String output = SwaggerUtil.replaceURL("http://example.com:8080/test?80", "/newPath");
		System.out.println(output);
		assertTrue(output.equals("http://example.com:8080/newPath?80"));
	}

	@Test
	public void checkReplaceURLWhenNoProtocolIsPresent() throws ItorixException {
		try {
			String output = SwaggerUtil.replaceURL("/api/v3", "/newPath");
		} catch (ItorixException m ) {
			assertTrue(m.getMessage().contains("Malformed URL Provided"));
		}
	}
}