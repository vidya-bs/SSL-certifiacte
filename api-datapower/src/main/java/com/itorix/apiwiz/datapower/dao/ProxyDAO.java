package com.itorix.apiwiz.datapower.dao;

import com.itorix.apiwiz.datapower.model.ProxySearchRequest;
import com.itorix.apiwiz.datapower.model.ProxySearchResponse;
import com.itorix.apiwiz.datapower.model.proxy.GenerateProxyRequestDTO;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.model.projectmanagement.ProjectProxyResponse;
import com.itorix.apiwiz.common.model.proxystudio.ProxyPortfolio;
import com.itorix.apiwiz.common.model.proxystudio.Scm;
import com.itorix.apiwiz.datapower.model.PromoteProxyRequest;
import com.itorix.apiwiz.datapower.model.db.PortfolioResponse;
import com.itorix.apiwiz.datapower.model.proxy.Proxy;
import com.itorix.apiwiz.identitymanagement.model.Pagination;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ProxyDAO {

	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Autowired
	private ProxyUtils proxyUttils;


	public String createProxy(Proxy proxy) throws ItorixException {
		Query query = new Query().addCriteria(Criteria.where("name").is(proxy.getName()));

		if (mongoTemplate.count(query, Proxy.class) != 0) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Portfolio-1014"), "Portfolio-1014");
		}

		proxy = mongoTemplate.save(proxy);
		log.info("saving Proxy to db");
		return proxy.getId();
	}


	public String updateProxy(Proxy proxy) throws ItorixException {

		Query query = new Query().addCriteria(Criteria.where("id").is(proxy.getId()));
		Proxy dbProxy = mongoTemplate.findOne(query,Proxy.class);

		if (dbProxy == null) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Portfolio-1014"), "Portfolio-1014");
		}

		if(!dbProxy.getName().equals(proxy.getName())) {
			query = new Query().addCriteria(Criteria.where("name").is(proxy.getName()));
			if (mongoTemplate.count(query, Proxy.class) != 0) {
				throw new ItorixException(ErrorCodes.errorMessage.get("Portfolio-1014"), "Portfolio-1014");
			}
		}
		proxy.setCreatedBy(dbProxy.getCreatedBy());
		proxy.setCreatedUserName(dbProxy.getCreatedUserName());
		proxy.setCts(dbProxy.getCts());
		proxy = mongoTemplate.save(proxy);
		log.info("saving Proxy to db");
		return proxy.getId();
	}

	public Proxy getProxy(String proxyId) throws ItorixException {
		Query query = new Query().addCriteria(Criteria.where("_id").is(proxyId.trim()));
		Proxy proxy = mongoTemplate.findOne(query,Proxy.class);
		if (proxy == null) {
			query = new Query().addCriteria(Criteria.where("name").is(proxyId));
			proxy = mongoTemplate.findOne(query,Proxy.class);
		}
		if (proxy == null) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Portfolio-1014"), "Portfolio-1014");
		}
		return proxy;
	}

	public PortfolioResponse getListOfProxies(int offset, int pageSize) {

		Query query = new Query().with(Sort.by(Direction.DESC, "mts"))
				.skip(offset > 0 ? ((offset - 1) * pageSize) : 0).limit(pageSize);

		PortfolioResponse response = new PortfolioResponse();

		List<Proxy> proxies = mongoTemplate.find(query, Proxy.class);

		if (!CollectionUtils.isEmpty(proxies)) {
			Long counter = mongoTemplate.count(new Query(), Proxy.class);
			Pagination pagination = new Pagination();
			pagination.setOffset(offset);
			pagination.setTotal(counter);
			pagination.setPageSize(pageSize);
			response.setPagination(pagination);
			response.setData(proxies);
		}

		return response;
	}

	public void deleteProxy(String proxyId) throws ItorixException {
		Query query = new Query().addCriteria(Criteria.where("id").is(proxyId));
		Proxy proxy = mongoTemplate.findOne(query,Proxy.class);
		if (proxy == null) {
			query = new Query().addCriteria(Criteria.where("name").is(proxyId));
			proxy = mongoTemplate.findOne(query,Proxy.class);
		}
		if (proxy == null) {
			throw new ItorixException(ErrorCodes.errorMessage.get("Portfolio-1014"), "Portfolio-1014");
		}
		query = new Query().addCriteria(Criteria.where("id").is(proxy.getId()));
		mongoTemplate.remove(query, Proxy.class);
	}

	public ProjectProxyResponse generateProxy(String proxyId, String jsessionId, String connectorId)
			throws ItorixException {
		return proxyUttils.generateProxy(getProxy(proxyId), jsessionId,connectorId);
	}
	
	
	public void promoteProxy(PromoteProxyRequest promoteProxyRequest, String jsessionid) throws ItorixException {
		ProxyPortfolio proxyPortfolio = promoteProxyRequest.getPortfolio();
		Scm scm = promoteProxyRequest.getScm();
		String proxyId = proxyPortfolio.getProjects().get(0).getProxies().get(0).getId();
		Proxy proxy = getProxy(proxyId);
		proxyUttils.promoteProxy(proxyPortfolio, proxy, scm, jsessionid);
	}


	public Object searchProxy(ProxySearchRequest proxySearchRequest, String jsessionid) {
		Query query = new Query().addCriteria(Criteria.where("name").regex(proxySearchRequest.getName(), "i"));
		List<Proxy> proxies = mongoTemplate.find(query, Proxy.class);
		List<ProxySearchResponse> proxiesResponse = new ArrayList<>();
		proxies.stream().forEach(proxy -> {
			ProxySearchResponse response = new ProxySearchResponse();
			response.setId(proxy.getId());
			response.setName(proxy.getName());
			response.setSummary(proxy.getSummary());
			response.setOwner(proxy.getOwner());
			proxiesResponse.add(response);
		});
		return proxiesResponse;
	}

	public Object generateApigeeProxy(String proxyId,
      Proxy requests, String jsessionid,String connectorId)
			throws Exception {
		return proxyUttils.generateApigeeProxy(getProxy(proxyId), requests, jsessionid,connectorId);
	}
}

