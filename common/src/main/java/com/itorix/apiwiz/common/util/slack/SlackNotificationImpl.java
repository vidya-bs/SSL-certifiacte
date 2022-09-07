package com.itorix.apiwiz.common.util.slack;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class SlackNotificationImpl {

	@Autowired
	MongoTemplate mongoTemplate;

	private static Logger logger = Logger.getLogger(SlackNotificationImpl.class);

	public static String SLACK_CHATPOST_URL = "https://slack.com/api/chat.postMessage";
	private HttpPost createPostRequest = null;
	CloseableHttpClient httpClient;

	SlackNotificationImpl() {

		httpClient = createHttpClient();
	}

	public static CloseableHttpClient createHttpClient() {
		return HttpClients.custom().setRetryHandler(new SlackHttpRetryHandler())
				.setDefaultRequestConfig(getDefaultRequestConfig()).build();
	}

	public static RequestConfig getDefaultRequestConfig() {

		return RequestConfig.custom().setSocketTimeout(1000).setConnectTimeout(1000).setConnectionRequestTimeout(1000)
				.build();
	}

	public HttpPost createPostRequest(String channel, String textMessage) throws URISyntaxException {

		URIBuilder uriBuilder = new URIBuilder(SLACK_CHATPOST_URL);
		uriBuilder.setParameter("token",
				"xoxp-288910944438-288084131044-558914235234-89af90cdc2f0ef57e96668da65af81a0");
		uriBuilder.setParameter("channel", channel);
		uriBuilder.setParameter("text", textMessage);
		URI build = uriBuilder.build();
		return new HttpPost(build);
	}

	public void sendMessage(String text, List<String> channelList) throws IOException {

		Boolean messageSent = true;
		// CloseableHttpClient httpClient = createHttpClient();
		CloseableHttpResponse respone = null;
		for (String channelName : channelList) {
			try {

				createPostRequest = createPostRequest(channelName, text);
				respone = httpClient.execute(createPostRequest);
				int statusCode = respone.getStatusLine().getStatusCode();

				if (statusCode == HttpStatus.SC_OK) {
					logger.debug("Message Sent ::" + text + "for channel::" + channelName);
				}
				if (statusCode != HttpStatus.SC_OK) {

					HttpEntity entity = respone.getEntity();
					String message = EntityUtils.toString(entity);
					ObjectMapper mapper = new ObjectMapper();
					JsonNode readTree = mapper.readTree(message);
					String errorMessage = (String) (readTree.get("error")).asText();
					messageSent = false;
				}

			} catch (IOException | URISyntaxException e) {
				// TODO Auto-generated catch block
				logger.error("Exception occurred", e);
			} finally {

				if (respone != null) {
					respone.close();
				}
			}
			// return messageSent;
		}
	}

	/*
	 * public static void main(String[] args) throws URISyntaxException,
	 * ClientProtocolException, IOException {
	 * 
	 * List<String> channelsList = new ArrayList(); channelsList.add("plan");
	 * channelsList.add("goals");
	 * 
	 * SlackNotificationImpl slack =new SlackNotificationImpl();
	 * 
	 * slack.sendMessage("Hello testingg via API", channelsList);
	 * 
	 * for (String channel : channelsList) {
	 * 
	 * URIBuilder uriBuilder = new
	 * URIBuilder("https://slack.com/api/chat.postMessage");
	 * uriBuilder.setParameter("token",
	 * "xoxp-288910944438-288084131044-558914235234-89af90cdc2f0ef57e96668da65af81a0"
	 * ); uriBuilder.setParameter("channel", channel);
	 * uriBuilder.setParameter("text", "Hello Welcome"); URI build =
	 * uriBuilder.build();
	 * 
	 * HttpClient builder = HttpClientBuilder.create().build();
	 * 
	 * HttpPost post = new HttpPost(build);
	 * 
	 * HttpResponse execute = builder.execute(post); InputStream content =
	 * execute.getEntity().getContent(); BufferedReader bufferedReader = new
	 * BufferedReader(new InputStreamReader(content));
	 * 
	 * log.info(bufferedReader.readLine());
	 * 
	 * log.info(execute.getEntity().toString()); ; } }
	 */

}
