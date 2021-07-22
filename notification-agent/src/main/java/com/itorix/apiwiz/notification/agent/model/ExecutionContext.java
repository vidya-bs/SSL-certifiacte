package com.itorix.apiwiz.notification.agent.model;

import com.itorix.apiwiz.notification.agent.db.NotificationExecutorEntity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExecutionContext {
    private NotificationExecutorEntity notificationExecutorEntity;
}
