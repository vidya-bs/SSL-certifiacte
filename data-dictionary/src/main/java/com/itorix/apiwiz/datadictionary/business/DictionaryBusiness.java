package com.itorix.apiwiz.datadictionary.business;

import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.datadictionary.model.ModelStatus;
import com.itorix.apiwiz.datadictionary.model.PortfolioHistoryResponse;
import com.itorix.apiwiz.datadictionary.model.PortfolioModel;
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

	public DeleteResult deletePortfolioById(PortfolioVO portfolioVO);

	public PortfolioModel createPortfolioModel(PortfolioModel model);

	public List<PortfolioModel> findPortfolioModelsByportfolioID(PortfolioModel model);

	public PortfolioModel findPortfolioModelsByportfolioIDAndModelName(PortfolioModel model);

	public DeleteResult deletePortfolioModelByportfolioIDAndModelName(PortfolioModel model);

	public Object portfolioSearch(String interactionid, String name, int limit) throws ItorixException;

	public void updatePortfolioModelStatus(String id, String model_name, ModelStatus modelStatus);
	
	public PortfolioVO createPortfolioRevision(String id, Integer revision);
	
	public PortfolioVO getPortfolioByRevision(String id, Integer revision);
	
	public List<Revision> getRevisions(String id);
	
	public Integer getMaxRevision(String id);
	
}
