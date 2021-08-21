package com.itorix.apiwiz.common.util.slack;

import java.io.IOException;
import java.util.Collections;

import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

public class SlackHttpRetryHandler extends DefaultHttpRequestRetryHandler {

	private static final int RETRY_INTERVAL = 1000;

	private static final int RETRY_COUNT = 3;

	private static final Logger logger = Logger.getLogger(SlackHttpRetryHandler.class);

	public SlackHttpRetryHandler() {
		super(RETRY_COUNT, false, Collections.emptyList());
	}

	@Override
	public boolean retryRequest(final IOException exception, final int executionCount, final HttpContext context) {

		boolean canRetry = super.retryRequest(exception, executionCount, context);

		if (canRetry) {

			logger.warn("Caught Exception during slack request, retry attempt " + executionCount + "will retry after"
					+ (int) RETRY_INTERVAL / 1000 + "seconds");
			try {
				Thread.sleep(RETRY_INTERVAL);
			} catch (InterruptedException e) {

			}
		}

		if (!canRetry) {
			logger.error("Could not able to send after" + RETRY_COUNT);
		}

		return canRetry;
	}
}
