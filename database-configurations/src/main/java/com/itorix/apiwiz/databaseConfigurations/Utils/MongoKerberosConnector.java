package com.itorix.apiwiz.databaseConfigurations.Utils;

import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.io.File;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.itorix.apiwiz.databaseConfigurations.Utils.ConnectStaicFileds.*;

@Component
public class MongoKerberosConnector {

    private static final Logger logger = LoggerFactory.getLogger(MongoKerberosConnector.class);

    public synchronized MongoClient kerberosAuth(final String hostUrl, final String realm, final String kdc, final String userPrincipal, final String password)
            throws LoginException, ItorixException {

        try {
            System.setProperty(KRB5_REALM, realm);
            System.setProperty(KRB5_KDC, kdc);

            logger.debug("Realm = {}, Kdc = {}", realm, kdc);
            try {
                File file = new File("/etc/krb5.conf");
                FileUtils.writeStringToFile(file, String.format(MONGODB_KERBEROS_KRB5_CONFIG, realm, realm, kdc));
            } catch (Exception e) {
                e.printStackTrace();
            }


            logger.debug("Userprincipal = {}", userPrincipal);

            // call back to set kerberos password
            CallbackHandler credentialsCallbackHandler = (cbs) -> {
                Callback[] callbacks = cbs;
                for (Callback cb : callbacks) {
                    if (NameCallback.class.isAssignableFrom(cb.getClass())) {
                        ((NameCallback) cb).setName(userPrincipal);
                    } else {
                        if (!PasswordCallback.class.isAssignableFrom(cb.getClass())) {
                            throw new UnsupportedCallbackException(cb, cb.getClass().getName());
                        }
                        ((PasswordCallback) cb).setPassword(password.toCharArray());
                    }
                }
            };
            ;

            // kerberos login config setup like jaas config
            Configuration loginConfig = new Configuration() {
                public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
                    Map<String, String> options = new HashMap();
                    options.put("useTicketCache", "true");
                    options.put("renewTGT", "false");
                    options.put("principal", userPrincipal);
                    options.put("ticketCache", "/tmp/krb5cc_1001");
                    options.put("debug", "true");
                    return new AppConfigurationEntry[]{new AppConfigurationEntry(
                            KRB5_LOGIN_MODULE, AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, options)};
                }
            };


            logger.debug("Login into Kerberos !");

            final Subject subject = new Subject();
            final LoginContext lc = new LoginContext("MONGODB", subject, credentialsCallbackHandler, loginConfig);
            lc.login();

            logger.debug("Login Successfull!");

            final Set<Principal> principalSet1 = subject.getPrincipals();
            if (principalSet1.size() != 1)
                throw new AssertionError("No or several principals: " + principalSet1);
            final Principal userPrincipal1 = principalSet1.iterator().next();


            logger.debug("Loging in as = {}", userPrincipal1.toString());

            class LogIn implements PrivilegedAction<MongoClient> {
                public MongoClient run() {
                    logger.info("Connecting to mongoDB as User Principal = {}", userPrincipal1.toString());
                    return createMongoClient(hostUrl);
                }
            }
            MongoClient mongoClient = Subject.doAsPrivileged(subject, new LogIn(), null);
            return mongoClient;
        } catch (Exception ex) {
            logger.error("Exception Occured - ", ex);
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1002"),"Kerberos server!."), "DatabaseConfiguration-1002");
        }
    }

    private MongoClient createMongoClient(String url) {
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(url)).build();
        return MongoClients.create(settings);
    }
}

