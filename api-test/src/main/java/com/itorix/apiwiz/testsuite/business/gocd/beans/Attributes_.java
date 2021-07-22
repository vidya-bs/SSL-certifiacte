package com.itorix.apiwiz.testsuite.business.gocd.beans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"run_if", "on_cancel", "plugin_configuration", "configuration", "pipeline", "stage", "job",
		"is_source_a_file", "source", "destination"})
public class Attributes_ {

	@JsonProperty("run_if")
	private List<String> runIf = null;

	@JsonProperty("on_cancel")
	private Object onCancel;

	@JsonProperty("plugin_configuration")
	private PluginConfiguration pluginConfiguration;

	@JsonProperty("configuration")
	private List<Configuration> configuration = null;

	@JsonProperty("pipeline")
	private String pipeline;

	@JsonProperty("stage")
	private String stage;

	@JsonProperty("job")
	private String job;

	@JsonProperty("is_source_a_file")
	private Boolean isSourceAFile;

	@JsonProperty("source")
	private String source;

	@JsonProperty("destination")
	private String destination;

	@JsonProperty("command")
	private String command;

	@JsonProperty("arguments")
	private String[] arguments;

	@JsonProperty("workingDirectory")
	private String workingDirectory;

	@JsonIgnore
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();

	public Attributes_() {
	}

	public Attributes_(String runCondition, String pipeline, String stage, String job, String source,
			String destination) {
		runIf = new ArrayList<String>();
		runIf.add(runCondition);
		this.pipeline = pipeline;
		this.stage = stage;
		this.job = job;
		this.source = source;
		this.destination = destination;
		this.isSourceAFile = true;
	}

	public Attributes_(String runCondition, String tasks, String additionalOptions, String gradleHome) {
		runIf = new ArrayList<String>();
		runIf.add(runCondition);
		if (gradleHome != null) {
			pluginConfiguration = new PluginConfiguration("gradle.task.plugin", "1");
			configuration = new ArrayList<>();
			configuration.add(new Configuration("GradleHome", ""));
			configuration.add(new Configuration("Tasks", tasks));
			configuration.add(new Configuration("Daemon", ""));
			configuration.add(new Configuration("UseWrapper", "false"));
			configuration.add(new Configuration("MakeWrapperExecutable", "false"));
			configuration.add(new Configuration("Offline", "false"));
			configuration.add(new Configuration("Debug", "false"));
			configuration.add(new Configuration("AdditionalOptions", additionalOptions));
			configuration.add(new Configuration("GradleHome", gradleHome));
		}
	}

	public Attributes_(String runCondition, String command, String arguments, String workingDirectory,
			boolean isCommand) {
		runIf = new ArrayList<String>();
		runIf.add(runCondition);
		this.command = command;
		if (arguments != null) {
			this.arguments = arguments.split(" ");
		}
		this.workingDirectory = workingDirectory;
	}

	public void setAdditionalProperties(Map<String, Object> additionalProperties) {
		this.additionalProperties = additionalProperties;
	}

	@JsonProperty("run_if")
	public List<String> getRunIf() {
		return runIf;
	}

	@JsonProperty("run_if")
	public void setRunIf(List<String> runIf) {
		this.runIf = runIf;
	}

	@JsonProperty("on_cancel")
	public Object getOnCancel() {
		return onCancel;
	}

	@JsonProperty("on_cancel")
	public void setOnCancel(Object onCancel) {
		this.onCancel = onCancel;
	}

	@JsonProperty("plugin_configuration")
	public PluginConfiguration getPluginConfiguration() {
		return pluginConfiguration;
	}

	@JsonProperty("plugin_configuration")
	public void setPluginConfiguration(PluginConfiguration pluginConfiguration) {
		this.pluginConfiguration = pluginConfiguration;
	}

	@JsonProperty("configuration")
	public List<Configuration> getConfiguration() {
		return configuration;
	}

	@JsonProperty("configuration")
	public void setConfiguration(List<Configuration> configuration) {
		this.configuration = configuration;
	}

	@JsonProperty("pipeline")
	public String getPipeline() {
		return pipeline;
	}

	@JsonProperty("pipeline")
	public void setPipeline(String pipeline) {
		this.pipeline = pipeline;
	}

	@JsonProperty("stage")
	public String getStage() {
		return stage;
	}

	@JsonProperty("stage")
	public void setStage(String stage) {
		this.stage = stage;
	}

	@JsonProperty("job")
	public String getJob() {
		return job;
	}

	@JsonProperty("job")
	public void setJob(String job) {
		this.job = job;
	}

	@JsonProperty("is_source_a_file")
	public Boolean getIsSourceAFile() {
		return isSourceAFile;
	}

	@JsonProperty("is_source_a_file")
	public void setIsSourceAFile(Boolean isSourceAFile) {
		this.isSourceAFile = isSourceAFile;
	}

	@JsonProperty("source")
	public String getSource() {
		return source;
	}

	@JsonProperty("source")
	public void setSource(String source) {
		this.source = source;
	}

	@JsonProperty("destination")
	public String getDestination() {
		return destination;
	}

	@JsonProperty("destination")
	public void setDestination(String destination) {
		this.destination = destination;
	}

	@JsonAnyGetter
	public Map<String, Object> getAdditionalProperties() {
		return this.additionalProperties;
	}

	@JsonAnySetter
	public void setAdditionalProperty(String name, Object value) {
		this.additionalProperties.put(name, value);
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public String[] getArguments() {
		return arguments;
	}

	public void setArguments(String[] arguments) {
		this.arguments = arguments;
	}

	public String getWorkingDirectory() {
		return workingDirectory;
	}

	public void setWorkingDirectory(String workingDirectory) {
		this.workingDirectory = workingDirectory;
	}
}
