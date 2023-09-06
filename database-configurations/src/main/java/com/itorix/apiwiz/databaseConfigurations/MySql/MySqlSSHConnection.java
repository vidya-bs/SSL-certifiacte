package com.itorix.apiwiz.databaseConfigurations.MySql;

import com.itorix.apiwiz.common.model.databaseconfigs.ClientConnection;
import com.itorix.apiwiz.common.model.databaseconfigs.SshAuthType;
import com.itorix.apiwiz.common.model.databaseconfigs.mysql.MySQLConfiguration;
import com.itorix.apiwiz.common.model.databaseconfigs.mysql.MySqlSSH;
import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.util.encryption.RSAEncryption;
import com.itorix.apiwiz.databaseConfigurations.Utils.PEMImporter;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.SocketUtils;

import static com.itorix.apiwiz.databaseConfigurations.Utils.ConnectStaicFileds.BEGIN_OPENSSH_PRIVATE_KEY;


@Component
public class MySqlSSHConnection {

  private static final Logger logger = LoggerFactory.getLogger(MySqlSSHConnection.class);

  @Autowired
  PEMImporter pemImporter;

  @Autowired
  private RSAEncryption rsaEncryption;

  public void prepareSshTunnel(MySQLConfiguration sqlConfiguration, ClientConnection mysqlConnection) throws Exception {

    Session session;
    JSch jSch = new JSch();

    // random number
    int localPort = SocketUtils.findAvailableTcpPort();

    if(sqlConfiguration.getSsh() == null){
      logger.error("Invalid mysql ssh connection - {}", sqlConfiguration.getSsh());
      throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1000"),"MysqlSSH parameter is missing"), "DatabaseConfiguration-1000");
    }

    MySqlSSH mySqlSSH = sqlConfiguration.getSsh();

    SshAuthType authType = mySqlSSH.getSshAuthType();
    if(authType == SshAuthType.NONE){
      return;
    } else if (authType == SshAuthType.IDENTITYFILE ) {
      // key file byte data and passphrase
      String ssh = mySqlSSH.getSshKeyfile();
      if(ssh == null){
        throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1000"),"ssh identityFile is required!"), "DatabaseConfiguration-1000");
      }
      if(ssh.startsWith(BEGIN_OPENSSH_PRIVATE_KEY)) {
        ssh = pemImporter.convertOpenSSHtoRSA(ssh);
      }
      String passphrase = mySqlSSH.getSshPassPhrase();
      if(passphrase == null ){
        jSch.addIdentity(null, ssh.getBytes(), null, null);
      } else {
        try {
          passphrase = rsaEncryption.decryptText(passphrase);
        } catch (Exception ex){
          throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1002"),"Mysql! Unable to decrypt the password"), "DatabaseConfiguration-1002");
        }
        jSch.addIdentity(null, ssh.getBytes(), null, passphrase.getBytes());
      }
    }

    int sshport = mySqlSSH.getSshPort() == null ? 22 : Integer.parseInt(mySqlSSH.getSshPort());
    session = jSch.getSession(mySqlSSH.getSshUsername(), mySqlSSH.getSshHostname(), sshport);

    if (authType == SshAuthType.IDENTITYFILE ) {
      session.setConfig("PreferredAuthentications", "publickey");
    } else if (authType == SshAuthType.PASSWORD) {
      session.setConfig("PreferredAuthentications", "password");
      String password = mySqlSSH.getSshPassword();
      if(password != null) {
        try {
          password = rsaEncryption.decryptText(password);
        } catch (Exception ex){
          throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1002"),"Mysql! Unable to decrypt the password"), "DatabaseConfiguration-1002");
        }
        session.setPassword(password.getBytes());
      } else {
        throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1000"),"password in mysqlSsh is missing"), "DatabaseConfiguration-1000");
      }
    }
    session.setConfig("StrictHostKeyChecking", "no");

    session.connect(30000);
    int allocatedLocalPort = session.setPortForwardingL(localPort,
            sqlConfiguration.getMysqlHostname(), Integer.parseInt(sqlConfiguration.getMysqlPort()));
    mysqlConnection.setSession(session);
    mysqlConnection.setHost("127.0.0.1");
    mysqlConnection.setPort(allocatedLocalPort);
  }
}