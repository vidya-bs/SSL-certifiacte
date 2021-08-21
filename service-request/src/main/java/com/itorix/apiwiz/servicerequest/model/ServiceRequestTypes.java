package com.itorix.apiwiz.servicerequest.model;

public enum ServiceRequestTypes {
	TARGETSERVER("TargetServer"), CACHE("Cache"), KVM("KVM"), PRODUCT("Product");

	private String value;

	ServiceRequestTypes(String value) {
		this.value = value;
	}

	public String getResponse() {
		return value;
	}

	public static boolean isServiceRequestTypeValid(String serviceRequestType) {

		for (ServiceRequestTypes serviceRequest : ServiceRequestTypes.values()) {
			if (serviceRequest.value.equals(serviceRequestType)) {
				return true;
			}
		}
		return false;
	}
}
