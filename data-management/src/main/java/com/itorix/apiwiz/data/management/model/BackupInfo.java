package com.itorix.apiwiz.data.management.model;

public interface BackupInfo {

	public String getJfrogUrl();

	public void setJfrogUrl(String jfrogUrl);

	public long getTimeTaken();

	public void setTimeTaken(long timeTaken);

	public String getOrganization();

	public void setOrganization(String organization);

	public String getStatus();

	public void setStatus(String status);

	public String getBackUpLevel();

	public void setBackUpLevel(String backUpLevel);

	public String getTempToken();

	public void setTempToken(String tempToken);
}
