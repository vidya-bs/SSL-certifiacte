package com.itorix.apiwiz.design.studio.model;

public class SwaggerLockResponse {
	private boolean lockStatus;
	private String lockedBy;
	private Long lockedAt;
	private String lockedByUserId;

	public boolean isLockStatus() {
		return lockStatus;
	}

	public void setLockStatus(boolean lockStatus) {
		this.lockStatus = lockStatus;
	}

	public String getLockedBy() {
		return lockedBy;
	}

	public void setLockedBy(String lockedBy) {
		this.lockedBy = lockedBy;
	}

	public Long getLockedAt() {
		return lockedAt;
	}

	public void setLockedAt(Long lockedAt) {
		this.lockedAt = lockedAt;
	}

	public String getLockedByUserId() {
		return lockedByUserId;
	}

	public void setLockedByUserId(String lockedByUserId) {
		this.lockedByUserId = lockedByUserId;
	}
	
	

	

}
