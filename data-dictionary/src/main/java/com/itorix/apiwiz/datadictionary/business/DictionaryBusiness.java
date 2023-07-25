package com.itorix.apiwiz.datadictionary.business;

import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.datadictionary.model.*;
import com.mongodb.client.result.DeleteResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public interface DictionaryBusiness {

	public PortfolioVO createPortfolio(PortfolioVO portfolioVO);

	public PortfolioVO createPortfolioRevision(PortfolioVO portfolioVO, String id);

	public PortfolioVO findPortfolio(PortfolioVO portfolioVO);

	public List<PortfolioVO> findAllPortfolioSummary(String interactionid);

	public PortfolioVO getPortfolioById(PortfolioVO portfolioVO);

	// public PortfolioVO updatePortfolioModel(PortfolioVO portfolio, String
	// action) throws
	// ItorixException;

	public PortfolioHistoryResponse findAllPortfolios(String interactionid, int offset, int pageSize);
	public PortfolioHistoryResponse findAllPortfoliosV2(String interactionid, int offset, int pageSize);

	public PortfolioVO findPortfolioById(PortfolioVO portfolioVO);

	public DeleteResult deletePortfolioByIdAndRevision(PortfolioVO portfolioVO);

	public void deletePortfolioById(PortfolioVO portfolioVO);

	public PortfolioModel createPortfolioModel(PortfolioModel model);


	public PortfolioModel createnewPortfolioModel(PortfolioModel model);

	public PortfolioModel updateModelRevision(PortfolioModel model);

	public List<PortfolioModel> findPortfolioModelsByportfolioID(PortfolioModel model);

	public PortfolioModel findPortfolioModelsByportfolioIDAndModelId(PortfolioModel model);

	public DeleteResult deletePortfolioModelByModelId(String modelId);

	public Object portfolioSearch(String interactionid, String name, int limit) throws ItorixException;

	public void updatePortfolioModelStatus(String id, String model_name, ModelStatus modelStatus);

	public PortfolioVO createPortfolioRevision(String id, Integer revision);

	public PortfolioVO getPortfolioByRevision(String id, Integer revision);

	public List<Revision> getRevisions(String id);

	public Integer getMaxRevision(String id);

	PortfolioModel findPortfolioModelByportfolioIDAndModelId(String id, String modelId);

	public Integer getDDRevisions(String name, String id);

	public List<PortfolioModel> findPortfolioModelsWithAllRevisions(String id, String modelId);

	public PortfolioModel findPortfolioModelsWithRevisions(String id, String modelId, Integer revision);

	public void updatePortfolioModelStatusWithRevision(String id, String modelId, ModelStatus modelStatus,
													   Integer revision);

	public DeleteResult deletePortfolioModelByportfolioIDAndModelIdAndRevision(PortfolioModel model);

	PortfolioModel findPortfolioModelByportfolioIDAndModelIdAndRevison(String id, String modelId, Integer revision);

	PortfolioModel findPortfolioModelsByportfolioIDAndModelName(PortfolioModel model);

	public void sendNotificationForModel(String jsessionid, PortfolioModel portfolioModel,
										 String message);
	public void sendNotificationToSwagger(String jsessionid, PortfolioVO portfolioVO,
										  String message);

	String getGlobalRule();

	List<DDSchema> getModels(String id);

	PortfolioReport getModelswithRulesets(String id);

	public void sync2Repo(String portfolioId, DictionaryScmUpload dictionaryScmUpload) throws Exception;

	public void deSyncFromRepo(String portfolioId);

	public DictionaryScmUpload getGitIntegrations(String jsessionid,String portfolioId) throws Exception;

	public Map<String, Map<String,String>> getDataModelMap(String portfolioId) throws Exception;
	public List<?> getAllDatabaseConnections(String databaseName) throws ItorixException;

	public List<String> getDatabases(String connectionId) throws ItorixException;

	public List<String> getPostgresSchemas(String connectionId) throws ItorixException;

	public List<String> getCollectionNames(String connectionId, String databaseType, String databaseName) throws ItorixException;

	public List<String> getTableNames(String connectionId,String databaseType, String schemaName) throws ItorixException;

	public Object getSchemas(String databaseType, String connectionId,String databaseName,String schemaName, List<String> collections,List<String> tables,boolean deepSearch) throws ItorixException;

	public List<String> searchForKey(String databaseType, String connectionId, String databaseName, String schemaName, String searchKey) throws ItorixException;
}
