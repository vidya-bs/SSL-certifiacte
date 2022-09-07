package com.itorix.apiwiz.identitymanagement.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.itorix.apiwiz.identitymanagement.model.Video;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.List;
@Slf4j
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
		log.info(someClassList.get(0).getCategory());
	}
}