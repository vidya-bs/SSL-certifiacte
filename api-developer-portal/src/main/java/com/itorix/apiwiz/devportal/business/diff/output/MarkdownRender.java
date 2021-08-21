package com.itorix.apiwiz.devportal.business.diff.output;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import io.swagger.models.HttpMethod;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.Property;

import java.util.Map.Entry;

import com.itorix.apiwiz.devportal.business.diff.SwaggerDiff;
import com.itorix.apiwiz.devportal.model.ChangedEndpoint;
import com.itorix.apiwiz.devportal.model.ChangedOperation;
import com.itorix.apiwiz.devportal.model.ChangedParameter;
import com.itorix.apiwiz.devportal.model.ElProperty;
import com.itorix.apiwiz.devportal.model.Endpoint;

public class MarkdownRender implements Render {

	final String H1 = "# ";
	final String H2 = "## ";
	final String H3 = "### ";
	final String H4 = "#### ";
	final String BLOCKQUOTE = "> ";
	final String CODE = "`";
	final String PRE_CODE = "    ";
	final String PRE_LI = "    ";
	final String LI = "* ";
	final String HR = "---\n";

	private SwaggerDiff diff;

	public MarkdownRender(SwaggerDiff diff) {
		this.diff = diff;
	}

	public String render() {
		List<Endpoint> newEndpoints = diff.getNewEndpoints();
		String ol_newEndpoint = ol_newEndpoint(newEndpoints);

		List<Endpoint> missingEndpoints = diff.getMissingEndpoints();
		String ol_missingEndpoint = ol_missingEndpoint(missingEndpoints);

		List<ChangedEndpoint> changedEndpoints = diff.getChangedEndpoints();
		String ol_changed = ol_changed(changedEndpoints);

		return renderHtml(diff.getSwaggerName(), diff.getSwaggerDescription(), diff.getOldVersion(),
				diff.getNewVersion(), ol_newEndpoint, ol_missingEndpoint, ol_changed);
	}

	public String renderHtml(String swaggerName, String swaggerDescription, String oldVersion, String newVersion,
			String ol_new, String ol_miss, String ol_changed) {
		StringBuffer sb = new StringBuffer();
		sb.append(H1).append(swaggerName + " Modified : " + getTime() + "UTC").append("\n").append(HR);
		sb.append(H4).append(swaggerDescription).append("\n").append(HR);
		sb.append(H2).append("Version " + oldVersion + " to " + newVersion).append("\n").append(HR);
		sb.append(H3).append("What's New").append("\n").append(HR).append(ol_new).append("\n").append(H3)
				.append("What's Deprecated").append("\n").append(HR).append(ol_miss).append("\n").append(H3)
				.append("What's Changed").append("\n").append(HR).append(ol_changed);
		return sb.toString();
	}

	private String ol_newEndpoint(List<Endpoint> endpoints) {
		if (null == endpoints)
			return "";
		StringBuffer sb = new StringBuffer();
		for (Endpoint endpoint : endpoints) {
			sb.append(li_newEndpoint(endpoint.getMethod().toString(), endpoint.getPathUrl(), endpoint.getSummary()));
		}
		return sb.toString();
	}

	private String li_newEndpoint(String method, String path, String desc) {
		StringBuffer sb = new StringBuffer();
		sb.append(LI).append(CODE).append(method).append(CODE).append(" " + path).append(" " + desc + "\n");
		return sb.toString();
	}

	private String ol_missingEndpoint(List<Endpoint> endpoints) {
		if (null == endpoints)
			return "";
		StringBuffer sb = new StringBuffer();
		for (Endpoint endpoint : endpoints) {
			sb.append(li_newEndpoint(endpoint.getMethod().toString(), endpoint.getPathUrl(), endpoint.getSummary()));
		}
		return sb.toString();
	}

	private String ol_changed(List<ChangedEndpoint> changedEndpoints) {
		if (null == changedEndpoints)
			return "";
		StringBuffer sb = new StringBuffer();
		for (ChangedEndpoint changedEndpoint : changedEndpoints) {
			String pathUrl = changedEndpoint.getPathUrl();
			Map<HttpMethod, ChangedOperation> changedOperations = changedEndpoint.getChangedOperations();
			for (Entry<HttpMethod, ChangedOperation> entry : changedOperations.entrySet()) {
				String method = entry.getKey().toString();
				ChangedOperation changedOperation = entry.getValue();
				String desc = changedOperation.getSummary();

				StringBuffer ul_detail = new StringBuffer();
				if (changedOperation.isDiffParam()) {
					ul_detail.append(PRE_LI).append("Parameter").append(ul_param(changedOperation));
				}
				if (changedOperation.isDiffProp()) {
					ul_detail.append(PRE_LI).append("Return Type").append(ul_response(changedOperation));
				}
				sb.append(LI).append(CODE).append(method).append(CODE).append(" " + pathUrl).append(" " + desc + "  \n")
						.append(ul_detail);
			}
		}
		return sb.toString();
	}

