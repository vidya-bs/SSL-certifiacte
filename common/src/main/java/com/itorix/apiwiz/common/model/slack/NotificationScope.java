package com.itorix.apiwiz.common.model.slack;


public class NotificationScope {
    public enum Scopes {
        Design("Design"),Monitoring("Monitoring"),Build("Build"),Gateway("Gateway");
        public final String scope;

        Scopes(String scope) {
            this.scope = scope;
        }
    }
}