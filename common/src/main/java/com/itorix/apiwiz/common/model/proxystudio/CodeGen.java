package com.itorix.apiwiz.common.model.proxystudio;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class CodeGen {
	
	private PolicyTemplates policyTemplates;

	private Proxy proxy;

	private Target target;

	public PolicyTemplates getPolicyTemplates ()
	{
		return policyTemplates;
	}

	public void setPolicyTemplates (PolicyTemplates policyTemplates)
	{
		this.policyTemplates = policyTemplates;
	}

	public Proxy getProxy ()
	{
		return proxy;
	}

	public void setProxy (Proxy proxy)
	{
		this.proxy = proxy;
	}

	public Target getTarget ()
	{
		return target;
	}

	public void setTarget (Target target)
	{
		this.target = target;
	}

	@Override
	public String toString()
	{
		return "[policyTemplates = "+policyTemplates+", proxy = "+proxy+", target = "+target+"]";
	}
}
