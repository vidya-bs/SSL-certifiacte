package com.itorix.apiwiz.identitymanagement.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.itorix.apiwiz.identitymanagement.model.Video;
import org.junit.Test;

import java.util.List;

public class WorkspaceDaoTest {

	WorkspaceDao workspaceDao = new WorkspaceDao();

	@Test
	public void getVideos() throws JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		TypeFactory typeFactory = objectMapper.getTypeFactory();
		String test = " [\n" + "    {\n" + "     \"name\": \"test\",\n" + "     \"summary\":\"test\",\n"
				+ "     \"category\":\"test\",\n" + "     \"url\": \"test\"\n" + " },\n" + " {\n"
				+ "     \"name\": \"Ge==\",\n" + "     \"summary\":\"test\",\n" + "     \"category\":\"Get Started\",\n"
				+ "     \"url\": \"test\"\n" + " }\n" + " ]";
		List<Video> someClassList = objectMapper.readValue(test,
				typeFactory.constructCollectionType(List.class, Video.class));
		System.out.println(someClassList.get(0).getCategory());
	}
}