	private String ul_response(ChangedOperation changedOperation) {
		List<ElProperty> addProps = changedOperation.getAddProps();
		List<ElProperty> delProps = changedOperation.getMissingProps();
		StringBuffer sb = new StringBuffer("\n\n");
		for (ElProperty prop : addProps) {
			sb.append(PRE_LI).append(PRE_CODE).append(li_addProp(prop) + "\n");
		}
		for (ElProperty prop : delProps) {
			sb.append(PRE_LI).append(PRE_CODE).append(li_missingProp(prop) + "\n");
		}
		return sb.toString();
	}

	private String li_missingProp(ElProperty prop) {
		Property property = prop.getProperty();
		StringBuffer sb = new StringBuffer("");
		sb.append("Delete ").append(prop.getEl())
				.append(null == property.getDescription() ? "" : (" //" + property.getDescription()));
		return sb.toString();
	}

	private String li_addProp(ElProperty prop) {
		Property property = prop.getProperty();
		StringBuffer sb = new StringBuffer("");
		sb.append("Add ").append(prop.getEl())
				.append(null == property.getDescription() ? "" : (" //" + property.getDescription()));
		return sb.toString();
	}

	private String ul_param(ChangedOperation changedOperation) {
		List<Parameter> addParameters = changedOperation.getAddParameters();
		List<Parameter> delParameters = changedOperation.getMissingParameters();
		List<ChangedParameter> changedParameters = changedOperation.getChangedParameter();
		StringBuffer sb = new StringBuffer("\n\n");
		for (Parameter param : addParameters) {
			sb.append(PRE_LI).append(PRE_CODE).append(li_addParam(param) + "\n");
		}
		for (ChangedParameter param : changedParameters) {
			List<ElProperty> increased = param.getIncreased();
			for (ElProperty prop : increased) {
				sb.append(PRE_LI).append(PRE_CODE).append(li_addProp(prop) + "\n");
			}
		}
		for (ChangedParameter param : changedParameters) {
			boolean changeRequired = param.isChangeRequired();
			boolean changeDescription = param.isChangeDescription();
			if (changeRequired || changeDescription)
				sb.append(PRE_LI).append(PRE_CODE).append(li_changedParam(param) + "\n");
		}
		for (ChangedParameter param : changedParameters) {
			List<ElProperty> missing = param.getMissing();
			for (ElProperty prop : missing) {
				sb.append(PRE_LI).append(PRE_CODE).append(li_missingProp(prop) + "\n");
			}
		}
		for (Parameter param : delParameters) {
			sb.append(PRE_LI).append(PRE_CODE).append(li_missingParam(param) + "\n");
		}
		return sb.toString();
	}

	private String li_addParam(Parameter param) {
		StringBuffer sb = new StringBuffer("");
		sb.append("Add ").append(param.getName())
				.append(null == param.getDescription() ? "" : (" //" + param.getDescription()));
		return sb.toString();
	}

	private String li_missingParam(Parameter param) {
		StringBuffer sb = new StringBuffer("");
		sb.append("Delete ").append(param.getName())
				.append(null == param.getDescription() ? "" : (" //" + param.getDescription()));
		return sb.toString();
	}

	private String li_changedParam(ChangedParameter changeParam) {
		boolean changeRequired = changeParam.isChangeRequired();
		boolean changeDescription = changeParam.isChangeDescription();
		Parameter rightParam = changeParam.getRightParameter();
		Parameter leftParam = changeParam.getLeftParameter();
		StringBuffer sb = new StringBuffer("");
		sb.append(rightParam.getName());
		if (changeRequired) {
			sb.append(" change into " + (rightParam.getRequired() ? "required" : "not required"));
		}
		if (changeDescription) {
			sb.append(" Notes ").append(leftParam.getDescription()).append(" change into ")
					.append(rightParam.getDescription());
		}
		return sb.toString();
	}

	private String getTime() {
		Date date = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		return dateFormat.format(date);
	}
}
