package com.itorix.apiwiz.ibm.apic.connector.service;

import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.ibm.apic.connector.model.ConnectorCardRequest;
import com.itorix.apiwiz.ibm.apic.connector.model.ConnectorCardResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
public interface ConnectorCardService {

	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.GET, value = "/v1/ibm-apic/connector")
	public ResponseEntity<Object> getAllConnectors(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID", required = false) String jsessionid
	) throws ItorixException, Exception;

	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.POST, value = "/v1/ibm-apic/connector")
	public ResponseEntity<Object> createConnector(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID", required = false) String jsessionid,
			@RequestBody ConnectorCardRequest connectorCardRequest
	)throws ItorixException, Exception;

	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.PUT, value = "/v1/ibm-apic/connector")
	public ResponseEntity<Object> updateConnector(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID", required = false) String jsessionid,
			@RequestBody ConnectorCardResponse updatedConnectorConfig
	)throws ItorixException, Exception;

	@PreAuthorize("hasAnyAuthority('GROWTH','ENTERPRISE')")
	@RequestMapping(method = RequestMethod.DELETE, value = "/v1/ibm-apic/connector/{id}")
	public ResponseEntity<Object> deleteConnectorById(
			@RequestHeader(value = "interactionid", required = false) String interactionid,
			@RequestHeader(value = "JSESSIONID", required = false) String jsessionid,
			@PathVariable(value = "id") String id
	)throws ItorixException, Exception;

}
