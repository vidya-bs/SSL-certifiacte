package com.itorix.apiwiz.devportal.business.diff.v3.compare;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import com.itorix.apiwiz.devportal.diff.v3.model.ChangedParameter;

import io.swagger.v3.oas.models.parameters.Parameter;

public class ParameterDiff {

	private List<Parameter> increased;
	private List<Parameter> missing;
	private List<ChangedParameter> changed;

	public ParameterDiff() {
		this.increased = new ArrayList<Parameter>();
		this.missing = new ArrayList<Parameter>();
		this.changed = new ArrayList<ChangedParameter>();
	}

	public ParameterDiff diff(List<Parameter> oldParams, List<Parameter> newParams) {
		if (null == oldParams)
			oldParams = new ArrayList<>();
		if (null == newParams)
			newParams = new ArrayList<>();

		ListDiff<Parameter> paramDiff = ListDiff.diff(oldParams, newParams, (t, param) -> {
			for (Parameter para : t) {
				if (param.getName().equals(para.getName())) {
					return para;
				}
			}
			return null;
		});

		this.increased.addAll(paramDiff.getIncreased());
		this.missing.addAll(paramDiff.getMissing());
		Map<Parameter, Parameter> shared = paramDiff.getShared();

		shared.forEach((leftPara, rightPara) -> {
			ChangedParameter changedParameter = new ChangedParameter();
			changedParameter.setLeftParameter(leftPara);
			changedParameter.setRightParameter(rightPara);

			// name
			String rightName = rightPara.getName();
			String lefName = leftPara.getName();
			if (!(Objects.isNull(rightName) || Objects.isNull(lefName))) {
				changedParameter.setIsNameChanged(!rightName.equals(lefName));
			}

			// in
			String rightIn = rightPara.getIn();
			String leftIn = leftPara.getIn();
			if (!(Objects.isNull(rightIn) || Objects.isNull(leftIn))) {
				changedParameter.setInChanged(!rightIn.equals(leftIn));
			}

			// description
			String description = rightPara.getDescription();
			String oldPescription = leftPara.getDescription();
			if (StringUtils.isBlank(description))
				description = "";
			if (StringUtils.isBlank(oldPescription))
				oldPescription = "";
			changedParameter.setChangeDescription(!description.equals(oldPescription));

			// required
			Boolean rightRequired = rightPara.getRequired();
			Boolean leftRequired = leftPara.getRequired();
			if (!(Objects.isNull(rightRequired) || Objects.isNull(leftRequired))) {
				changedParameter.setChangeRequired(leftRequired != rightRequired);
			}

			// deprecated
			Boolean rightDep = rightPara.getDeprecated();
			Boolean leftDep = leftPara.getDeprecated();
			if (!(Objects.isNull(rightDep) || Objects.isNull(leftDep))) {
				changedParameter.setIsDeprecatedChanged(rightDep != leftDep);
			}

			// allow empty value
			Boolean rightAllow = rightPara.getAllowEmptyValue();
			Boolean leftAllow = leftPara.getAllowEmptyValue();
			if (!(Objects.isNull(rightAllow) || Objects.isNull(leftAllow))) {
				changedParameter.setIsAllowEmptyValue(rightAllow != leftAllow);
			}

			// ref
			String rightRef = rightPara.get$ref();
			String leftRef = leftPara.get$ref();
			if (!(Objects.isNull(rightRef) || Objects.isNull(leftRef))) {
				changedParameter.setIs$refChanged(!rightRef.equals(leftRef));
			}

			if (changedParameter.isDiff()) {
				this.changed.add(changedParameter);
			}
		});

		return this;
	}

	public List<Parameter> getIncreased() {
		return increased;
	}

	public void setIncreased(List<Parameter> increased) {
		this.increased = increased;
	}

	public List<Parameter> getMissing() {
		return missing;
	}

	public void setMissing(List<Parameter> missing) {
		this.missing = missing;
	}

	public List<ChangedParameter> getChanged() {
		return changed;
	}

	public void setChanged(List<ChangedParameter> changed) {
		this.changed = changed;
	}
}
