package com.itorix.apiwiz.design.studio.model;

import java.util.List;

public enum Clients {
	android("android"), cwiki("cwiki"), dynamic_html("dynamic-html"), groovy("groovy"), html("html"), html2(
			"html2"), java("java"), javascript("javascript"), ruby("ruby"), scala("scala"), typescript_angularjs(
					"typescript-angularjs"), typescript_jquery("typescript-jquery"), typescript_node("typescript-node");

	private String value;

	private Clients(String value) {
		this.value = value;
	}

	public String getClient() {
		return value;
	}

	public void setClient(String value) {
		this.value = value;
	}

	public List<String> getValues() {
		for (Clients value : Clients.values()) {
		}

		return null;
	}

	public static String getClients(Clients clients) {
		String client = null;
		for (Clients value : Clients.values()) {
			if (value.equals(clients)) {
				client = value.getClient();
			}
		}
		return client;
	}
}
