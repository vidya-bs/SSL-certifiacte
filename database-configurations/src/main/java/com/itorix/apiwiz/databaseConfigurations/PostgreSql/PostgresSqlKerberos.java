package com.itorix.apiwiz.databaseConfigurations.PostgreSql;

import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.databaseConfigurations.Utils.KerberosConnector;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import static com.itorix.apiwiz.databaseConfigurations.Utils.ConnectStaicFileds.*;

@Component
public class PostgresSqlKerberos {

    private static final Logger logger = LoggerFactory.getLogger(PostgresSqlKerberos.class);


    public synchronized Connection createConnection(Properties properties, String url, String username, String kerberosRealm, String kerberosKdcServer) throws ItorixException {

        final File jaasConfFile;
        try
        {
            jaasConfFile = File.createTempFile("jaas.conf", null);
            final PrintStream bos = new PrintStream(new FileOutputStream(jaasConfFile));
            bos.print(String.format(
                    "pgjdbc {  com.sun.security.auth.module.Krb5LoginModule required  " +
                            "refreshKrb5Config=true  " +
                            "renewTGT=true " +
                            "useDefaultCcache=true " +
                            "useTicketCache=true " +
                            "ticketCache=\"/tmp/krb5cc_0\" " +
                            "debug=true ; };"
            ));
            bos.close();
            jaasConfFile.deleteOnExit();
        } catch (final IOException ex) {
            throw new IOError(ex);
        }

        properties.put("kerberosServerName", username);
        properties.setProperty("JAASConfigName", "pgjdbc");
        properties.put("jaasApplicationName", "pgjdbc");
        properties.put("gsslib", "gssapi");
        properties.put("gssEncMode", "require");

        System.setProperty(KRB5_REALM, kerberosRealm);
        System.setProperty(KRB5_KDC, kerberosKdcServer);
        System.setProperty(AUTH_LOGIN_CONFIG,jaasConfFile.getAbsolutePath());

        System.setProperty(JGSS_DEBUG,"true"); //To Debug
        System.setProperty(KRB5_DEBUG,"true"); //To Debug

        try {
            Connection connection = DriverManager.getConnection(url , properties);
            return connection;
        } catch (Exception ex) {
            logger.error("Exception Occured - ", ex);
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1002"),"Kerberos server!."), "DatabaseConfiguration-1002");
        }
    }
}
