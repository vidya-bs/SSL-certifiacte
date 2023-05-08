package com.itorix.apiwiz.design.studio.service;

import com.itorix.apiwiz.design.studio.model.AsyncApi;
import java.util.Optional;
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
public interface AsyncApiService {

	@PreAuthorize("hasAnyRole('ADMIN','OPERATION') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.POST, value = "/v1/design/async/{name}")
	public ResponseEntity<?> createAsyncApi(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable(value = "name") String name,
			@RequestBody String asyncapi) throws Exception;


	@RequestMapping(method = RequestMethod.PUT, value = "/v1/design/async/{id}")
	public ResponseEntity<?> updateAsyncApi(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable("id") String id,
			@RequestBody String asyncapi) throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN','OPERATION') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/design/async/history")
	public ResponseEntity<?> getAllAsyncApis(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(value = "name")Optional<String> name,
			@RequestParam(value = "sortBy")Optional<String> sortBy,
			@RequestParam(value = "status")Optional<String> status,
			@RequestParam(value = "offset",required = false, defaultValue = "1") int offset,
			@RequestParam(value = "pagesize", required = false, defaultValue = "10") int pageSize) throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN','OPERATION') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/design/async/names")
	public ResponseEntity<?> getAllNamesOfAsyncApi(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN','OPERATION') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/design/async/stats")
	public ResponseEntity<?> getAllStatsOfAsyncApi(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid) throws Exception;


	@PreAuthorize("hasAnyRole('ADMIN','OPERATION') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/design/async/{asyncapiId}/revisions")
	public ResponseEntity<?> getAllRevisionsOfAsyncApi(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable (value = "asyncapiId")String asyncapiId) throws Exception;

	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/design/async/{id}")
	public ResponseEntity<?> deleteAsyncApi(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable("id") String id) throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN','OPERATION') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.POST, value = "/v1/design/async/{asyncId}/revision")
	public ResponseEntity<?> createAsyncApiRevision(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable("asyncId") String asyncId,
			@RequestBody String asyncapi) throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN','OPERATION') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/design/async/{asyncId}/revision/{revision}")
	public ResponseEntity<?> updateAsyncApiRevision(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable("asyncId") String asyncId,
			@PathVariable("revision") int revision,
			@RequestBody AsyncApi asyncapi) throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN','OPERATION') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/design/async/{asyncapiId}/revisions/{revison}")
	public ResponseEntity<?> getRevisionOfAsyncApi(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable (value = "asyncapiId")String asyncapiId,
			@PathVariable (value = "revison")int revison) throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN','OPERATION') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/design/async/{asyncapiId}/revisions/{revison}")
	public ResponseEntity<?> deleteRevisionOfAsyncApi(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable (value = "asyncapiId")String asyncapiId,
			@PathVariable (value = "revison")int revison) throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN','OPERATION') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/design/async/{asyncId}/revision/{revision}/status")
	public ResponseEntity<?> updateStatusAsyncApiRevision(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable("asyncId") String asyncId,
			@PathVariable("revision") int revision,
			@RequestBody AsyncApi asyncapi) throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN','OPERATION') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/design/async/{asyncapiId}/revisions/{revison}/lockstatus")
	public ResponseEntity<?> lockRevision(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable (value = "asyncapiId")String asyncapiId,
			@PathVariable (value = "revison")int revison,
			@RequestBody AsyncApi asyncApi) throws Exception;

	@PreAuthorize("hasAnyRole('ADMIN','OPERATION') and hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/design/async/search")
	public ResponseEntity<?> searchAsyncApi(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam (value = "name")String name,
			@RequestParam (value = "limit",defaultValue = "10")int limit) throws Exception;


	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','ANALYST','SITE-ADMIN') and hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.POST, value = "/v1/design/async/import")
	public ResponseEntity<Object> importAsyncApis(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@RequestParam(value = "file", required = false) MultipartFile file,
			@RequestParam(value = "type", required = true) String type,
			@RequestParam(value = "gitURI", required = false) String gitURI,
			@RequestParam(value = "branch", required = false) String branch,
			@RequestParam(value = "authType", required = false) String authType,
			@RequestParam(value = "userName", required = false) String userName,
			@RequestParam(value = "password", required = false) String password,
			@RequestParam(value = "personalToken", required = false) String personalToken) throws Exception;

}
