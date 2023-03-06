package com.itorix.apiwiz.design.studio.dto;

import com.itorix.apiwiz.design.studio.model.OPERATION;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ComplicanceScannerExecutorEntity {
    public static final String TABLE_NAME = "compliance_scanner_executor";

    public enum STATUSES {

        SCHEDULED("Scheduled"), IN_PROGRESS("In Progress"), COMPLETED("Completed"), FAILED("Failed"), CANCELLED("Cancelled");

        private final String value;

        STATUSES(String value) {
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
    private String complianceScannerExecutionId;
    private OPERATION operation;
    private String errorDescription;
    private String status;
    private String lockedBy;
    private String errorCode;

}
