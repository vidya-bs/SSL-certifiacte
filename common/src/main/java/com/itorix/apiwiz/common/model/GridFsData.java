package com.itorix.apiwiz.common.model;

import java.io.InputStream;

import com.mongodb.DBObject;

public class GridFsData {

	private String id;
	private String filePath;
	private String filename;
	private String contentType;
	private DBObject metaData;
	private InputStream inputStream;

	public GridFsData() {
		super();
	}

	public GridFsData(String filePath, String filename) {
		super();
		this.filePath = filePath;
		this.filename = filename;
	}

	public GridFsData(InputStream inputStream, String filename) {
		super();
		this.inputStream = inputStream;
		this.filename = filename;
	}

	public GridFsData(String id, String filePath, String filename) {
		super();
		this.id = id;
		this.filePath = filePath;
		this.filename = filename;
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public DBObject getMetaData() {
		return metaData;
	}

	public void setMetaData(DBObject metaData) {
		this.metaData = metaData;
	}
}
