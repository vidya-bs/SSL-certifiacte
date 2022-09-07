package com.itorix.apiwiz.analytics.beans;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document("PessimisticLock")
public class PessimisticLock {
	@Id
	private String id;
	private Date ts;
	private String keeperId;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Date getTs() {
		return ts;
	}

	public void setTs(Date ts) {
		this.ts = ts;
	}

	public String getKeeperId() {
		return keeperId;
	}

	public void setKeeperId(String keeperId) {
		this.keeperId = keeperId;
	}
}
