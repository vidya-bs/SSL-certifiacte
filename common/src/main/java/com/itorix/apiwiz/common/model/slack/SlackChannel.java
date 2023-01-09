package com.itorix.apiwiz.common.model.slack;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Set;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SlackChannel {
    private String channelName;
    private Set<notificationScope.NotificationScope> scopeSet;
}
