package com.itorix.apiwiz.common.model.proxystudio;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Flows {
	private Flow[] flow;

	public Flow[] getFlow() {
		return flow;
	}

	public void setFlow(Flow[] flow) {
		this.flow = flow;
	}

	public void setFlows(List<Flow> flowList) {
		Flow flowsArray[] = new Flow[flowList.size()];
		for (int i = 0; i < flowList.size(); i++) {
			flowsArray[i] = flowList.get(i);
		}
		this.flow = flowsArray;
	}

	public void addFLow(Flow flow) {
		if (this.flow != null) {
			int length = this.flow.length;
			this.flow[length] = flow;
		} else {

		}
	}

	@Override
	public String toString() {
		return "[flow = " + flow + "]";
	}
}
