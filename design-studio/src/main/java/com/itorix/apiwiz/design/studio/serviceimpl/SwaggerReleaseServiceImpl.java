package com.itorix.apiwiz.design.studio.serviceimpl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.design.studio.model.SwaggerSubscriptionDao;
import com.itorix.apiwiz.design.studio.service.SwaggerReleaseService;
import com.itorix.apiwiz.design.studio.swaggerdiff.dao.SwaggerDiffService;
import com.itorix.apiwiz.design.studio.swaggerdiff.model.DiffVO;

@CrossOrigin
@RestController
public class SwaggerReleaseServiceImpl implements SwaggerReleaseService {

	@Autowired
	private SwaggerDiffService swaggerDiffService;
	
	@Autowired
	private SwaggerSubscriptionDao swaggerSubscriptionDao;

	@Override
	public ResponseEntity<Object> getDifference(@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "oas", required = false, defaultValue = "2.0") String oas, @RequestBody DiffVO diff)
			throws Exception {
		List<String> missingFields = new ArrayList<>();
		if (diff.getSwaggerName() == null || diff.getSwaggerName().trim() == "")
			missingFields.add("swaggerName");
		if (diff.getOldRevision() <= 0)
			missingFields.add("oldRevision");
		if (diff.getNewRevision() <= 0)
			missingFields.add("newRevision");
		if (missingFields.size() > 0)
			raiseException(missingFields);
		HttpHeaders headers = new HttpHeaders();
		headers.add("Access-Control-Expose-Headers", "X-Swagger-oldVersion, X-Swagger-newVersion, X-Swagger-id");
		headers.add("X-Swagger-oldVersion", diff.getOldRevision() + "");
		headers.add("X-Swagger-newVersion", diff.getNewRevision() + "");
		headers.add("X-Swagger-id", diff.getSwaggerName());
		return new ResponseEntity<Object>(
				swaggerDiffService.getDiff(diff.getSwaggerName(), diff.getOldRevision(), diff.getNewRevision(), oas),
				headers, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> saveDifference(@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "oas", required = false, defaultValue = "2.0") String oas,
			@RequestParam(value = "text", required = false) String text,
			@RequestParam(value = "oldRevision", required = false) String oldRevision,
			@RequestParam(value = "newRevision", required = false) String newRevision,
			@RequestParam(value = "summary", required = false) String summary,
			@PathVariable(value = "swaggerid") String swaggerid) throws Exception {
		List<String> missingFields = new ArrayList<>();
		if (text == null || text.trim() == "")
			missingFields.add("text");
		if (swaggerid == null || swaggerid.trim() == "")
			missingFields.add("swaggerid");
		if (oldRevision == null || oldRevision.trim() == "")
			missingFields.add("oldRevision");
		if (newRevision == null || newRevision.trim() == "")
			missingFields.add("newRevision");
		if (summary == null || summary.trim() == "")
			missingFields.add("summary");
		if (missingFields.size() > 0)
			raiseException(missingFields);
		swaggerDiffService.saveReleaseNotes(text, oas, swaggerid, oldRevision, newRevision, summary);
		swaggerSubscriptionDao.swaggerNotification(swaggerid, oas, summary, text);
		return new ResponseEntity<Object>(HttpStatus.CREATED);
	}

	private void raiseException(List<String> fileds) throws ItorixException {
		try {
			String message = new ObjectMapper().writeValueAsString(fileds);
			message = message.replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("\"", "").replaceAll(",", ", ");
			message = "Invalid request data! Missing mandatory data: " + message;
			throw new ItorixException(message, "General-1001");
		} catch (JsonProcessingException e) {
			throw new ItorixException(ErrorCodes.errorMessage.get("General-1001"), "General-1001");
		}
	}

	@Override
	public ResponseEntity<Object> updateDifference(@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "oas", required = false, defaultValue = "2.0") String oas,
			@PathVariable(value = "swaggerid", required = false) String swaggerid,
			@PathVariable(value = "id", required = false) String id,
			@RequestParam(value = "text", required = false) String text,
			@RequestParam(value = "summary", required = false) String summary) throws Exception {
		swaggerDiffService.updateSwaggerReleaseNotes(id, text, oas, summary);
		return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<Object> getReleasenotesbyyear(@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "oas", required = false, defaultValue = "2.0") String oas,
			@PathVariable(value = "year", required = false) String year) throws Exception {
		return new ResponseEntity<Object>(swaggerDiffService.getReleaseNotes(year, oas).getNotes(), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getYearsr(@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "oas", required = false, defaultValue = "2.0") String oas) throws Exception {
		return new ResponseEntity<Object>(swaggerDiffService.getYears(), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getSwaggerChangelog(@RequestHeader(value = "JSESSIONID") String jsessionId,
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "oas", required = false, defaultValue = "2.0") String oas,
			@RequestParam(value = "offset", required = false, defaultValue = "1") int offset,
			@RequestParam(value = "timeRange", required = false) String timeRange,
			@PathVariable(value = "swaggerid", required = false) String swaggerid,
			@PathVariable(value = "id", required = false) String id) throws Exception {
		if (id != null)
			return new ResponseEntity<Object>(swaggerDiffService.getSwaggerChangeLog(id), HttpStatus.OK);
		return new ResponseEntity<Object>(
				swaggerDiffService.getSwaggerIdReleaseNotes(timeRange, oas, swaggerid, offset), HttpStatus.OK);
	}
}
