package com.itorix.apiwiz.datadictionary.businessimpl;

import com.itorix.apiwiz.datadictionary.model.PortfolioModel;
import com.itorix.apiwiz.identitymanagement.dao.BaseRepository;
import org.junit.Before;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class DataDictionaryBusinessImplTest {

  @InjectMocks
  DictionaryBusinessImpl dictionaryBusiness = new DictionaryBusinessImpl();

  @Mock
  BaseRepository baseRepository;

  @Mock
  MongoTemplate mongoTemplate;

  @Before
  public void setupMock() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void findPortfolioModelsWithRevisions() {
    String id = "62e918aa48e6716749a0d918";
    String name = "ATM Services3";
    Integer revision = 1;
    PortfolioModel models = spy(new PortfolioModel());
    models.setPortfolioID(id);
    models.setModelName(name);
    models.setModel("{\n" + "    \"name\": \"ATM Services3\",\n" + "    \"definitions\": {\n"
        + "        \"ATM Services\": {\n" + "            \"description\": \"\",\n"
        + "            \"type\": \"striaang\"\n" + "        }\n" + "    }\n" + "}");
    when(baseRepository.findOne("modelName", name, "revision", revision, "portfolioId", id, PortfolioModel.class))
        .thenReturn(models);
    PortfolioModel portfolioModel = dictionaryBusiness.findPortfolioModelsWithRevisions(id, name, revision);
    assertEquals(name, models.getModelName());

  }
  @Test
  public void getDDRevisionsTest() {
    String name = "ATM Services3";
    String id = "";
    PortfolioModel portfolioModel = spy(new PortfolioModel());
    portfolioModel.setRevision(1);
    Query query = new Query(Criteria.where("modelName").is(name)).with(Sort.by(Sort.Direction.DESC, "revision"))
        .limit(1);
    when(mongoTemplate.findOne(query, PortfolioModel.class)).thenReturn(portfolioModel);
    Integer portfolioModel1 = dictionaryBusiness.getDDRevisions(name, id);
    assertEquals(Long.valueOf(1), Long.valueOf(portfolioModel1));

  }

  @Test
  public void findPortfolioModelsWithAllRevisions() {
    String id = "62e918aa48e6716749a0d918";
    String name = "ATM Services3";
    Integer revision = 1;
    List<PortfolioModel> models = spy(new ArrayList<>());
    PortfolioModel portfolioModel = spy(new PortfolioModel());
    portfolioModel.setPortfolioID(id);
    portfolioModel.setModelName(name);
    portfolioModel.setModel("{\n" + "    \"name\": \"ATM Services3\",\n" + "    \"definitions\": {\n"
        + "        \"ATM Services\": {\n" + "            \"description\": \"\",\n"
        + "            \"type\": \"striaang\"\n" + "        }\n" + "    }\n" + "}");
    PortfolioModel portfolioModel1 = spy(new PortfolioModel());
    String id1 = "62e918aa48e6716749a0d918";
    String name1 = "ATM Services";
    Integer revision1 = 2;
    portfolioModel.setPortfolioID(id1);
    portfolioModel.setModelName(name1);
    portfolioModel.setModel("{\n" + "    \"name\": \"ATM Services3\",\n" + "    \"definitions\": {\n"
        + "        \"ATM Services\": {\n" + "            \"description\": \"\",\n"
        + "            \"type\": \"striaang\"\n" + "        }\n" + "    }\n" + "}");
    models.add(portfolioModel);
    models.add(portfolioModel1);
    when(baseRepository.find("modelName", name, "portfolioId", id, PortfolioModel.class)).thenReturn(models);
    List<PortfolioModel> models1 = spy(dictionaryBusiness.findPortfolioModelsWithAllRevisions(id, name));
    assertEquals(name1, models.get(0).getModelName());
  }
}