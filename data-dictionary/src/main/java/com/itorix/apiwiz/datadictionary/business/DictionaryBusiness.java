package com.itorix.apiwiz.datadictionary.business;

import java.util.List;

import org.springframework.stereotype.Service;

import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.datadictionary.model.PortfolioHistoryResponse;
import com.itorix.apiwiz.datadictionary.model.PortfolioModel;
import com.itorix.apiwiz.datadictionary.model.PortfolioVO;
import com.mongodb.client.result.DeleteResult;

@Service
public interface DictionaryBusiness {

	
	public PortfolioVO createPortfolio(PortfolioVO portfolioVO) ;

	public PortfolioVO findPortfolio(PortfolioVO portfolioVO);

	public List<PortfolioVO> findAllPortfolioSummary(String interactionid);
	
	public PortfolioVO getPortfolioById(PortfolioVO portfolioVO);
	
//	public PortfolioVO updatePortfolioModel(PortfolioVO portfolio, String action) throws ItorixException;

	public PortfolioHistoryResponse findAllPortfolios(String interactionid, int offset, int pageSize);

	public PortfolioVO findPortfolioById(PortfolioVO portfolioVO) ;

	public DeleteResult deletePortfolioById(PortfolioVO portfolioVO);

	public PortfolioModel createPortfolioModel(PortfolioModel model);

	public List<PortfolioModel> findPortfolioModelsByportfolioID(PortfolioModel model) ;

	public PortfolioModel findPortfolioModelsByportfolioIDAndModelName(PortfolioModel model) ;

	public DeleteResult deletePortfolioModelByportfolioIDAndModelName(PortfolioModel model);
	
	public Object portfolioSearch(String interactionid, String name, int limit) throws ItorixException;
}
