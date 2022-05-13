package com.itorix.apiwiz.analytics.businessImpl;

import com.itorix.apiwiz.analytics.model.MetricForAdminUser;
import com.itorix.apiwiz.analytics.model.UserRoleMetrics;
import com.itorix.apiwiz.common.model.SwaggerTeam;
import com.itorix.apiwiz.identitymanagement.dao.BaseRepository;
import com.itorix.apiwiz.identitymanagement.model.TenantContext;
import com.itorix.apiwiz.identitymanagement.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserRoleMetricsImpl {

    @Qualifier("masterMongoTemplate")
    @Autowired
    private MongoTemplate masterMongoTemplate;

    @Autowired
    private BaseRepository baseRepository;

    public UserRoleMetrics createUserRoleMetrics() {
        UserRoleMetrics userRoleMetrics = new UserRoleMetrics();

        MetricForAdminUser metricForAdminUser = generateMetricsForAdminUser();
        userRoleMetrics.setMetricForAdminUser(metricForAdminUser);
        return userRoleMetrics;
    }




    private MetricForAdminUser generateMetricsForAdminUser() {
        MetricForAdminUser metricForAdminUser = new MetricForAdminUser();

        generateUserMetrics(metricForAdminUser);

        generateTeamMetrics(metricForAdminUser);


        return metricForAdminUser;
    }

    private void generateUserMetrics(MetricForAdminUser metricForAdminUser) {
        String currentTenant = TenantContext.getCurrentTenant();
        List<User> all = masterMongoTemplate.findAll(User.class);

        Set<User> usersFilterByWS = all.stream().filter(u -> u.containsWorkspace(currentTenant)).collect(Collectors.toSet());
        metricForAdminUser.setNumberOfUsers(usersFilterByWS.stream().count());

        long numberOfLockedUsers = usersFilterByWS.stream().filter(u -> u.getUserWorkspace(currentTenant).getActive()).count();
        metricForAdminUser.setNumberOfLockedUsers(numberOfLockedUsers);


        long unixEpoch = LocalDateTime.now().minusDays(5).toInstant(ZoneOffset.UTC).toEpochMilli();

        long numberOfNewUsersCreated = usersFilterByWS.stream().filter(u -> u.getCts() >= unixEpoch).count();

        metricForAdminUser.setNumberOfNewUsers(numberOfNewUsersCreated);
    }

    private void generateTeamMetrics(MetricForAdminUser metricForAdminUser) {
        long unixEpoch = LocalDateTime.now().minusDays(5).toInstant(ZoneOffset.UTC).toEpochMilli();
        List<SwaggerTeam> listOfTeams = baseRepository.findAll(SwaggerTeam.class);
        int totalTeams = listOfTeams.size();
        metricForAdminUser.setTotalTeams(totalTeams);

        long numberOfNewTeams = listOfTeams.stream().filter(t -> t.getCts() >= unixEpoch).count();
        metricForAdminUser.setNumberOfNewTeams(numberOfNewTeams);
    }
}
