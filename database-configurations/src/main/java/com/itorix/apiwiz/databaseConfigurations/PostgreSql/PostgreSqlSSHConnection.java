package com.itorix.apiwiz.databaseConfigurations.PostgreSql;

import com.itorix.apiwiz.common.model.databaseconfigs.ClientConnection;
import com.itorix.apiwiz.common.model.databaseconfigs.SshAuthType;
import com.itorix.apiwiz.common.model.databaseconfigs.postgress.PostgreSQLConfiguration;
import com.itorix.apiwiz.common.model.databaseconfigs.postgress.PostgreSQLSSH;
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
public class PostgreSqlSSHConnection {

  private static final Logger logger = LoggerFactory.getLogger(PostgreSqlSSHConnection.class);

  @Autowired
  PEMImporter pemImporter;

  @Autowired
  private RSAEncryption rsaEncryption;


  public void prepareSshTunnel(PostgreSQLConfiguration postgreSQLConfiguration, ClientConnection postgresConection) throws Exception {

    Session session;
    JSch jSch = new JSch();

    int localPort = SocketUtils.findAvailableTcpPort();

    PostgreSQLSSH postgreSQLSsh = postgreSQLConfiguration.getSsh();

    if(postgreSQLSsh == null){
      logger.error("Invalid postgresql ssh connection - {}", postgreSQLConfiguration.getSsh());
      throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1000"),"MongoDbSsh parameter is missing"), "DatabaseConfiguration-1000");
    }

    SshAuthType authType = postgreSQLSsh.getSshAuthenticationType();

    if (authType == SshAuthType.IDENTITYFILE ) {
      // key file byte data and passphrase
      String ssh = postgreSQLConfiguration.getSsh().getSshIdentityfile();
      if(ssh == null){
        throw new Exception();
      }
      if(ssh.startsWith(BEGIN_OPENSSH_PRIVATE_KEY)) {
        ssh = pemImporter.convertOpenSSHtoRSA(ssh);
      }

      String passphrase = postgreSQLSsh.getSshPassphrase();
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

    int sshport = postgreSQLSsh.getSshTunnelport() == null ? 22 : Integer.parseInt(postgreSQLSsh.getSshTunnelport());
    session = jSch.getSession(postgreSQLSsh.getSshUsername(), postgreSQLSsh.getSshTunnelhost(), sshport);

    if (authType == SshAuthType.IDENTITYFILE ) {
      session.setConfig("PreferredAuthentications", "publickey");
    } else if (authType == SshAuthType.PASSWORD) {
      session.setConfig("PreferredAuthentications", "password");
      String password = postgreSQLSsh.getSshPassword();
      if(password != null) {
        try {
          password = rsaEncryption.decryptText(password);
        } catch (Exception ex){
          throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1002"),"Mysql! Unable to decrypt the password"), "DatabaseConfiguration-1002");
        }
        session.setPassword(password.getBytes());
      } else {
        throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1000"),"password in postgreSql is missing"), "DatabaseConfiguration-1000");
      }
    }

    session.setConfig("StrictHostKeyChecking", "no");

    session.connect(30000);
    int allocatedLocalPort = session.setPortForwardingL(localPort,
            postgreSQLConfiguration.getPostgresqlHostname(), Integer.parseInt(postgreSQLConfiguration.getPostgresqlPort()));
    postgresConection.setSession(session);
    postgresConection.setPort(allocatedLocalPort);
    postgresConection.setHost("127.0.0.1");
  }
}