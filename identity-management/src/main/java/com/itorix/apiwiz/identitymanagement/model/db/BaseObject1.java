package com.itorix.apiwiz.identitymanagement.model.db;

public interface BaseObject1 {

	String getId();

	void setId(String id);

	Long getCts();

	void setCts(Long cts);

	Long getMts();

	void setMts(Long mts);

	String getCreatedBy();

	void setCreatedBy(String createdBy);

	String getModifiedBy();

	void setModifiedBy(String modifiedBy);

	String getCreatedUserName();

	void setCreatedUserName(String createdUserName);

	String getModifiedUserName();

	void setModifiedUserName(String modifiedUserName);

}
