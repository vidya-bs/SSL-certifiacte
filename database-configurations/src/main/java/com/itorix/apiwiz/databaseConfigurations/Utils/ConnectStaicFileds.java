package com.itorix.apiwiz.databaseConfigurations.Utils;

public class ConnectStaicFileds {

    public static String AUTH_LOGIN_CONFIG = "java.security.auth.login.config";
    public static String MYSQL_JASS_FILE_NAME = "mysql-jaas.conf";
    public static String MYSQL_JASS_FILE = "MySQLConnectorJ { \n com.sun.security.auth.module.Krb5LoginModule required \n useTicketCache=true; \n };";
    public static String MYSQL_KERBEROS_KRB5_CONFIG = "[libdefaults]\n" +
            "        default_realm = %s \n" +
            "        ignore_acceptor_hostname = true\n" +
            "        udp_preference_limit =1\n" +
            "[realms]\n" +
            "        %s = {\n" +
            "                kdc = %s\n" +
            "        }";

    public static String MONGODB_KERBEROS_KRB5_CONFIG = "[libdefaults]\n" +
            "        default_realm = %s \n" +
            "ignore_acceptor_hostname = true\n" +
            "[realms]\n" +
            "        %s = {\n" +
            "                kdc = %s\n" +
            "        }";
    public static String KRB5_DEBUG = "sun.security.krb5.debug";
    public static String JGSS_DEBUG = "sun.security.jgss.debug";
    public static String KRB5_REALM = "java.security.krb5.realm";
    public static String KRB5_KDC = "java.security.krb5.kdc";
    public static String KRB5_LOGIN_MODULE = "com.sun.security.auth.module.Krb5LoginModule";
    public static String BEGIN_PRIVATE_KEY = "-----BEGIN PRIVATE KEY-----";
    public static String END_PRIVATE_KEY = "-----END PRIVATE KEY-----";
    public static String BEGIN_RSA_PRIVATE_KEY = "-----BEGIN RSA PRIVATE KEY-----";
    public static String END_RSA_PRIVATE_KEY = "-----END RSA PRIVATE KEY-----";
    public static String BEGIN_ENCRYPTED_PRIVATE_KEY = "-----BEGIN ENCRYPTED PRIVATE KEY-----";
    public static String END_ENCRYPTED_PRIVATE_KEY = "-----END ENCRYPTED PRIVATE KEY-----";
    public static String BEGIN_OPENSSH_PRIVATE_KEY = "-----BEGIN OPENSSH PRIVATE KEY-----";
    public static String BEGIN_CERTIFICATE = "-----BEGIN CERTIFICATE-----";
    public static String END_CERTIFICATE =  "-----END CERTIFICATE-----";
    public static String CA_CERTIFICATE =  "ca-cert";
    public static String SERVER_CA =  "server-ca";
    public static String CLIENT_CERTIFICATE =  "client-cert";
    public static String CLIENT_KEY =  "client-key";
    public static String KEYSTORE_EXTENSION =  ".jks";
    public static final char[] DEFAULT_PASSWORD = new char[]{};

    public static String POSTGRES_SSLCERT = "sslcert";
    public static String POSTGRES_SSLKEY = "sslkey";
    public static String POSTGRES_SSLROOTCRT = "sslrootcert";


}
