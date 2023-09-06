package com.itorix.apiwiz.databaseConfigurations.Utils;


import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.databaseConfigurations.Utils.KinitTools.KerberosKinit;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;

import static com.itorix.apiwiz.databaseConfigurations.Utils.ConnectStaicFileds.*;

@Component
public class KerberosConnector {

    private static final Logger logger = LoggerFactory.getLogger(KerberosConnector.class);


    //only one thread can access this method at a time
    public synchronized void CreateKerberosTicket(String username, String password, String relam, String kdcServer) throws ItorixException {
        try {
            String principal = username + "@" + relam;
            String defaultRelam = relam;
            try{
                File file = new File("/etc/krb5.conf");
                FileUtils.writeStringToFile(file,String.format(MYSQL_KERBEROS_KRB5_CONFIG, defaultRelam, defaultRelam, kdcServer));
            }  catch (Exception e) {
                e.printStackTrace();
            }
            //TODO use below instead of using above one and test it
//            System.setProperty("java.security.krb5.realm", "MYSERVER.LOCALHOST");
//            System.setProperty("java.security.krb5.kdc", "myserver.localhost");

            System.setProperty(KRB5_DEBUG,"true"); //enable for krb5logs
            System.setProperty(JGSS_DEBUG,"true");

            KerberosKinit.main(new String[]{principal, password});
        } catch (Exception e) {
            logger.error("Exception while creating Kerberos Ticket: " + e);
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1002"),"Kerberos server!."), "DatabaseConfiguration-1002");
        }
    }
}
