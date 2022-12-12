package com.itorix.apiwiz.common.model.slack;

public class notificationScope {
    public enum NotificationScope {
        DESIGN_STUDIO("design"),MONITORING("monitoring"),BUILD("build"),GATEWAY("gateway");
        public final String scope;

        private NotificationScope(String scope) {
            this.scope = scope;
        }
    }
}
