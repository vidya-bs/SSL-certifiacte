package com.itorix.apiwiz.datadictionary.business;

import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.datadictionary.model.DDSchema;
import com.itorix.apiwiz.datadictionary.model.ModelStatus;
import com.itorix.apiwiz.datadictionary.model.PortfolioHistoryResponse;
import com.itorix.apiwiz.datadictionary.model.PortfolioModel;
import com.itorix.apiwiz.datadictionary.model.PortfolioReport;
import com.itorix.apiwiz.datadictionary.model.PortfolioVO;
import com.itorix.apiwiz.datadictionary.model.Revision;
import com.mongodb.client.result.DeleteResult;
import org.springframework.stereotype.Service;

import java.util.List;

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

}
