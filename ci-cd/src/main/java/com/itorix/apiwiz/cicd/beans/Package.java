package com.itorix.apiwiz.cicd.beans;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

@Document(collection = "CICD.Release.Package.List")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Package {

	public static final String LABEL_CREATED_TIME = "metadata.cts";
	@Id
	private String id;

	@Indexed(unique = true)
	private String packageName;

	private String packageId;
	private String description;
	private List<PackagePortfolio> projects;
	private Metadata metadata;
	private String state;
	private String approvedBy;
	private String approvedOn;
	private String comments;
	private List<Proxy> pipelines;
	private List<PackageMetadata> packageMetadata;

	@JsonIgnore
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	/** @return the packageId */
	public String getPackageId() {
		return packageId;
	}

	/**
	 * @param packageId
	 *            the packageId to set
	 */
	public void setPackageId(String packageId) {
		this.packageId = packageId;
	}

	/** @return the description */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/** @return the project */
	public List<PackagePortfolio> getProjects() {
		return projects;
	}

	/**
	 * @param project
	 *            the project to set
	 */
	public void setProjects(List<PackagePortfolio> projects) {
		this.projects = projects;
	}

	/** @return the metadata */
	public Metadata getMetadata() {
		return metadata;
	}

	/**
	 * @param metadata
	 *            the metadata to set
	 */
	public void setMetadata(Metadata metadata) {
		this.metadata = metadata;
	}

	/** @return the state */
	public String getState() {
		return state;
	}

	/**
	 * @param state
	 *            the state to set
	 */
	public void setState(String state) {
		this.state = state;
	}

	/** @return the approvedBy */
	public String getApprovedBy() {
		return approvedBy;
	}

	/**
	 * @param approvedBy
	 *            the approvedBy to set
	 */
	public void setApprovedBy(String approvedBy) {
		this.approvedBy = approvedBy;
	}

	/** @return the approvedOn */
	public String getApprovedOn() {
		return approvedOn;
	}

	/**
	 * @param approvedOn
	 *            the approvedOn to set
	 */
	public void setApprovedOn(String approvedOn) {
		this.approvedOn = approvedOn;
	}

	/** @return the comments */
	public String getComments() {
		return comments;
	}

	/**
	 * @param comments
	 *            the comments to set
	 */
	public void setComments(String comments) {
		this.comments = comments;
	}

	/** @return the packageMetadata */
	public List<PackageMetadata> getPackageMetadata() {
		return packageMetadata;
	}

	/**
	 * @param packageMetadata
	 *            the packageMetadata to set
	 */
	public void setPackageMetadata(List<PackageMetadata> packageMetadata) {
		this.packageMetadata = packageMetadata;
	}

	/** @return the pipelines */
	public List<Proxy> getPipelines() {
		return pipelines;
	}

	/**
	 * @param pipelines
	 *            the pipelines to set
	 */
	public void setPipelines(List<Proxy> pipelines) {
		this.pipelines = pipelines;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
}
