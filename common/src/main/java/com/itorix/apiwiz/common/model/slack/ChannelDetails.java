package com.itorix.apiwiz.common.model.slack;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Component;

@Component("ChannelDetails")
@Document(collection = "Slack.ChaneelDetails")
public class ChannelDetails {

	private String channelName;

	private String token;

	public String getChannelName() {
		return channelName;
	}

	public void setChannelName(String channelName) {
		this.channelName = channelName;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
}
