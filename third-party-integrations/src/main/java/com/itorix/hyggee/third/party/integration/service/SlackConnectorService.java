package com.itorix.hyggee.third.party.integration.service;

import com.itorix.apiwiz.common.model.slack.SlackWorkspace;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/v1/slack")
public interface SlackConnectorService {


    @PostMapping
    public ResponseEntity<?> installSlack(
            @RequestHeader(value = "interactionid",required = false) String interactionid,
            @RequestHeader(value = "JSESSIONID") String jsessionid,
            @RequestBody SlackWorkspace slackWorkspace
    ) throws Exception;


    @GetMapping(value = "")
    public ResponseEntity<Object> getAllWorkspaces(
            @RequestHeader(value = "interactionid",required = false) String interactionid,
            @RequestHeader(value = "JSESSIONID") String jsessionid
    ) throws Exception;


    @GetMapping(value = "/{id}")
    public ResponseEntity<Object> getWorkspaceById(
            @RequestHeader(value = "interactionid",required = false) String interactionid,
            @RequestHeader(value = "JSESSIONID") String jsessionid,
            @PathVariable(value = "id") String workspaceId
    ) throws Exception;


    @PutMapping(value = "/{id}")
    public ResponseEntity<?> updateSlackWorkspace(
            @RequestHeader(value = "interactionid",required = false) String interactionid,
            @RequestHeader(value = "JSESSIONID") String jsessionid,
            @PathVariable(value = "id") String workspaceId,
            @RequestBody SlackWorkspace slackWorkspace
    ) throws Exception;


    @DeleteMapping(value = "/{id}")
    public ResponseEntity<?> deleteSlackWorkspace(
            @RequestHeader(value = "interactionid",required = false) String interactionid,
            @RequestHeader(value = "JSESSIONID") String jsessionid,
            @PathVariable(value = "id") String workspaceId
    ) throws Exception;
}
