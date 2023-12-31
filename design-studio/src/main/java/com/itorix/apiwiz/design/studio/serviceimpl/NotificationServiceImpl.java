package com.itorix.apiwiz.design.studio.serviceimpl;

import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.design.studio.business.NotificationBusiness;
import com.itorix.apiwiz.design.studio.model.NotificationDetails;
import com.itorix.apiwiz.design.studio.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin
@Slf4j
@RestController
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    NotificationBusiness notificationBusiness;


    @Override
    public ResponseEntity<List<NotificationDetails>> retrieveNotifications(String jsessionId, String interactionId, String userId, int offset, int pageSize) throws Exception {
        log.info("retrieveNotifications :{}", userId);
        List<NotificationDetails> response = notificationBusiness.getNotificationsForUser(jsessionId, userId, offset, pageSize);
        return new ResponseEntity<List<NotificationDetails>>(response, HttpStatus.OK);

    }


    @Override
    public ResponseEntity<?> createNotification(String interactionid, String jsessionid,
                                                NotificationDetails notificationDetails) throws Exception {

        log.info("createNotification :{} {}", notificationDetails.getInteractionid(), notificationDetails);
        if (notificationDetails == null) {
            throw new ItorixException(ErrorCodes.errorMessage.get("General-1000"), "General-1000");
        } else {
            notificationDetails.setInteractionid(interactionid);
            NotificationDetails no = notificationBusiness.findNotifications(notificationDetails);
            if (no != null) {
                throw new ItorixException(
                        String.format(ErrorCodes.errorMessage.get("General-1001"), notificationDetails.getType()),
                        "General-1001");
            }
            notificationBusiness.createNotification(notificationDetails, jsessionid);
            return new ResponseEntity<>(HttpStatus.CREATED);
        }

    }

    @Override
    public ResponseEntity<?> removeNotification(String jsessionId, String interactionId, String id) throws Exception {
        log.info("removeNotification :{}", id);
        notificationBusiness.deleteNotification(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<NotificationDetails> updateNotifications(String jsessionId, String interactionId, String id, NotificationDetails notificationDetails) throws Exception {
        log.info(" updateNotifications :{} {}", id, notificationDetails);
        if (notificationDetails == null) {
            throw new ItorixException(ErrorCodes.errorMessage.get("General-1000"), "General-1000");
        } else {
            notificationDetails.setInteractionid(interactionId);
            NotificationDetails no = notificationBusiness.findNotifications(notificationDetails);
            if (no != null) {
                throw new ItorixException(
                        String.format(ErrorCodes.errorMessage.get("General-1001"), notificationDetails.getType()),
                        "General-1001");
            }
            notificationBusiness.updateNotification(notificationDetails, id);
            return new ResponseEntity<NotificationDetails>(HttpStatus.OK);
        }
    }

    @Override
    public ResponseEntity<Object> retrieveNotificationsByNotificationId(
            String jsessionId, String interactionId, String notificationId, int offset, int pageSize)
            throws Exception {
        log.info("retrieveNotifications :{}", notificationId);
        return ResponseEntity.ok(notificationBusiness.findNotificationsById(notificationId));
    }

    @Override
    public ResponseEntity<?> updateUserNotifications(String jsessionId,
        String interactionId, String userId) throws Exception {
        return ResponseEntity.ok(notificationBusiness.updateUserNotifications(userId));
    }
}

