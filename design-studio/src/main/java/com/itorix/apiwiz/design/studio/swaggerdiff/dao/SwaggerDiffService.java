package com.itorix.apiwiz.design.studio.swaggerdiff.dao;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itorix.apiwiz.common.model.SwaggerChangeLog;
import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.util.Date.DateUtil;
import com.itorix.apiwiz.design.studio.businessimpl.SwaggerBusinessImpl;
import com.itorix.apiwiz.design.studio.model.Swagger3VO;
import com.itorix.apiwiz.design.studio.model.SwaggerVO;
import com.itorix.apiwiz.design.studio.swaggerdiff.model.ReleaseNotesVO;
import com.itorix.apiwiz.design.studio.swaggerdiff.model.SwaggerChangeLogResponse;
import com.itorix.apiwiz.identitymanagement.dao.BaseRepository;
import com.itorix.apiwiz.identitymanagement.model.Pagination;
import com.itorix.hyggee.oas2.changelog.diff.SwaggerDiff;
import com.itorix.hyggee.oas2.changelog.output.MarkdownRender;
import com.itorix.hyggee.oas3.changelog.output.Swagger3MarkdownRender;
import com.itorix.hyggee.oas3.changelog.compare.OpenAPIDiff;
@Slf4j
/** @author sudhakar */
@Service
public class SwaggerDiffService {
	@Autowired
	private SwaggerBusinessImpl swaggerService;

	@Autowired
	private BaseRepository baseRepository;

	@Autowired
	private ObjectMapper mapper = new ObjectMapper();

	@Autowired
	private MongoTemplate mongoTemplate;

	/**
	 * @param name
	 * @param revision
	 * 
	 * @return
	 */
	private String getSwagger(String name, int revision, String oas) {
		try {
			String swagger;
			if (oas.equals("3.0")) {
				Swagger3VO vo = swaggerService.getSwagger3(name, null);
				Swagger3VO swagger3VO = swaggerService.getSwagger3WithVersionNumber(vo.getName(), revision, null);
				// swagger = mapper.readValue(swagger3VO.getSwagger(),
				// JsonNode.class);
				return swagger3VO.getSwagger();
			} else {
				SwaggerVO vo = swaggerService.getSwagger(name, null);
				SwaggerVO swaggerVO = swaggerService.getSwaggerWithVersionNumber(vo.getName(), revision, null);
				// swagger = mapper.readValue(swaggerVO.getSwagger(),
				// JsonNode.class);
				return swaggerVO.getSwagger();
			}
		} catch (ItorixException e) {
			log.error("Exception occurred", e);
		}
		return null;
	}

	private JsonNode getSwagger3(String name, int revision, String oas) {
		try {
			JsonNode swagger;
			Swagger3VO vo = swaggerService.getSwagger3(name, null);
			Swagger3VO swagger3VO = swaggerService.getSwagger3WithVersionNumber(vo.getName(), revision, null);
			swagger = mapper.readValue(swagger3VO.getSwagger(), JsonNode.class);
			return swagger;
		} catch (Exception e) {
			log.error("Exception occurred", e);
		}
		return null;
	}

	/**
	 * @param name
	 * @param oldRevision
	 * @param newRevision
	 * 
	 * @return
	 */
	public String getDiff(String name, int oldRevision, int newRevision, String oas) {
		if (oas.equals("3.0")) {
			return new Swagger3MarkdownRender(
					OpenAPIDiff.compare(getSwagger3(name, oldRevision, oas), getSwagger3(name, newRevision, oas)))
							.render();
		}
		return new MarkdownRender(
				SwaggerDiff.compareV2Raw(getSwagger(name, oldRevision, oas), getSwagger(name, newRevision, oas)))
						.render();
	};

	/**
	 * @param notes
	 * 
	 * @return
	 */
	public boolean saveUpdateReleaseNotes(ReleaseNotesVO notes) {
		ReleaseNotesVO note = getReleaseNotes(notes.getYear(), notes.getOas());
		if (note != null) {
			note.setNotes(notes.getNotes());
			baseRepository.save(note);
			return true;
		} else {
			baseRepository.save(notes);
			return true;
		}
	}

