package com.itorix.apiwiz.ibm.apic.connector.serviceImpl;
import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.ibm.apic.connector.dao.IBMAPICConnectorRuntimeDAO;
import com.itorix.apiwiz.ibm.apic.connector.model.ConnectorCardRequest;
import com.itorix.apiwiz.ibm.apic.connector.model.ConnectorCardResponse;
import com.itorix.apiwiz.ibm.apic.connector.service.ConnectorCardService;
import com.itorix.apiwiz.ibm.apic.connector.util.IBMAPICSpecUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import static com.itorix.apiwiz.ibm.apic.connector.util.HttpUtil.response;

@Service
@CrossOrigin
@RestController
public class ConnectorCardServiceImpl implements ConnectorCardService {

	private static final Logger logger = LoggerFactory.getLogger(ConnectorCardServiceImpl.class);

	@Autowired
	private IBMAPICConnectorRuntimeDAO ibmapicConnectorRuntimeDAO;

	@Autowired
	private IBMAPICSpecUtil ibmapicSpecUtil;

	@Override
	public ResponseEntity<Object> getAllConnectors(String interactionid,
			String jsessionid) throws ItorixException, Exception {
		try{
			return new ResponseEntity<>(ibmapicConnectorRuntimeDAO.getAllConnectors(), HttpStatus.OK);
		}catch(Exception ex){
			logger.error(String.format("%s :: %s", ErrorCodes.errorMessage.get("IBM-APIC-Connector-1"),ex.getMessage()));
			return response(ErrorCodes.errorMessage.get("IBM-APIC-Connector-1"),HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public ResponseEntity<Object> createConnector(String interactionid, String jsessionid,
			ConnectorCardRequest connectorCardRequest) throws ItorixException, Exception {
		try{
			ConnectorCardResponse connectorCardResponse = ibmapicConnectorRuntimeDAO.insertConnector(connectorCardRequest,jsessionid);
			if(connectorCardResponse != null){
				ibmapicConnectorRuntimeDAO.createOneTimeImportSchedule(connectorCardResponse.getOrgName());
				return new ResponseEntity<>(connectorCardResponse,HttpStatus.CREATED);
			}else{
				logger.error(String.format("%s :: %s", ErrorCodes.errorMessage.get("IBM-APIC-Connector-2"),"connectorCardResponse is null"));
				return response(ErrorCodes.errorMessage.get("IBM-APIC-Connector-2"),HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}catch(Exception ex){
			logger.error(String.format("%s :: %s", ErrorCodes.errorMessage.get("IBM-APIC-Connector-3"),ex.getMessage()));
			return response(ErrorCodes.errorMessage.get("IBM-APIC-Connector-3"),HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public ResponseEntity<Object> updateConnector(String interactionid, String jsessionid,
			ConnectorCardResponse updatedConnectorConfig) throws ItorixException, Exception {
		try{
			ConnectorCardResponse connectorCardResponse = ibmapicConnectorRuntimeDAO.updateConnector(updatedConnectorConfig,jsessionid);
			if(connectorCardResponse != null){
				return new ResponseEntity<>(connectorCardResponse,HttpStatus.OK);
			}else{
				logger.error(String.format("%s :: %s", ErrorCodes.errorMessage.get("IBM-APIC-Connector-4"),"connectorCardResponse is null"));
				return response(ErrorCodes.errorMessage.get("IBM-APIC-Connector-4"),HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}catch(Exception ex){
			logger.error(String.format("%s :: %s", ErrorCodes.errorMessage.get("IBM-APIC-Connector-5"),ex.getMessage()));
			return response(ErrorCodes.errorMessage.get("IBM-APIC-Connector-5"),HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public ResponseEntity<Object> deleteConnectorById(String interactionid, String jsessionid, String id)
			throws ItorixException, Exception {
		try{
			ibmapicConnectorRuntimeDAO.deleteConnector(id);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}catch(Exception ex){
			logger.error(String.format("%s :: %s", ErrorCodes.errorMessage.get("IBM-APIC-Connector-6"),ex.getMessage()));
			return response(ErrorCodes.errorMessage.get("IBM-APIC-Connector-6"),HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
