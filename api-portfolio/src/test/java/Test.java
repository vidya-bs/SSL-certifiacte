import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itorix.apiwiz.portfolio.model.db.Portfolio;
import org.eclipse.jgit.api.errors.GitAPIException;

public class Test {

	public static void main(String[] args) {
		ObjectMapper objectMapper = new ObjectMapper();

		String str = "{\n" + "  \"id\": \"6153e27c5474604de39070ba\",\n" + "  \"name\": \"Order_Mamanagement\",\n"
				+ "  \"summary\": \"Portfolio for managemt of Orders\",\n"
				+ "  \"description\": \"this is a example text\",\n" + "  \"owner\": \"Tonik-Bank\",\n"
				+ "  \"ownerEmail\": \"support@itorix.com\",\n" + "  \"cts\": 1632887420851,\n"
				+ "  \"mts\": 1632888587239,\n" + "  \"createdBy\": \"Tonik Support\",\n"
				+ "  \"modifiedBy\": \"Tonik Support\",\n"
				+ "  \"portfolioImage\": \"https://api.apiwiz.io/itorix/v1/download/tonik-uat/portfolio/6153e27c5474604de39070ba/sudhakar-pro-portfolio-615222c9e9940f6264239e3e-istio.png?type=s3\",\n"
				+ "  \"products\": [\n" + "    {\n" + "      \"id\": \"6153e50a8d29a66fe1284d1d\",\n"
				+ "      \"name\": \"Mobile-channel\",\n"
				+ "      \"summary\": \"Product to access API's on mobile \",\n"
				+ "      \"description\": \"this is a sample data\",\n" + "      \"owner\": \"Tonik-Bank\",\n"
				+ "      \"ownerEmail\": \"support@itorix.com\",\n" + "      \"productStatus\": \"New\",\n"
				+ "      \"productAccess\": \"external\",\n" + "      \"publishStatus\": false\n" + "    }\n" + "  ],\n"
				+ "  \"projects\": [\n" + "    {\n" + "      \"id\": \"6153e70b618b6c1c411c2b33\",\n"
				+ "      \"name\": \"CRD1\",\n" + "      \"summary\": \"Sample project for testing\",\n"
				+ "      \"description\": \"this is a sample text\",\n" + "      \"owner\": \"Tonik-Bank\",\n"
				+ "      \"owner_email\": \"support@itorix.com\",\n" + "      \"isActive\": false,\n"
				+ "      \"status\": \"Active\",\n" + "      \"consumers\": [\n" + "        \"VISA\"\n" + "      ]\n"
				+ "    }\n" + "  ]\n" + "}";

		try {
			objectMapper.readValue(str, Portfolio.class);
		} catch (JsonProcessingException e) {

		}

		// log.info();
	}
}
