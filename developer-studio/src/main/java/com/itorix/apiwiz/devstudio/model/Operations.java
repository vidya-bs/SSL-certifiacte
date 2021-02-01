package com.itorix.apiwiz.devstudio.model;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.itorix.apiwiz.identitymanagement.model.User;


@Component("operations")

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Operations {
	
	private MultipartFile file;
	private String fileName;
	private int version;
	private String type;
	private String dir;
	private String jSessionid;
	private User user;
	private  String oas;
	private boolean isSwaggerInDB = false;
	
	public MultipartFile getFile() {
		return file;
	}
	public void setFile(MultipartFile file) {
		this.file = file;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getDir() {
		return dir;
	}
	public void setDir(String dir) {
		this.dir = dir;
	}
	public String getjSessionid() {
		return jSessionid;
	}
	public void setjSessionid(String jSessionid) {
		this.jSessionid = jSessionid;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public boolean isSwaggerInDB() {
		return isSwaggerInDB;
	}
	public void setSwaggerInDB(boolean isSwaggerInDB) {
		this.isSwaggerInDB = isSwaggerInDB;
	}
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}
	public String getOas() {
		return oas;
	}
	public void setOas(String oas) {
		this.oas = oas;
	}

}
