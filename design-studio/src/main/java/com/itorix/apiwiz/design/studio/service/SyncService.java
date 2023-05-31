package com.itorix.apiwiz.design.studio.service;

import com.itorix.apiwiz.design.studio.model.dto.ScmUploadDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
public interface SyncService {

	@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN','ANALYST','SITE-ADMIN') and hasAnyAuthority('STARTER','GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/sync2Repo/{module}/{id}/revisions/{revision}")
	public ResponseEntity<?> saveSync2RepoData(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID") String jsessionid,
			@PathVariable("module") String module,
			@PathVariable("id") String id,
			@PathVariable("revision") int revision,
			@RequestBody ScmUploadDTO scmUploadDTO) throws Exception;

}