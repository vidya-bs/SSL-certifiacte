package com.itorix.apiwiz.cicd.dashboard.beans;

public class Pipelines {
	private String total;

	private Metrics[] metrics;

	private String proxy_name;

	private PipelineSuccessRatio pipelineSuccessRatio;

	private StageSuccessRatios[] stageSuccessRatios;

	private String pipelineName;

	public String getTotal() {
		return total;
	}

	public void setTotal(String total) {
		this.total = total;
	}

	public Metrics[] getMetrics() {
		return metrics;
	}

	public void setMetrics(Metrics[] metrics) {
		this.metrics = metrics;
	}

	public String getProxy_name() {
		return proxy_name;
	}

	public void setProxy_name(String proxy_name) {
		this.proxy_name = proxy_name;
	}

	public PipelineSuccessRatio getPipelineSuccessRatio() {
		return pipelineSuccessRatio;
	}

	public void setPipelineSuccessRatio(PipelineSuccessRatio pipelineSuccessRatio) {
		this.pipelineSuccessRatio = pipelineSuccessRatio;
	}

	public StageSuccessRatios[] getStageSuccessRatios() {
		return stageSuccessRatios;
	}

	public void setStageSuccessRatios(StageSuccessRatios[] stageSuccessRatios) {
		this.stageSuccessRatios = stageSuccessRatios;
	}

	public String getPipelineName() {
		return pipelineName;
	}

	public void setPipelineName(String pipelineName) {
		this.pipelineName = pipelineName;
	}

	@Override
	public String toString() {
		return "ClassPojo [total = " + total + ", metrics = " + metrics + ", proxy_name = " + proxy_name
				+ ", pipelineSuccessRatio = " + pipelineSuccessRatio + ", stageSuccessRatios = " + stageSuccessRatios
				+ ", pipelineName = " + pipelineName + "]";
	}
}
