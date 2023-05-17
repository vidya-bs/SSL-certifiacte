package com.itorix.apiwiz.design.studio.serviceimpl;

import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.design.studio.business.GraphQLBusiness;
import com.itorix.apiwiz.design.studio.model.GraphQL;
import com.itorix.apiwiz.design.studio.model.GraphQLData;
import com.itorix.apiwiz.design.studio.model.GraphQLImport;
import com.itorix.apiwiz.design.studio.model.swagger.sync.StatusHistory;
import com.itorix.apiwiz.design.studio.service.GraphQLService;
import java.util.List;
import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin
@RestController
public class GraphQLServiceImpl implements GraphQLService {

  private static final Logger logger = LoggerFactory.getLogger(GraphQLServiceImpl.class);

  @Autowired
  GraphQLBusiness graphQLBusiness;

  @Override
  public ResponseEntity<?> create(String interactionid, String jsessionid, String name, GraphQLData graphqlSchema)
      throws ItorixException {
    GraphQL checkGraphQL = new GraphQL();
    checkGraphQL.setName(name);
    String graphQLData = graphqlSchema.getData()!=null ? graphqlSchema.getData() : "";
    GraphQL graphQL = graphQLBusiness.findGraphQL(checkGraphQL);
    if(graphQL!=null){
      logger.info("Creating a new revision for {}",graphQL.getName());
      checkGraphQL.setGraphQLSchema(graphQLData);
      graphQLBusiness.createNewRevisionWithName(checkGraphQL);
    }else{
      logger.info("Creating a new GraphQL Schema");
      checkGraphQL.setGraphQLSchema(graphQLData);
      graphQLBusiness.create(checkGraphQL);
    }
    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @Override
  public ResponseEntity<?> updateWithRevision(String interactionid, String jsessionid, String graphQLId,
      Integer revision, GraphQLData graphqlSchema) throws ItorixException {
    logger.info("Updating the GraphQL schema for Id-{} and revision-{}",graphQLId,revision);
    String graphQLData = graphqlSchema.getData()!=null ? graphqlSchema.getData() : "";
    graphQLBusiness.updateWithRevision(graphQLId,revision,graphQLData);
    return new ResponseEntity<>(HttpStatus.ACCEPTED);
  }

  @Override
  public ResponseEntity<?> getWithRevision(String interactionid, String jsessionid, String graphQLId,
      Integer revision) throws ItorixException {
    logger.info("Fetching the GraphQL schema for Id-{} and revision-{}",graphQLId,revision);
    return new ResponseEntity<>(graphQLBusiness.getWithRevision(graphQLId,revision), HttpStatus.OK);
  }

  @Override
  public ResponseEntity<?> deleteWithRevision(String interactionid, String jsessionid, String graphQLId,
      Integer revision) throws ItorixException {
    logger.info("Deleting the GraphQL schema for Id-{} and revision-{}",graphQLId,revision);
    graphQLBusiness.deleteWithRevision(graphQLId,revision);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @Override
  public ResponseEntity<?> createNewRevision(String interactionid, String jsessionid, String graphQLId,
      GraphQLData graphqlSchema) throws ItorixException {
    GraphQL checkGraphQL = new GraphQL();
    checkGraphQL.setGraphQLId(graphQLId);
    String graphQLData = graphqlSchema.getData()!=null ? graphqlSchema.getData() : "";
    checkGraphQL.setGraphQLSchema(graphQLData);
    logger.info("Creating a new revision for Id-{}",graphQLId);
    graphQLBusiness.createNewRevisionWithId(checkGraphQL);
    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @Override
  public ResponseEntity<?> changeStatusWithRevision(String interactionid, String jsessionid, String graphQLId,
      Integer revision, StatusHistory statusHistory) throws ItorixException {
    logger.info("Updating the status of the GraphQL Schema for Id-{} and revision-{}",graphQLId,revision);
    graphQLBusiness.changeStatusWithRevision(graphQLId,revision,statusHistory);
    return new ResponseEntity<>(HttpStatus.ACCEPTED);
  }

  @Override
  public ResponseEntity<?> getAllRevisionsWithId(String interactionid, String jsessionid, String graphQLId)
      throws ItorixException {
    logger.info("Fetching all revision of the GraphQL Schema for Id-{}",graphQLId);
    return ResponseEntity.ok(graphQLBusiness.getAllRevisionsWithId(graphQLId));
  }

  @Override
  public ResponseEntity<?> deleteAllRevisionsWithId(String interactionid, String jsessionid, String graphQLId)
      throws ItorixException {
    logger.info("Deleting all revision of the GraphQL Schema for Id-{}",graphQLId);
    graphQLBusiness.deleteAllRevisionsWithId(graphQLId);
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<?> getHistory(String interactionid, String jsessionid, int offset, int pagesize,
      String status,String sortByMts,String name,int limit) throws ItorixException {
    logger.info("Fetching the latest revisions history");
    return ResponseEntity.ok(graphQLBusiness.getHistory(jsessionid,offset, pagesize, status, sortByMts,name,limit));
  }

  @Override
  public ResponseEntity<?> getStats(String interactionid, String jsessionid)
      throws ItorixException {
    logger.info("Fetching the stats of latest revisions");
    return ResponseEntity.ok(graphQLBusiness.getStats());
  }

  @Override
  public ResponseEntity<?> importFile(String interactionid, String jsessionid, MultipartFile file, String type,
      String gitURI, String branch, String authType, String userName, String password,
      String personalToken) throws Exception {
    List<GraphQLImport> graphQLImports = graphQLBusiness.importFile(file, type, gitURI, branch, authType, userName, password, personalToken);
    return ResponseEntity.ok(graphQLImports);
  }

  @Override
  public ResponseEntity<?> getLockStatus(String interactionid, String jsessionid,
      String graphQLId, Integer revision) throws ItorixException {
    GraphQL graphQL = new GraphQL();
    graphQL.setGraphQLId(graphQLId);
    graphQL.setRevision(revision);
    return new ResponseEntity<>(graphQLBusiness.getLockStatus(graphQL),HttpStatus.OK);
  }

  @Override
  public ResponseEntity<?> updateLockStatus(String interactionid, String jsessionid,
      String graphQLId, Integer revision, GraphQL graphQL) throws ItorixException {
    graphQL.setGraphQLId(graphQLId);
    graphQL.setRevision(revision);
    logger.info("Updating the lockstatus of the GraphQL Schema for Id-{} and revision-{}",graphQLId,revision);
    graphQLBusiness.updateLockStatus(graphQL,jsessionid);
    return new ResponseEntity<>(HttpStatus.ACCEPTED);
  }

}
