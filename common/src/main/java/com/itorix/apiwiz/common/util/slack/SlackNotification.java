package com.itorix.apiwiz.common.util.slack;

public interface SlackNotification {

	public boolean sendMessage(String text, String channelName);
}
