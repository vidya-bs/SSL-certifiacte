package com.itorix.apiwiz.devportal.diff.v3.model;

import java.util.ArrayList;
import java.util.List;

import io.swagger.v3.oas.models.parameters.Parameter;

public class ChangedParameter implements Changed {

	private List<ElProperty> increased = new ArrayList<ElProperty>();
	private List<ElProperty> missing = new ArrayList<ElProperty>();
	private List<ElProperty> changed = new ArrayList<ElProperty>();

	private Parameter leftParameter;
	private Parameter rightParameter;

	private boolean isChangeRequired;
	private boolean isChangeDescription;

	private Boolean isNameChanged;
	private Boolean inChanged;
	private Boolean isDeprecatedChanged;
	private Boolean isAllowEmptyValue;
	private Boolean is$refChanged;

	public Boolean getIsNameChanged() {
		return isNameChanged;
	}

	public void setIsNameChanged(Boolean isNameChanged) {
		this.isNameChanged = isNameChanged;
	}

	public Boolean getInChanged() {
		return inChanged;
	}

	public void setInChanged(Boolean inChanged) {
		this.inChanged = inChanged;
	}

	public Boolean getIsDeprecatedChanged() {
		return isDeprecatedChanged;
	}

	public void setIsDeprecatedChanged(Boolean isDeprecatedChanged) {
		this.isDeprecatedChanged = isDeprecatedChanged;
	}

	public Boolean getIsAllowEmptyValue() {
		return isAllowEmptyValue;
	}

	public void setIsAllowEmptyValue(Boolean isAllowEmptyValue) {
		this.isAllowEmptyValue = isAllowEmptyValue;
	}

	public Boolean getIs$refChanged() {
		return is$refChanged;
	}

	public void setIs$refChanged(Boolean is$refChanged) {
		this.is$refChanged = is$refChanged;
	}

	public boolean isChangeRequired() {
		return isChangeRequired;
	}

	public void setChangeRequired(boolean isChangeRequired) {
		this.isChangeRequired = isChangeRequired;
	}

	public boolean isChangeDescription() {
		return isChangeDescription;
	}

	public void setChangeDescription(boolean isChangeDescription) {
		this.isChangeDescription = isChangeDescription;
	}

	public Parameter getLeftParameter() {
		return leftParameter;
	}

	public void setLeftParameter(Parameter leftPara) {
		this.leftParameter = leftPara;
	}

	public Parameter getRightParameter() {
		return rightParameter;
	}

	public void setRightParameter(Parameter rightParameter) {
		this.rightParameter = rightParameter;
	}

	public boolean isDiff() {
		return isChangeRequired || isChangeDescription || !increased.isEmpty() || !missing.isEmpty()
				|| !changed.isEmpty();
	}

	public List<ElProperty> getIncreased() {
		return increased;
	}

	public void setIncreased(List<ElProperty> increased) {
		this.increased = increased;
	}

	public List<ElProperty> getMissing() {
		return missing;
	}

	public void setMissing(List<ElProperty> missing) {
		this.missing = missing;
	}

	public List<ElProperty> getChanged() {
		return changed;
	}

	public void setChanged(List<ElProperty> changed) {
		this.changed = changed;
	}
}
