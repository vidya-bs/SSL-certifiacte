package com.itorix.apiwiz.monitor.agent.executor;

import com.itorix.apiwiz.monitor.model.NotificationDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import static com.itorix.apiwiz.monitor.agent.util.MonitorAgentConstants.*;

@Component
public class EmailContentParser {

    @Autowired
    private Environment env;

    private Map<String, String> notificationEmailTemplates;

    @PostConstruct
    private void initNotificationTemplates() {
        notificationEmailTemplates = new HashMap<>();
        notificationEmailTemplates.put(SUMMARY_NOTIFICATION, MONITORING_TEST_SUBJECT);
        notificationEmailTemplates.put(LATENCY_THRESHOLD_BREACH, MONITORING_LATENCY_TEST_SUBJECT);
    }


    public String getEmailSubject (String notificationType, Object... contentToReplace) {
        String notificationEmailSubject = notificationEmailTemplates.get(notificationType);
        return MessageFormat.format(env.getProperty(notificationEmailSubject), contentToReplace);
    }

    public String getEmailBody (Object... contentToReplace) {
        return MessageFormat.format(env.getProperty(MONITORING_TEST_BODY), contentToReplace);
    }


    public String[] getRelevantEmailContent(NotificationDetails notificationDetail, Map<String, String> notificationData) {
        String status = notificationData.get(STATUS);
        String resource = notificationData.get(SCHEDULER_ID);
            String dailyUptime = String.valueOf(notificationDetail.getDailyUptime());
            String dailyLatency = String.valueOf(notificationDetail.getDailyLatency());
            String avgUptime = String.valueOf(notificationDetail.getAvgUptime());
            String avgLatency = String.valueOf(notificationDetail.getAvgLatency());

            return new String[]{notificationDetail.getWorkspaceName(),
                    notificationDetail.getCollectionname(), notificationDetail.getEnvironmentName(),
                    notificationDetail.getDate(), status, resource, dailyUptime,
                    dailyLatency, avgUptime,
                    avgLatency, notificationDetail.getSchedulerId()};
    }

}
