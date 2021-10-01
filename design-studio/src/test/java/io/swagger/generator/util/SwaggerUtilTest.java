package io.swagger.generator.util;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;
public class SwaggerUtilTest {

	@Test
	public void removeResponseSchemaTag() throws IOException {

		ObjectMapper objectMapper = new ObjectMapper();

		Map<?, ?> map = objectMapper.readValue(Paths.get("/Users/balajivijayan/IdeaProjects/apiwiz-core-platform-api/design-studio/src/test/resources/swagger.json").toFile(), Map.class);





		String output = SwaggerUtil.removeResponseSchemaTag(objectMapper.writeValueAsString(map));

		System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(output));

	}
}