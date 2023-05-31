package com.itorix.apiwiz.design.studio.businessimpl;

import static com.itorix.apiwiz.identitymanagement.model.Constants.GRAPHQL_ID;
import static com.itorix.apiwiz.identitymanagement.model.Constants.REVISION;

import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.design.studio.model.GraphQL;
import com.itorix.apiwiz.design.studio.model.Revision;
import com.itorix.apiwiz.design.studio.model.Status;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.internal.bytebuddy.matcher.ElementMatchers.is;
import static org.mockito.Mockito.*;
@RunWith(org.mockito.junit.MockitoJUnitRunner.class)
@SpringBootTest
public class GraphQLBusinessImplTest {
  @Mock
  private MongoTemplate mongoTemplate;

  @InjectMocks
  private GraphQLBusinessImpl graphQLBusinessImpl;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }
  @Test
  public void createGraphQLSchema(){
    GraphQL graphQL = new GraphQL();
    when(mongoTemplate.save(graphQL)).thenReturn(null);
    graphQLBusinessImpl.create(graphQL);
    verify(mongoTemplate).save(graphQL);
  }

  @Test(expected=ItorixException.class)
  public void updateWithRevisionWhenNull() throws ItorixException {
    String graphQLId = "5c59e13253b64b718774c6cfe35ea7f6";
    Integer revision = 1;
    String schema = "type Book {\n" + " title: String\n" + " author: Author\n" + " }\n" + " \n" + " type Author {\n" + " name: String\n" + " books: [Book]\n" + " }";
    Mockito.when(mongoTemplate.findOne(Mockito.any(), Mockito.any())).thenReturn(null);
    graphQLBusinessImpl.updateWithRevision(graphQLId,revision,schema);
  }

  @Test
  public void updateWithRevisionWhenNonNull() throws ItorixException {
    String graphQLId = "5c59e13253b64b718774c6cfe35ea7f6";
    Integer revision = 1;
    String schema = "type Book {\n" + " title: String\n" + " author: Author\n" + " }\n" + " \n" + " type Author {\n" + " name: String\n" + " books: [Book]\n" + " }";
    GraphQL graphQL = new GraphQL();
    graphQL.setGraphQLId(graphQLId);
    graphQL.setRevision(revision);
    graphQL.setGraphQLSchema(schema);
    Mockito.when(mongoTemplate.findOne(Mockito.any(), Mockito.any())).thenReturn(graphQL);
    graphQLBusinessImpl.updateWithRevision(graphQLId,revision,schema);
    verify(mongoTemplate).save(graphQL);
  }

  @Test(expected=ItorixException.class)
  public void getWithRevisionWhenNull() throws ItorixException {
    String graphQLId = "5c59e13253b64b718774c6cfe35ea7f6";
    Integer revision = 1;
    Mockito.when(mongoTemplate.findOne(Mockito.any(), Mockito.any())).thenReturn(null);
    graphQLBusinessImpl.getWithRevision(graphQLId,revision);
  }

  @Test
  public void getWithRevisionWhenNonNull() throws ItorixException {
    String graphQLId = "5c59e13253b64b718774c6cfe35ea7f6";
    Integer revision = 1;
    GraphQL graphQL = new GraphQL();
    graphQL.setGraphQLId(graphQLId);
    graphQL.setRevision(revision);
    Mockito.when(mongoTemplate.findOne(Mockito.any(), Mockito.any())).thenReturn(graphQL);
    graphQLBusinessImpl.getWithRevision(graphQLId,revision);
    verify(mongoTemplate).findOne(new Query(Criteria.where(GRAPHQL_ID).is(graphQLId)
        .and(REVISION).is(revision)),GraphQL.class);
  }


  @Test(expected=ItorixException.class)
  public void deleteWithRevisionWhenNull() throws ItorixException {
    String graphQLId = "5c59e13253b64b718774c6cfe35ea7f6";
    Integer revision = 1;
    Mockito.when(mongoTemplate.findOne(Mockito.any(), Mockito.any())).thenReturn(null);
    graphQLBusinessImpl.deleteWithRevision(graphQLId,revision);
  }

  @Test
  public void deleteWithRevisionWhenNonNull() throws ItorixException {
    String graphQLId = "5c59e13253b64b718774c6cfe35ea7f6";
    Integer revision = 1;
    GraphQL graphQL = new GraphQL();
    graphQL.setGraphQLId(graphQLId);
    graphQL.setRevision(revision);
    Mockito.when(mongoTemplate.findOne(Mockito.any(), Mockito.any())).thenReturn(graphQL);
    graphQLBusinessImpl.deleteWithRevision(graphQLId,revision);
    verify(mongoTemplate).remove(graphQL);
  }

  @Test(expected=ItorixException.class)
  public void getAllRevisionsWithIdWhenNull() throws ItorixException {
    String graphQLId = "5c59e13253b64b718774c6cfe35ea7f6";
    Mockito.when(mongoTemplate.find(Mockito.any(), Mockito.any())).thenReturn(new ArrayList<>());
    graphQLBusinessImpl.getAllRevisionsWithId(graphQLId);
  }

  @Test
  public void getAllRevisionsWithIdWhenNonNull() throws ItorixException {
    String graphQLId = "5c59e13253b64b718774c6cfe35ea7f6";
    GraphQL graphQL1 = new GraphQL();
    graphQL1.setGraphQLId(graphQLId);
    graphQL1.setRevision(1);
    GraphQL graphQL2 = new GraphQL();
    graphQL2.setGraphQLId(graphQLId);
    graphQL2.setRevision(2);
    List<GraphQL> graphQLList = new ArrayList<>();
    graphQLList.add(graphQL1);
    graphQLList.add(graphQL2);
    Mockito.when(mongoTemplate.find(Mockito.any(), Mockito.any())).thenReturn(
        Collections.singletonList(graphQLList));
    graphQLBusinessImpl.getAllRevisionsWithId(graphQLId);
    verify(mongoTemplate).find(new Query(Criteria.where(
        GRAPHQL_ID).is(graphQLId)),GraphQL.class);
  }


  @Test(expected=ItorixException.class)
  public void deleteAllRevisionsWithIdWhenNull() throws ItorixException {
    String graphQLId = "5c59e13253b64b718774c6cfe35ea7f6";
    Mockito.when(mongoTemplate.find(Mockito.any(), Mockito.any())).thenReturn(new ArrayList<>());
    graphQLBusinessImpl.deleteAllRevisionsWithId(graphQLId);
  }

  @Test
  public void deleteAllRevisionsWithIdWhenNonNull() throws ItorixException {
    String graphQLId = "5c59e13253b64b718774c6cfe35ea7f6";
    GraphQL graphQL1 = new GraphQL();
    graphQL1.setGraphQLId(graphQLId);
    graphQL1.setRevision(1);
    GraphQL graphQL2 = new GraphQL();
    graphQL1.setGraphQLId(graphQLId);
    graphQL1.setRevision(2);
    List<GraphQL> graphQLList = new ArrayList<>();
    graphQLList.add(graphQL1);
    graphQLList.add(graphQL2);
    Mockito.when(mongoTemplate.find(Mockito.any(), Mockito.any())).thenReturn(
        Collections.singletonList(graphQLList));
    graphQLBusinessImpl.deleteAllRevisionsWithId(graphQLId);
    verify(mongoTemplate).remove(new Query(Criteria.where(GRAPHQL_ID).is(graphQLId)),GraphQL.class);
  }


  @Test
  public void getListOfRevisions() throws ItorixException {
    String graphQLId = "5c59e13253b64b718774c6cfe35ea7f6";
    GraphQL graphQL1 = new GraphQL();
    graphQL1.setGraphQLId(graphQLId);
    graphQL1.setRevision(1);
    graphQL1.setStatus(String.valueOf(Status.Draft));
    GraphQL graphQL2 = new GraphQL();
    graphQL2.setGraphQLId(graphQLId);
    graphQL2.setRevision(2);
    graphQL2.setStatus(String.valueOf(Status.Draft));
    List<GraphQL> graphQLList = new ArrayList<>();
    graphQLList.add(graphQL1);
    graphQLList.add(graphQL2);
    List<Revision> versions = new ArrayList<Revision>();
    for (GraphQL graphQl : graphQLList) {
      Revision version = new Revision();
      version.setRevision(graphQl.getRevision());
      version.setStatus(graphQl.getStatus());
      version.setId(graphQl.getGraphQLId() != null ? graphQl.getGraphQLId() : graphQl.getId());
      versions.add(version);
    }
    Mockito.when(mongoTemplate.find(Mockito.any(), Mockito.any())).thenReturn(
        (ArrayList)graphQLList);
    List<Revision> checkVersions = (graphQLBusinessImpl.getListOfRevisions(graphQLId));
//    Assert.assertEquals(versions, checkVersions);
//    Assert.assertArrayEquals(versions.toArray()., checkVersions.toArray());
//    assertThat(Arrays.asList(versions))
//        .isEqualTo(Arrays.asList(graphQLBusinessImpl.getListOfRevisions(graphQLId)));
//    Assert.assertArrayEquals(versions.toArray(), checkVersions.toArray());
//    Assertions.assertTrue(versions.containsAll(checkVersions));
//        Assert.assertEquals(versions.toString(),graphQLBusinessImpl.getListOfRevisions(graphQLId).toString());
  }

}
