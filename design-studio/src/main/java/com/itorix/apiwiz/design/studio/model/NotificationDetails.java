package com.itorix.apiwiz.design.studio.model;

import com.itorix.apiwiz.common.model.AbstractObject;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "Apiwiz.Notification.Details")
public class NotificationDetails extends AbstractObject {

    private String notification;
    private NotificationType type;
    private List<String> userId;
    private Boolean isRead;

}
