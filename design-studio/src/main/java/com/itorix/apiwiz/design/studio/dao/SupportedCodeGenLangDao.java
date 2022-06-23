package com.itorix.apiwiz.design.studio.dao;

import com.itorix.apiwiz.design.studio.model.SupportedCodeGenLang;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SupportedCodeGenLangDao {

	@Autowired
	private MongoTemplate mongoTemplate;

	public List<String> getSupportedLanguages(String type, String oas) {
		List<SupportedCodeGenLang> supportedLanguages = new ArrayList<>();

		if (oas.isEmpty() || oas.toLowerCase().contains("2")) {
			supportedLanguages.addAll(
					mongoTemplate.find(new Query(Criteria.where("oas2Compatible").is(true).and("type").is(type)),
							SupportedCodeGenLang.class));
		} else {
			supportedLanguages.addAll(
					mongoTemplate.find(new Query(Criteria.where("oas3Compatible").is(true).and("type").is(type)),
							SupportedCodeGenLang.class));
		}

		return supportedLanguages.stream().map(SupportedCodeGenLang::getName).collect(Collectors.toList());
	}

	public SupportedCodeGenLang addLang(SupportedCodeGenLang newLangData) {

		if (!langExists(newLangData.getName())) {
			mongoTemplate.save(newLangData);
		}

		return newLangData;
	}

	public boolean langExists(String name) {
		Query query = new Query();
		query.addCriteria(Criteria.where("name").is(name));
		return mongoTemplate.exists(query, SupportedCodeGenLang.class);
	}

	public SupportedCodeGenLang updateLang(String existingLangName, SupportedCodeGenLang newLangData) {
		removeLang(existingLangName);
		addLang(newLangData);
		return newLangData;
	}

	public void removeLang(String name) {
		mongoTemplate.findAndRemove(new Query(Criteria.where("name").is(name)), SupportedCodeGenLang.class);
	}
}
