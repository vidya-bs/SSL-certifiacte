package com.itorix.apiwiz.design.studio.service;

import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.design.studio.model.GraphQL;
import com.itorix.apiwiz.design.studio.model.GraphQLData;
import com.itorix.apiwiz.design.studio.model.swagger.sync.StatusHistory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin
@RestController
public interface GraphQLService {

  @PreAuthorize("hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
  @RequestMapping(method = RequestMethod.POST, value = "/v1/design/graphql/{name}")
  public ResponseEntity<?> create(@RequestHeader(value = "interactionid", required = false) String interactionid,
      @RequestHeader(value = "JSESSIONID") String jsessionid,
      @PathVariable(value="name") String name,
      @RequestBody GraphQLData graphqlSchema)throws ItorixException;

  @PreAuthorize("hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
  @RequestMapping(method = RequestMethod.PUT, value = "/v1/design/graphql/{graphqlId}/revision/{revision}")
  public ResponseEntity<?> updateWithRevision(@RequestHeader(value = "interactionid", required = false) String interactionid,
      @RequestHeader(value = "JSESSIONID") String jsessionid,
      @PathVariable(value = "graphqlId")String graphqlId,
      @PathVariable(value = "revision")Integer revision,
      @RequestBody GraphQLData graphqlSchema)throws ItorixException;

  @PreAuthorize("hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
  @RequestMapping(method = RequestMethod.GET, value = "/v1/design/graphql/{graphqlId}/revision/{revision}")
  public ResponseEntity<?> getWithRevision(@RequestHeader(value = "interactionid", required = false) String interactionid,
      @RequestHeader(value = "JSESSIONID") String jsessionid,
      @PathVariable(value = "graphqlId")String graphqlId,
      @PathVariable(value = "revision")Integer revision)throws ItorixException;

  @PreAuthorize("hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
  @RequestMapping(method = RequestMethod.DELETE, value = "/v1/design/graphql/{graphqlId}/revision/{revision}")
  public ResponseEntity<?> deleteWithRevision(@RequestHeader(value = "interactionid", required = false) String interactionid,
      @RequestHeader(value = "JSESSIONID") String jsessionid,
      @PathVariable(value = "graphqlId")String graphqlId,
      @PathVariable(value = "revision")Integer revision)throws ItorixException;

  @PreAuthorize("hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
  @RequestMapping(method = RequestMethod.POST, value = "/v1/design/graphql/{graphqlId}/revision")
  public ResponseEntity<?> createNewRevision(@RequestHeader(value = "interactionid", required = false) String interactionid,
      @RequestHeader(value = "JSESSIONID") String jsessionid,
      @PathVariable(value = "graphqlId")String graphqlId,
      @RequestParam(value = "revision",required = false)Integer revision,
      @RequestBody GraphQLData graphqlSchema)throws ItorixException;

  @PreAuthorize("hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
  @RequestMapping(method = RequestMethod.PUT, value = "/v1/design/graphql/{graphqlId}/revision/{revision}/status")
  public ResponseEntity<?> changeStatusWithRevision(@RequestHeader(value = "interactionid", required = false) String interactionid,
      @RequestHeader(value = "JSESSIONID") String jsessionid,
      @PathVariable(value = "graphqlId")String graphqlId,
      @PathVariable(value = "revision")Integer revision,
      @RequestBody StatusHistory statusHistory)throws ItorixException;

  @PreAuthorize("hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
  @RequestMapping(method = RequestMethod.GET, value = "/v1/design/graphql/{graphqlId}")
  public ResponseEntity<?> getAllRevisionsWithId(@RequestHeader(value = "interactionid", required = false) String interactionid,
      @RequestHeader(value = "JSESSIONID") String jsessionid,
      @PathVariable(value = "graphqlId") String graphqlId)throws ItorixException;

  @PreAuthorize("hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
  @RequestMapping(method = RequestMethod.DELETE, value = "/v1/design/graphql/{graphqlId}")
  public ResponseEntity<?> deleteAllRevisionsWithId(@RequestHeader(value = "interactionid", required = false) String interactionid,
      @RequestHeader(value = "JSESSIONID") String jsessionid,
      @PathVariable(value = "graphqlId")String graphqlId)throws ItorixException;

  @PreAuthorize("hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
  @RequestMapping(method = RequestMethod.GET, value = "/v1/design/graphql/history")
  public ResponseEntity<?> getHistory(@RequestHeader(value = "interactionid", required = false) String interactionid,
      @RequestHeader(value = "JSESSIONID") String jsessionid,
      @RequestParam(value = "offset", required = false, defaultValue = "1") int offset,
      @RequestParam(value = "pagesize", required = false, defaultValue = "10") int pagesize,
      @RequestParam(value = "status", required = false) String status,
      @RequestParam(value = "sortbymts", required = false) String sortByMts,
      @RequestParam(value = "name", required = false)String name,
      @RequestParam(value= "limit", required = false,defaultValue = "10")int limit)throws ItorixException;

  @PreAuthorize("hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
  @RequestMapping(method = RequestMethod.GET, value = "/v1/design/graphql/stats")
  public ResponseEntity<?> getStats(@RequestHeader(value = "interactionid", required = false) String interactionid,
      @RequestHeader(value = "JSESSIONID") String jsessionid)throws ItorixException;

  @PreAuthorize("hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
  @RequestMapping(method = RequestMethod.POST, value = "/v1/design/graphql/import")
  public ResponseEntity<?> importFile(@RequestHeader(value = "interactionid", required = false) String interactionid,
      @RequestHeader(value = "JSESSIONID") String jsessionid,
      @RequestParam(value = "file", required = false) MultipartFile file,
      @RequestParam(value = "type", required = true) String type,
      @RequestParam(value = "gitURI", required = false) String gitURI,
      @RequestParam(value = "branch", required = false) String branch,
      @RequestParam(value = "authType", required = false) String authType,
      @RequestParam(value = "userName", required = false) String userName,
      @RequestParam(value = "password", required = false) String password,
      @RequestParam(value = "personalToken", required = false) String personalToken)throws Exception;


  @PreAuthorize("hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
  @RequestMapping(method = RequestMethod.GET, value = "/v1/design/graphql/{graphQLId}/revision/{revision}/lockstatus")
  public ResponseEntity<?> getLockStatus(
      @RequestHeader(value = "interactionid", required = false) String interactionid,
      @RequestHeader(value = "JSESSIONID") String jsessionid,
      @PathVariable("graphQLId") String graphQLId,
      @PathVariable("revision") Integer revision) throws ItorixException;

  @PreAuthorize("hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
  @RequestMapping(method = RequestMethod.PUT, value = "/v1/design/graphql/{graphQLId}/revision/{revision}/lockstatus")
  public ResponseEntity<?> updateLockStatus(
      @RequestHeader(value = "interactionid", required = false) String interactionid,
      @RequestHeader(value = "JSESSIONID") String jsessionid,
      @PathVariable("graphQLId") String swaggername,
      @PathVariable("revision") Integer revision,
      @RequestBody GraphQL graphQL) throws ItorixException;

}
