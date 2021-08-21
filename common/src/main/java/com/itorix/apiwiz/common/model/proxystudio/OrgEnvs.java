package com.itorix.apiwiz.common.model.proxystudio;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class OrgEnvs {

	private List<OrgEnv> OrgEnvs;

	public List<OrgEnv> getOrgEnvs() {
		return OrgEnvs;
	}

	public void setOrgEnvs(List<OrgEnv> orgEnvs) {
		OrgEnvs = orgEnvs;
	}

	@JsonIgnore
	public void addOrgEnv(OrgEnv orgEnv) {
		if (this.OrgEnvs == null)
			this.OrgEnvs = new ArrayList<OrgEnv>();
		if (!containsOrg(orgEnv))
			this.OrgEnvs.add(orgEnv);
		else
			addEnv(orgEnv);
	}

	@JsonIgnore
	public boolean containsOrg(OrgEnv orgEnv) {
		for (OrgEnv org : OrgEnvs)
			if (org.getName().equals(orgEnv.getName()))
				return true;
		return false;
	}

	@JsonIgnore
	private void addEnv(OrgEnv orgEnv) {
		for (OrgEnv org : this.OrgEnvs)
			if (org.getName().equals(orgEnv.getName()))
				org.addEnv(orgEnv);
	}
}
