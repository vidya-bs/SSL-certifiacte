package com.itorix.apiwiz.common.model.proxystudio;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class OrgEnv {

	private String name;
	private String type = "saas";
	private List<Env> envs;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Env> getEnvs() {
		return envs;
	}

	public void setEnvs(List<Env> envs) {
		this.envs = envs;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@JsonIgnore
	public void addEnv(OrgEnv orgEnv) {
		if (this.envs != null) {
			for (Env newEnv : orgEnv.getEnvs()) {
				boolean contains = false;
				for (Env env : this.envs)
					if (env.getName().equals(newEnv.getName()))
						contains = true;
				if (!contains)
					this.envs.add(newEnv);
			}
		} else {
			this.envs = orgEnv.getEnvs();
		}
	}
}
