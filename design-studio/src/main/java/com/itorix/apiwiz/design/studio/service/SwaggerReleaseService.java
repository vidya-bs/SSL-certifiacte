package com.itorix.apiwiz.design.studio.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.itorix.apiwiz.design.studio.swaggerdiff.model.DiffVO;

@CrossOrigin
@RestController
public interface SwaggerReleaseService {

//	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','ANALYST','PROJECT-ADMIN','SITE-ADMIN') and hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.POST, value = "/v1/swaggers/change-log")
	public ResponseEntity<Object> getDifference(@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "oas", required = false, defaultValue = "2.0") String oas, @RequestBody DiffVO diff)
			throws Exception;

//	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','ANALYST','PROJECT-ADMIN','SITE-ADMIN') and hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.POST, value = "/v1/swaggers/{swaggerid}/change-log")
	public ResponseEntity<Object> saveDifference(@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "oas", required = false, defaultValue = "2.0") String oas,
			@RequestParam(value = "text", required = false) String text,
			@RequestParam(value = "oldRevision", required = false) String oldRevision,
			@RequestParam(value = "newRevision", required = false) String newRevision,
			@RequestParam(value = "summary", required = false) String summary,
			@PathVariable(value = "swaggerid") String swaggerid) throws Exception;

//	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','ANALYST','PROJECT-ADMIN','SITE-ADMIN') and hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.POST, value = {"/v1/swaggers/{swaggerid}/change-log/{id}"})
	public ResponseEntity<Object> updateDifference(@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "oas", required = false, defaultValue = "2.0") String oas,
			@PathVariable(value = "swaggerid", required = false) String swaggerid,
			@PathVariable(value = "id", required = false) String id,
			@RequestParam(value = "text", required = false) String text,
			@RequestParam(value = "summary", required = false) String summary) throws Exception;

//	@PreAuthorize("hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = {"/v1/documentation/release-notes/{year}",
			"/v1/documentation/release-notes"})
	public ResponseEntity<Object> getReleasenotesbyyear(@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "oas", required = false, defaultValue = "2.0") String oas,
			@PathVariable(value = "year", required = false) String year) throws Exception;

//	@PreAuthorize("hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = {"/v1/documentation/release-notes/years"})
	public ResponseEntity<Object> getYearsr(@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "oas", required = false, defaultValue = "2.0") String oas) throws Exception;

//	@PreAuthorize("hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = {"/v1/swaggers/{swaggerid}/change-log/{id}",
			"/v1/swaggers/{swaggerid}/change-log"})
	public ResponseEntity<?> getSwaggerChangelog(@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "oas", required = false, defaultValue = "2.0") String oas,
			@RequestParam(value = "offset", required = false, defaultValue = "1") int offset,
			@RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
			@RequestParam(value = "timeRange", required = false) String timeRange,
			@RequestParam(value = "paginated", required = false, defaultValue = "true") boolean paginated,
			@PathVariable(value = "swaggerid", required = false) String swaggerid,
			@PathVariable(value = "id", required = false) String id) throws Exception;
}
