package com.itorix.apiwiz.design.studio.business;

import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.design.studio.model.NotificationDetails;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface NotificationBusiness {

    public List<NotificationDetails> getNotifications() throws ItorixException;

    public void createNotification(NotificationDetails notificationDetails, String jsessionid) throws ItorixException;

    public String deleteNotification(String id) throws ItorixException;

    public NotificationDetails updateNotification(NotificationDetails notificationDetails, String id)
            throws ItorixException;

    public List<NotificationDetails> getNotificationsForUser(String jsessionId, String userId, int offset, int pageSize);

    public NotificationDetails findNotifications(NotificationDetails notificationDetails);

    public NotificationDetails findNotificationsById(String notificationId);


}
