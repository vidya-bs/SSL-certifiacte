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
        notificationEmailTemplates.put("DAILY_SUMMARY_NOTIFICATION_SUB", "itorix.app.monitor.summary.report.email.subject");
        notificationEmailTemplates.put("DAILY_SUMMARY_NOTIFICATION_BODY", "itorix.app.monitor.summary.report.email.body");
        notificationEmailTemplates.put("LATENCY_THRESHOLD_BREACH_SUB", "itorix.app.monitor.test.report.email.subject");
        notificationEmailTemplates.put("LATENCY_THRESHOLD_BREACH_BODY", "itorix.app.monitor.test.report.email.body");
    }


    public String getEmailSubject (String notificationType, Object... contentToReplace) {
        String notificationEmailSubject = notificationEmailTemplates.get(notificationType+"_SUB");
        return MessageFormat.format(env.getProperty(notificationEmailSubject), contentToReplace);
    }

    public String getEmailBody (String notificationType, Object... contentToReplace) {
        String notificationEmailBody = notificationEmailTemplates.get(notificationType+"_BODY");
        return MessageFormat.format(env.getProperty(notificationEmailBody), contentToReplace);
    }


    public String[] getRelevantEmailContent(String notificationType, NotificationDetails notificationDetail, Map<String, String> notificationData) {
        String status = notificationData.get(STATUS);
        String resource = notificationData.get(SCHEDULER_ID);
        if(SUMMARY_NOTIFICATION.equals(notificationType)) {
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

        if(LATENCY_THRESHOLD_BREACH.equals(notificationType)) {
            String expectedLatency = notificationData.get(EXPECTED_LATENCY);
            String measuredLatency = notificationData.get(MEASURED_LATENCY);
            return new String[]{status, notificationDetail.getWorkspaceName(), notificationDetail.getDate(),
                    notificationDetail.getCollectionname(), notificationDetail.getEnvironmentName(),
                    status, expectedLatency, measuredLatency};
        }
        return new String[]{};
    }

}
