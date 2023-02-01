package com.itorix.apiwiz.design.studio.model;

import com.itorix.apiwiz.common.model.AbstractObject;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document("API.Compliance.Scanner.ExecutionEvents")
public class ComplianceScannerExecutionEvent extends AbstractObject {
    public enum STATUSES {

        SCHEDULED("Scheduled"), IN_PROGRESS("In Progress"), COMPLETED("Completed"), FAILED("Failed"), CANCELLED("Cancelled");

        private final String value;

        STATUSES(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static String getStatus(ComplianceScannerExecutionEvent.STATUSES status) {

            String userRoles = null;
            for (ComplianceScannerExecutionEvent.STATUSES role : ComplianceScannerExecutionEvent.STATUSES.values()) {
                if (role.equals(status)) {
                    userRoles = role.getValue();
                }
            }
            return userRoles;
        }

    }
    private String swaggerId;
    private String operation;
    private String status;
    private String tennant;
    private long scheduledTime;
}
