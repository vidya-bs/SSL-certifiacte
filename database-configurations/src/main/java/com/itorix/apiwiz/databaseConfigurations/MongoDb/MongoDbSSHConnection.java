package com.itorix.apiwiz.databaseConfigurations.MongoDb;

import com.itorix.apiwiz.common.model.databaseconfigs.mongodb.MongoDBConfiguration;
import com.itorix.apiwiz.common.model.databaseconfigs.mongodb.MongoDbSshAuthType;
import com.itorix.apiwiz.common.model.databaseconfigs.mongodb.MongoSSH;
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

import static com.itorix.apiwiz.databaseConfigurations.Utils.ConnectStaicFileds.*;


@Component
public class MongoDbSSHConnection {

  private static final Logger logger = LoggerFactory.getLogger(MongoDbSSHConnection.class);

  @Autowired
  private PEMImporter pemImporter;

  @Autowired
  private RSAEncryption rsaEncryption;

  public int prepareSshTunnel(MongoDBConfiguration mongoDBConfiguration) throws Exception {
    Session session;
    JSch jSch = new JSch();

    int localPort = SocketUtils.findAvailableTcpPort();

    MongoSSH mongoSsh = mongoDBConfiguration.getSsh();

    if(mongoSsh == null){
      logger.error("Invalid mongodb ssh connection - {}", mongoDBConfiguration.getSsh());
      throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1000"),"MongoDbSsh parameter is missing"), "DatabaseConfiguration-1000");
    }


    MongoDbSshAuthType authType = mongoSsh.getSshAuthType();

    if (authType == MongoDbSshAuthType.IDENTITYFILE ) {
      // key file byte data and passphrase
      String ssh = mongoSsh.getSshIdentityFile();
      if(ssh == null){
        throw new Exception();
      }
      if(ssh.startsWith(BEGIN_OPENSSH_PRIVATE_KEY)) {
        ssh = pemImporter.convertOpenSSHtoRSA(ssh);
      }
      String passphrase = mongoSsh.getSshPassphrase();
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

    int sshport = mongoSsh.getSshPort() == null ? 22 : Integer.parseInt(mongoSsh.getSshPort());
    session = jSch.getSession(mongoSsh.getSshUsername(), mongoSsh.getSshHostname(), sshport);

    if (authType == MongoDbSshAuthType.IDENTITYFILE ) {
      session.setConfig("PreferredAuthentications", "publickey");
    } else if (authType == MongoDbSshAuthType.PASSWORD) {
      session.setConfig("PreferredAuthentications", "password");
      String password = mongoSsh.getSshPassword();
      if(password != null) {
        try {
          password = rsaEncryption.decryptText(password);
        } catch (Exception ex){
          throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1002"),"Mysql! Unable to decrypt the password"), "DatabaseConfiguration-1002");
        }
        session.setPassword(password.getBytes());
      } else{
        throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1000"),"password in mongodbssh is missing"), "DatabaseConfiguration-1000");
      }
    }

    session.setConfig("StrictHostKeyChecking", "no");

    String host = mongoDBConfiguration.getHost();
    String[] hosts = host.split(":");
    if (hosts.length < 2) {
      throw new ItorixException(String.format(ErrorCodes.errorMessage.get("DatabaseConfiguration-1000"),"host port is missing"), "DatabaseConfiguration-1000");
    }
    session.connect(30000);
    int allocatedLocalPort = session.setPortForwardingL(localPort, hosts[0], Integer.parseInt(hosts[1]));
    return allocatedLocalPort;
  }
}