package com.itorix.apiwiz.notification.agent.db;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationExecutorEntity {
    public static final String TABLE_NAME = "notification";

    public enum STATUSES {

        SCHEDULED("Scheduled"), IN_PROGRESS("In Progress"), COMPLETED("Completed"), ERROR("Error");

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
    private String type;
    private String content;
    private String status;

}
