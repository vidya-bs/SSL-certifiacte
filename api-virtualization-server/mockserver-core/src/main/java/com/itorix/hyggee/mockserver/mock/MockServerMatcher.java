package com.itorix.hyggee.mockserver.mock;

import org.springframework.stereotype.Component;

import com.itorix.hyggee.mockserver.collections.CircularLinkedList;
import com.itorix.hyggee.mockserver.logging.MockServerLogger;
import com.itorix.hyggee.mockserver.matchers.HttpRequestMatcher;
import com.itorix.hyggee.mockserver.matchers.HttpRequestMatcherResponse;
import com.itorix.hyggee.mockserver.matchers.MatcherBuilder;
import com.itorix.hyggee.mockserver.model.HttpRequest;
import com.itorix.hyggee.mockserver.scheduler.Scheduler;

import static com.itorix.hyggee.mockserver.configuration.ConfigurationProperties.maxExpectations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *   
 */
@Component
public class MockServerMatcher{// extends MockServerMatcherNotifier  {
	
	
	DataPersist dataPersist;
	
	
	protected List<HttpRequestMatcher> httpRequestMatchers = Collections.synchronizedList(new CircularLinkedList<HttpRequestMatcher>(maxExpectations()));
	
	private MatcherBuilder matcherBuilder;
	
	

	public MockServerMatcher(MockServerLogger logFormatter, Scheduler scheduler , DataPersist dataPersist) {
//		super(scheduler);
		this.matcherBuilder = new MatcherBuilder(logFormatter);
		this.dataPersist = dataPersist;
		if(dataPersist.getExpectations() != null)
		this.httpRequestMatchers = cloneMatchers(dataPersist.getExpectations());
		

	}
	
	public MockServerMatcher() {

	}

	public synchronized void add(Expectation expectation) {
		reloadData();
		//httpRequestMatchers.add(matcherBuilder.transformsToMatcher(expectation));
		//dataPersist.updateExpectation(expectation);
		//notifyListeners(this);
	}
	
	

	private synchronized List<HttpRequestMatcher> cloneMatchers() {
		return new ArrayList<>(httpRequestMatchers);
	}

	private synchronized List<HttpRequestMatcher> cloneMatchers(List<Expectation> expectations) {
		for(Expectation expectation : expectations)
			httpRequestMatchers.add(matcherBuilder.transformsToMatcher(expectation));
		return new ArrayList<>(httpRequestMatchers);
	}

	public synchronized void reset() {
		httpRequestMatchers.clear();
//		        notifyListeners(this);
	}
	
	public synchronized void reloadData() {
		this.httpRequestMatchers.clear();
		this.httpRequestMatchers = cloneMatchers(dataPersist.getExpectations());
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public MockServerMatcherResponse firstMatchingExpectation(HttpRequest httpRequest) {
		MockServerMatcherResponse mockServerMatcherResponse = new MockServerMatcherResponse();
		Expectation matchingExpectation = null;
		Map partialMatch = new HashMap();
		for (HttpRequestMatcher httpRequestMatcher : cloneMatchers()) {
			HttpRequestMatcherResponse httpRequestMatcherResponse = httpRequestMatcher.match(httpRequest, httpRequest);
			String id = httpRequestMatcher.getExpectation().getId();
			if(!httpRequestMatcherResponse.isMatched() && httpRequestMatcherResponse.isPathMatched())
				partialMatch.put(id, httpRequestMatcherResponse.getBecause()) ;
			if (httpRequestMatcherResponse.isMatched()) {
				matchingExpectation = httpRequestMatcher.decrementRemainingMatches();
			}
			if (!httpRequestMatcher.isActive()) {
				if (httpRequestMatchers.contains(httpRequestMatcher)) {
					httpRequestMatchers.remove(httpRequestMatcher);
					//notifyListeners(this);
				}
			}
			if (matchingExpectation != null) {
				break;
			}
		}
		mockServerMatcherResponse.setPartialMatches(partialMatch);
		mockServerMatcherResponse.setExpectation(matchingExpectation);
		return mockServerMatcherResponse;
	}

	public void clear(HttpRequest httpRequest) {
		if (httpRequest != null) {
			HttpRequestMatcher clearHttpRequestMatcher = matcherBuilder.transformsToMatcher(httpRequest);
			for (HttpRequestMatcher httpRequestMatcher : cloneMatchers()) {
				if (clearHttpRequestMatcher.matches(httpRequestMatcher.getExpectation().getHttpRequest())) {
					if (httpRequestMatchers.contains(httpRequestMatcher)) {
						httpRequestMatchers.remove(httpRequestMatcher);
//						notifyListeners(this);
					}
				}
			}
		} else {
			reset();
		}
	}

	public List<Expectation> retrieveExpectations(HttpRequest httpRequest) {
		List<Expectation> expectations = new ArrayList<Expectation>();
		HttpRequestMatcher requestMatcher = matcherBuilder.transformsToMatcher(httpRequest);
		for (HttpRequestMatcher httpRequestMatcher : cloneMatchers()) {
			if (httpRequest == null ||
					requestMatcher.matches(httpRequestMatcher.getExpectation().getHttpRequest())) {
				expectations.add(httpRequestMatcher.getExpectation());
			}
		}
		return expectations;
	}

	public boolean isEmpty() {
		return httpRequestMatchers.isEmpty();
	}
}
