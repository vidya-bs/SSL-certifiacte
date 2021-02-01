package com.itorix.apiwiz.design.studio.model;

import org.springframework.data.mongodb.core.mapping.Document;

import com.itorix.apiwiz.identitymanagement.model.AbstractObject;


@Document(collection = "Design.Swagger.Review.Coments")
public class SwaggerReviewComents extends AbstractObject {
	private String userName;
	private String userEmailId;
	private String swaggerName;
	private Integer revision;
	private String commentId;
	private String comment;
	private String commentedBy;
	private String externalFlag;
	private Long startTime;
	private Long endTime;

	public String getSwaggerName() {
		return swaggerName;
	}

	public void setSwaggerName(String swaggerName) {
		this.swaggerName = swaggerName;
	}

	public Integer getRevision() {
		return revision;
	}

	public void setRevision(Integer revision) {
		this.revision = revision;
	}

	public String getCommentId() {
		return commentId;
	}

	public void setCommentId(String commentId) {
		this.commentId = commentId;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getCommentedBy() {
		return commentedBy;
	}

	public void setCommentedBy(String commentedBy) {
		this.commentedBy = commentedBy;
	}

	public Long getStartTime() {
		return startTime;
	}

	public void setStartTime(Long startTime) {
		this.startTime = startTime;
	}

	public Long getEndTime() {
		return endTime;
	}

	public void setEndTime(Long endTime) {
		this.endTime = endTime;
	}

	public String getExternalFlag() {
		return externalFlag;
	}

	public void setExternalFlag(String externalFlag) {
		this.externalFlag = externalFlag;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserEmailId() {
		return userEmailId;
	}

	public void setUserEmailId(String userEmailId) {
		this.userEmailId = userEmailId;
	}

}
