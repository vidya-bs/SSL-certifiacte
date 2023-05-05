package com.itorix.apiwiz.design.studio.business;

import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.design.studio.model.GraphQL;
import com.itorix.apiwiz.design.studio.model.GraphQLImport;
import com.itorix.apiwiz.design.studio.model.Stat;
import com.itorix.apiwiz.design.studio.model.SwaggerLockResponse;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public interface GraphQLBusiness {

  public void create(GraphQL graphQL);

  public void updateWithRevision(String graphqlId,Integer revision,String graphqlSchema)
      throws ItorixException;

  public GraphQL getWithRevision(String graphqlId,Integer revision) throws ItorixException;

  public void deleteWithRevision(String graphqlId,Integer revision) throws ItorixException;

  public void createNewRevisionWithName(GraphQL graphQL) throws ItorixException;

  public void createNewRevisionWithId(GraphQL graphQL) throws ItorixException;

  public void changeStatusWithRevision(String graphqlId,Integer revision,String status)
      throws ItorixException;

  public List<GraphQL> getAllRevisionsWithId(String graphqlId) throws ItorixException;

  public void deleteAllRevisionsWithId(String graphqlId) throws ItorixException;

  public Object getHistory(String jsessionId,int offset, int pagesize,
      String status, String sortByMts,String name);

  public List<Stat> getStats();

  public List<GraphQLImport> importFile( MultipartFile file, String type,
      String gitURI, String branch, String authType, String userName, String password,
      String personalToken) throws Exception;

  public SwaggerLockResponse getLockStatus(GraphQL graphQL) throws ItorixException;

  public void updateLockStatus(GraphQL graphQL,String jsessionId) throws ItorixException;

  public GraphQL findGraphQL(GraphQL graphQL);

}
