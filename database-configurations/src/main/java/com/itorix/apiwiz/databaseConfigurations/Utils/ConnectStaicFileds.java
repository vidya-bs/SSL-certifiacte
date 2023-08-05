package com.itorix.apiwiz.databaseConfigurations.Utils;

public class ConnectStaicFileds {

    public static final String AUTH_LOGIN_CONFIG = "java.security.auth.login.config";
    public static final String MYSQL_JASS_FILE_NAME = "mysql-jaas.conf";
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
    public static final String KRB5_DEBUG = "sun.security.krb5.debug";
    public static final String JGSS_DEBUG = "sun.security.jgss.debug";
    public static final String KRB5_REALM = "java.security.krb5.realm";
    public static final String KRB5_KDC = "java.security.krb5.kdc";
    public static final String KRB5_LOGIN_MODULE = "com.sun.security.auth.module.Krb5LoginModule";
    public static final String BEGIN_PRIVATE_KEY = "-----BEGIN PRIVATE KEY-----";
    public static final String END_PRIVATE_KEY = "-----END PRIVATE KEY-----";
    public static final String BEGIN_RSA_PRIVATE_KEY = "-----BEGIN RSA PRIVATE KEY-----";
    public static final String END_RSA_PRIVATE_KEY = "-----END RSA PRIVATE KEY-----";
    public static final String BEGIN_ENCRYPTED_PRIVATE_KEY = "-----BEGIN ENCRYPTED PRIVATE KEY-----";
    public static final String END_ENCRYPTED_PRIVATE_KEY = "-----END ENCRYPTED PRIVATE KEY-----";
    public static final String BEGIN_OPENSSH_PRIVATE_KEY = "-----BEGIN OPENSSH PRIVATE KEY-----";
    public static final String BEGIN_CERTIFICATE = "-----BEGIN CERTIFICATE-----";
    public static final String END_CERTIFICATE =  "-----END CERTIFICATE-----";
    public static final String CA_CERTIFICATE =  "ca-cert";
    public static final String SERVER_CA =  "server-ca";
    public static final String CLIENT_CRL =  "client-crl";
    public static final String CLIENT_CERTIFICATE =  "client-cert";
    public static final String CLIENT_KEY =  "client-key";
    public static final String KEYSTORE_EXTENSION =  ".jks";
    public static final char[] DEFAULT_PASSWORD = new char[]{};

    public static final String POSTGRES_SSLCERT = "sslcert";
    public static final String POSTGRES_SSLKEY = "sslkey";
    public static final String POSTGRES_SSLROOTCRT = "sslrootcert";
    public static final String POSTGRES_CLIENTCRL = "sslcrl";
    public static final String PEM_FILE_EXTENSION = ".pem";
    public static final String DER_FILE_EXTENSION = ".der";

}
