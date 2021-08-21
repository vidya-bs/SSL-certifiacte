package com.itorix.apiwiz.common.model.projectmanagement;

public class Stage {
	private String name;
	private String orgName;
	private String envName;
	private int sequenceID;
	private String type;
	private String isSaaS;
	private UnitTests unitTests;
	private CodeCoverage codeCoverage;

	public String getOrgName() {
		return orgName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	public String getEnvName() {
		return envName;
	}

	public void setEnvName(String envName) {
		this.envName = envName;
	}

	public int getSequenceID() {
		return sequenceID;
	}

	public void setSequenceID(int sequenceID) {
		this.sequenceID = sequenceID;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public UnitTests getUnitTests() {
		return unitTests;
	}

	public void setUnitTests(UnitTests unitTests) {
		this.unitTests = unitTests;
	}

	public CodeCoverage getCodeCoverage() {
		return codeCoverage;
	}

	public void setCodeCoverage(CodeCoverage codeCoverage) {
		this.codeCoverage = codeCoverage;
	}

	@Override
	public String toString() {
		return "Stage [orgName=" + orgName + ", envName=" + envName + ", sequenceID=" + sequenceID + ", type=" + type
				+ ", unitTests=" + unitTests + ", codeCoverage=" + codeCoverage + "]";
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIsSaaS() {
		return isSaaS;
	}

	public void setIsSaaS(String isSaaS) {
		this.isSaaS = isSaaS;
	}
}
