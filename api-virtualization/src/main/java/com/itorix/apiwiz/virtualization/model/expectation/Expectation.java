package com.itorix.apiwiz.virtualization.model.expectation;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Document(collection = "Mock.Expectation.List")
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Expectation extends AbstractObject {
     String name;
     String description;
     String summary;
     String groupId;
     String scenarioName;
     Request request;
     Response response;
     List<String> pathArray;
}