	/**
	 * @param year
	 * 
	 * @return
	 */
	public ReleaseNotesVO getReleaseNotes(String year, String oas) {
		if (year == null)
			year = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
		return baseRepository.findOne("year", year, "oas", oas, ReleaseNotesVO.class);
	}

	/**
	 * @param year
	 * 
	 * @return
	 */
	public SwaggerChangeLog getSwaggerChangeLog(String id) {
		return baseRepository.findOne("id", id, SwaggerChangeLog.class);
	}

	/** @return */
	public List<String> getYears() {
		List<String> years = new ArrayList<String>();
		for (ReleaseNotesVO releaseNotesVO : baseRepository.findAll("year", "-", ReleaseNotesVO.class)) {
			years.add(releaseNotesVO.getYear());
		}
		return years;
	}

	/**
	 * @param notes
	 * @param oas
	 * @param swagger
	 * 
	 * @return
	 */
	public boolean saveReleaseNotes(String notes, String oas) {
		String yearInString = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
		ReleaseNotesVO note = getReleaseNotes(yearInString, oas);
		if (note != null) {
			StringBuffer sb = new StringBuffer();
			sb.append(notes);
			sb.append(note.getNotes());
			note.setNotes(sb.toString());
			return saveUpdateReleaseNotes(note);
		} else {
			note = new ReleaseNotesVO();
			note.setYear(yearInString);
			note.setOas(oas);
			note.setNotes(notes);
			return saveUpdateReleaseNotes(note);
		}
	}

	public void saveReleaseNotes(String text, String oas, String swaggerId, String oldRevision, String newRevision,
			String summary) throws ItorixException {
		saveReleaseNotes(text, oas);
		saveSwaggerReleaseNotes(text, oas, swaggerId, oldRevision, newRevision, summary);
	}

	public void saveSwaggerReleaseNotes(String text, String oas, String swaggerId, String oldRevision,
			String newRevision, String summary) throws ItorixException {
		String swaggerName = null;
		if (oas.equals("2.0")) {
			SwaggerVO vo = getSwagger(swaggerId);
			swaggerName = vo != null ? vo.getName() : null;
		} else {
			Swagger3VO vo = getSwagger3(swaggerId);
			swaggerName = vo != null ? vo.getName() : null;
		}
		if (swaggerName != null) {
			String yearInString = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
			SwaggerChangeLog swaggerChangeLog = new SwaggerChangeLog(yearInString, oas, text, swaggerName, oldRevision,
					newRevision, swaggerId, summary);
			baseRepository.save(swaggerChangeLog);
		} else {
			log.info("no swagger");
			throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Swagger-1000")), "Swagger-1000");
		}
	}

	public void updateSwaggerReleaseNotes(String id, String text, String oas, String summary) throws ItorixException {
		SwaggerChangeLog swaggerChangeLog = getSwaggerChangeLog(id);
		if (swaggerChangeLog != null) {
			if (text != null && text.trim() != "")
				swaggerChangeLog.setNotes(text);
			if (summary != null && summary.trim() != "")
				swaggerChangeLog.setSummary(summary);
			baseRepository.save(swaggerChangeLog);
		} else
			throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Swagger-1000")), "Swagger-1000");
	}

