package com.itorix.apiwiz.design.studio.businessimpl;

import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.design.studio.business.NotificationBusiness;
import com.itorix.apiwiz.design.studio.model.NotificationDetails;
import com.itorix.apiwiz.design.studio.model.NotificationType;
import com.itorix.apiwiz.identitymanagement.dao.BaseRepository;
import com.itorix.apiwiz.identitymanagement.dao.IdentityManagementDao;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.eclipse.jgit.transport.CredentialItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import com.itorix.apiwiz.design.studio.model.NotificationType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class NotificationBusinessImpl implements NotificationBusiness {
    @Autowired
    BaseRepository baseRepository;

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    IdentityManagementDao identityManagementDao;

    @Override
    public List<NotificationDetails> getNotifications() throws ItorixException {
        log.debug("getNotifications:");
        List<NotificationDetails> notificationDetails = baseRepository.findAll(NotificationDetails.class);
        return notificationDetails;
    }

    @Override
    public void instantiateNotification(String jsessionid, String name, String createdBy, String notifType, String notificationMessage) throws ItorixException {
        NotificationDetails notificationDetails = new NotificationDetails();
        notificationDetails.setNotification(notificationMessage .concat(name));
        notificationDetails.setUserId(Arrays.asList(createdBy));
        notificationDetails.setType(NotificationType.fromValue(notifType));
        createNotification(notificationDetails,jsessionid);
        log.debug("createNotification:{} {} " , jsessionid , notificationDetails);
        if (null == notificationDetails.getUserId()) {
            notificationDetails.setUserId(Arrays.asList(new ObjectId().toString()));
        }
        baseRepository.save(notificationDetails);
    }


    @Override
    public void createNotification(NotificationDetails notificationDetails, String jsessionid) throws ItorixException {
        log.debug("createNotification:{} {} " , jsessionid , notificationDetails);
        if (null == notificationDetails.getUserId()) {
            notificationDetails.setUserId(Arrays.asList(new ObjectId().toString()));
        }
        baseRepository.save(notificationDetails);
    }

    @Override
    public String deleteNotification(String id) throws ItorixException {
        if (id != null) {
            log.debug("deleteNotification :{} ", id);
            baseRepository.delete(id, NotificationDetails.class);
        }
        return "Deleted successfully";
    }

    @Override
    public NotificationDetails updateNotification(NotificationDetails notificationDetails, String id)
            throws ItorixException {
        log.debug("updateNotification :{} {} ",id, notificationDetails);
        NotificationDetails notificationDetails1 = baseRepository.findById(id, NotificationDetails.class);
        NotificationDetails updateNotifcations1=null;
        if (notificationDetails1.getId() != null) {
            notificationDetails1.setNotification(notificationDetails.getNotification());
            notificationDetails1.setIsRead(notificationDetails.getIsRead());
            notificationDetails1.setType(notificationDetails1.getType());
            notificationDetails1.setMts(System.currentTimeMillis());
            notificationDetails1.setUserId(notificationDetails.getUserId());
            updateNotifcations1 = baseRepository.save(notificationDetails1);
        }
        return updateNotifcations1;
    }



    @Override
    public List<NotificationDetails> getNotificationsForUser(String jsessionId, String userId, int offset, int pageSize) {

        log.debug("getNotificationForUser : {} {}", jsessionId, userId);
        List<NotificationDetails> list = baseRepository.find("userId", userId,
            NotificationDetails.class);
        list = list.stream().filter(notification -> !notification.getIsRead())
            .collect(Collectors.toList());
        log.debug("getNotificationForUser: {}", jsessionId);
        return list;
//        return trimList(list, offset, pageSize);

    }

    @Override
    public NotificationDetails findNotifications(NotificationDetails notificationDetails) {
        log.debug("findNotifications :{} {} " , notificationDetails.getInteractionid(), notificationDetails);
        return baseRepository.findOne("id", notificationDetails.getId(), NotificationDetails.class);
    }

    @Override
    public NotificationDetails findNotificationsById(String notificationId) {
        log.debug("findNotifications :{}" , notificationId);
        return baseRepository.findOne("id", notificationId, NotificationDetails.class);
    }

    @Override
    public Object updateUserNotifications(String userId) {
        Query query = Query.query(Criteria.where("userId").in(userId));
        Update update = new Update();
        update.set("isRead", true);
        return mongoTemplate.updateMulti(query,update,NotificationDetails.class);
    }

    private List<NotificationDetails> trimList(List<NotificationDetails> list, int offset, int pageSize) {
        List<NotificationDetails> notificationDetailsList = new ArrayList<>();
        int i = offset > 0 ? ((offset - 1) * pageSize) : 0;
        int end = i + pageSize;
        for (; i < list.size() && i < end; i++) {
            notificationDetailsList.add(list.get(i));
        }
        return notificationDetailsList;
    }

}
