package com.itorix.apiwiz.marketing.contactus.model;

import com.itorix.apiwiz.common.model.AbstractObject;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document("Notification.Events")
public class NotificationExecutionEvent extends AbstractObject {

    public enum STATUSES {

        SCHEDULED("Scheduled"), IN_PROGRESS("In Progress"), COMPLETED("Completed"), FAILED("Failed"), ERROR("Error");

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

    private RequestModel requestModel;
    private String status;
    private String errorCode;
    private String agent;
    private String tenant;
}