	/**
	 * @param year
	 * 
	 * @return
	 * 
	 * @throws ParseException
	 * @throws ItorixException
	 */
	public SwaggerChangeLogResponse getSwaggerIdReleaseNotes(String timeRange, String oas, String swaggerId, int offset)
			throws ParseException, ItorixException {
		String swaggerName = null;
		if (oas.equals("2.0")) {
			SwaggerVO vo = getSwagger(swaggerId);
			swaggerName = vo != null ? vo.getName() : null;
		} else {
			Swagger3VO vo = getSwagger3(swaggerId);
			swaggerName = vo != getSwagger3(swaggerName) ? vo.getName() : null;
		}

		if (swaggerName != null) {
			Query query = null;
			if (timeRange != null) {
				SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
				String timeRanges[] = timeRange.split("~");
				Date startDate = format.parse(timeRanges[0]);
				Date endDate = format.parse(timeRanges[1]);
				long StartTime = DateUtil.getStartOfDay(startDate).getTime();
				long endDateTime = DateUtil.getEndOfDay(endDate).getTime();
				query = new Query(Criteria.where("swaggerId").is(swaggerId).and("oas").is(oas).and("mts").gte(StartTime)
						.lte(endDateTime)).with(Sort.by(Direction.DESC, "mts"))
								.skip(offset > 0 ? ((offset - 1) * 10) : 0).limit(10);
			} else {
				query = new Query(Criteria.where("swaggerId").is(swaggerId).and("oas").is(oas))
						.with(Sort.by(Direction.DESC, "mts")).skip(offset > 0 ? ((offset - 1) * 10) : 0).limit(10);
			}
			List<SwaggerChangeLog> list = baseRepository.find(query, SwaggerChangeLog.class);
			for (SwaggerChangeLog log : list)
				log.setNotes(null);
			long counter;
			if (timeRange != null) {
				SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
				String timeRanges[] = timeRange.split("~");
				Date startDate = format.parse(timeRanges[0]);
				Date endDate = format.parse(timeRanges[1]);
				long StartTime = DateUtil.getStartOfDay(startDate).getTime();
				long endDateTime = DateUtil.getEndOfDay(endDate).getTime();
				counter = mongoTemplate.count(new Query(Criteria.where("swaggerId").is(swaggerId).and("oas").is(oas)
						.and("mts").gte(StartTime).lte(endDateTime)), SwaggerChangeLog.class);
			} else
				counter = mongoTemplate.count(new Query(Criteria.where("swaggerId").is(swaggerId).and("oas").is(oas)),
						SwaggerChangeLog.class);
			Pagination pagination = new Pagination();
			pagination.setOffset(offset);
			pagination.setTotal(counter);
			pagination.setPageSize(10);

			SwaggerChangeLogResponse response = new SwaggerChangeLogResponse();
			if (list == null)
				response.setData(new ArrayList());
			else
				response.setData(list);
			response.setPagination(pagination);
			return response;
		}
		throw new ItorixException(String.format(ErrorCodes.errorMessage.get("Swagger-1000")), "Swagger-1000");
	}

	/**
	 * @param notes
	 * @param year
	 * 
	 * @return
	 */
	public boolean updateReleaseNotes(String notes, String year, String oas) {
		if (year == null)
			year = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
		ReleaseNotesVO note = getReleaseNotes(year, oas);
		if (note != null) {
			note.setNotes(notes);
			return saveUpdateReleaseNotes(note);
		} else {
			note = new ReleaseNotesVO();
			note.setYear(year);
			note.setOas(oas);
			note.setNotes(notes);
			return saveUpdateReleaseNotes(note);
		}
	}

	/**
	 * getSwagger
	 *
	 * @param name
	 * 
	 * @return
	 */
	public SwaggerVO getSwagger(String name) {
		SwaggerVO vo = null;
		vo = baseRepository.findOne("id", name, SwaggerVO.class);
		if (vo != null)
			return vo;
		vo = baseRepository.findOne("swaggerId", name, SwaggerVO.class);
		if (vo != null)
			return vo;
		vo = baseRepository.findOne("name", name, SwaggerVO.class);
		return vo;
	}

	/**
	 * getSwagger3
	 *
	 * @param name
	 * 
	 * @return
	 */
	public Swagger3VO getSwagger3(String name) {
		Swagger3VO vo = null;
		vo = baseRepository.findOne("id", name, Swagger3VO.class);
		if (vo != null)
			return vo;
		vo = baseRepository.findOne("swaggerId", name, Swagger3VO.class);
		if (vo != null)
			return vo;
		vo = baseRepository.findOne("name", name, Swagger3VO.class);
		if (vo != null)
			return vo;
		return vo;
	}
}
