package com.itorix.apiwiz.monitor.agent.db;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MonitorAgentExecutorEntity {
	public static final String TABLE_NAME = "monitor_executor";

	public enum STATUSES {

		SCHEDULED("Scheduled"), IN_PROGRESS("In Progress") , COMPLETED("Completed"),CANCELLED("Cancelled");
		private String value;

		private STATUSES(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}

		public static String getStatus(STATUSES status) {

			String userRoles = null;
			for (STATUSES role : STATUSES.values()) {
				if (role.equals(status)) {
					userRoles = role.getValue();
				}
			}
			return userRoles;
		}

	}

	private Long id;
	private String tenant;
	private String schedulerId;
	private String collectionId;
	private String status;
	private String errorDescription;

}
