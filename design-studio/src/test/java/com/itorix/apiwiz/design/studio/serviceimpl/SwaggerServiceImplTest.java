package com.itorix.apiwiz.design.studio.serviceimpl;

import static org.junit.Assert.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.itorix.apiwiz.design.studio.model.Subscriber;
import com.itorix.apiwiz.design.studio.model.SwaggerSubscription;
import com.itorix.apiwiz.identitymanagement.dao.BaseRepository;

import java.util.HashSet;
import java.util.Set;

public class SwaggerServiceImplTest {

	@InjectMocks
	SwaggerServiceImpl swaggerService = new SwaggerServiceImpl();

	@Mock
	BaseRepository baseRepository;

	@Mock
	MongoTemplate mongoTemplate;

	@Before
	public void setupMock() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void getSwaggerSubscriptionDetails() {
		String oas = "2.0";
		String swaggerId = "0af476b3d44447bcbcfb9f0f0459963d";
		String jsessionId = "1234";
		
		SwaggerSubscription swaggerSubscription = spy(new SwaggerSubscription());
		String swaggerName = "Test";
		swaggerSubscription.setSwaggerName(swaggerName);
		swaggerSubscription.setSwaggerId(swaggerId);
		swaggerSubscription.setOas(oas);
		Subscriber subscriber = new Subscriber();
		subscriber.setName("Bob");
		subscriber.setEmailId("bob@123.com");
		swaggerSubscription.setSubscribers(subscriber);
		Set<Subscriber> subscribers = new HashSet<Subscriber>();
		subscribers.add(subscriber);
		when(baseRepository.findOne("swaggerId", swaggerId, SwaggerSubscription.class)).thenReturn(swaggerSubscription);
		assertEquals(subscribers, swaggerSubscription.getSubscribers());
	}
	 @Test
	 public void removeSubscriber() {
		String oas = "2.0";
		String swaggerId = "0af476b3d44447bcbcfb9f0f0459963d";
		String jsessionId = "1234";
		
		SwaggerSubscription swaggerSubscription = spy(new SwaggerSubscription());
		String swaggerName = "Test";
		swaggerSubscription.setSwaggerName(swaggerName);
		swaggerSubscription.setSwaggerId(swaggerId);
		swaggerSubscription.setOas(oas);
		Subscriber subscriber = new Subscriber();
		subscriber.setName("Bob");
		subscriber.setEmailId("bob@123.com");
		swaggerSubscription.setSubscribers(subscriber);
		Set<Subscriber> subscribers = new HashSet<Subscriber>();
		subscribers.add(subscriber);
		swaggerSubscription.removeSubscribers("bob@123.com");
		when(baseRepository.findOne("swaggerId", swaggerId, SwaggerSubscription.class)).thenReturn(swaggerSubscription);
		assertFalse(swaggerSubscription.getSubscribers().contains(subscriber));

	 }
	 
	 @Test
	 public void addSubscribers() {
		String oas = "2.0";
		String swaggerId = "0af476b3d44447bcbcfb9f0f0459963d";
		String jsessionId = "1234";
		
		SwaggerSubscription swaggerSubscription = spy(new SwaggerSubscription());
		String swaggerName = "Test";
		swaggerSubscription.setSwaggerName(swaggerName);
		swaggerSubscription.setSwaggerId(swaggerId);
		swaggerSubscription.setOas(oas);
		Subscriber subscriber = new Subscriber();
		subscriber.setName("Bob");
		subscriber.setEmailId("bob@123.com");
		swaggerSubscription.setSubscribers(subscriber);
		Set<Subscriber> subscribers = new HashSet<Subscriber>();
		subscribers.add(subscriber);
		when(baseRepository.findOne("swaggerId", swaggerId, SwaggerSubscription.class)).thenReturn(swaggerSubscription);
		assertTrue(swaggerSubscription.getSubscribers().contains(subscriber));
	 }

}
