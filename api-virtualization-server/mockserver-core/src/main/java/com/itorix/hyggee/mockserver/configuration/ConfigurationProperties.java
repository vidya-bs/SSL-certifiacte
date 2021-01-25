package com.itorix.hyggee.mockserver.configuration;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.itorix.hyggee.mockserver.logging.MockServerLogger;
import com.itorix.hyggee.mockserver.socket.KeyStoreFactory;

import io.netty.util.NettyRuntime;
import io.netty.util.internal.SystemPropertyUtil;

import org.slf4j.event.Level;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.itorix.hyggee.mockserver.character.Character.NEW_LINE;
import static com.itorix.hyggee.mockserver.log.model.MessageLogEntry.LogMessageType.SERVER_CONFIGURATION;

/**
 *   
 */
public class ConfigurationProperties {

    public static final String DEFAULT_LOG_LEVEL = "INFO";
    private static final long DEFAULT_MAX_TIMEOUT = 20;
    private static final int DEFAULT_CONNECT_TIMEOUT = 20000;
    private static final int DEFAULT_MAX_EXPECTATIONS = 1000;
    private static final int DEFAULT_NIO_EVENT_LOOP_THREAD_COUNT = Math.max(1, SystemPropertyUtil.getInt("io.netty.eventLoopThreads", NettyRuntime.availableProcessors() * 5));
    private static final Properties PROPERTIES = readPropertyFile();

    private static final Set<String> ALL_SUBJECT_ALTERNATIVE_DOMAINS = Sets.newConcurrentHashSet();
    private static final Set<String> ALL_SUBJECT_ALTERNATIVE_IPS = Sets.newConcurrentHashSet();
    private static final AtomicBoolean REBUILD_KEY_STORE = new AtomicBoolean(false);

    private static final IntegerStringListParser INTEGER_STRING_LIST_PARSER = new IntegerStringListParser();

    static {
        addSslSubjectAlternativeNameDomains(readPropertyHierarchically("mockserver.sslSubjectAlternativeNameDomains", "localhost").split(","));
        addSslSubjectAlternativeNameIps(readPropertyHierarchically("mockserver.sslSubjectAlternativeNameIps", "127.0.0.1,0.0.0.0").split(","));
    }

    private static String propertyFile() {
        return System.getProperty("mockserver.propertyFile", "mockserver.properties");
    }

    public static boolean enableCORSForAPI() {
        return Boolean.parseBoolean(readPropertyHierarchically("mockserver.enableCORSForAPI", "" + true));
    }

    public static boolean enableCORSForAPIHasBeenSetExplicitly() {
        return (
            System.getProperty("mockserver.enableCORSForAPI") != null ||
                PROPERTIES.getProperty("mockserver.enableCORSForAPI") != null
        );
    }

    public static void enableCORSForAPI(boolean enableCORSForAPI) {
        System.setProperty("mockserver.enableCORSForAPI", "" + enableCORSForAPI);
    }

    public static boolean enableCORSForAllResponses() {
        return Boolean.parseBoolean(readPropertyHierarchically("mockserver.enableCORSForAllResponses", "" + false));
    }

    public static void enableCORSForAllResponses(boolean enableCORSForAPI) {
        System.setProperty("mockserver.enableCORSForAllResponses", "" + enableCORSForAPI);
    }

    public static int maxExpectations() {
        return readIntegerProperty("mockserver.maxExpectations", DEFAULT_MAX_EXPECTATIONS);
    }

    public static void maxExpectations(int count) {
        System.setProperty("mockserver.maxExpectations", "" + count);
    }

    public static int nioEventLoopThreadCount() {
        return readIntegerProperty("mockserver.nioEventLoopThreadCount", DEFAULT_NIO_EVENT_LOOP_THREAD_COUNT);
    }

    public static void nioEventLoopThreadCount(int count) {
        System.setProperty("mockserver.nioEventLoopThreadCount", "" + count);
    }

    public static long maxSocketTimeout() {
        return readLongProperty("mockserver.maxSocketTimeout", TimeUnit.SECONDS.toMillis(DEFAULT_MAX_TIMEOUT));
    }

    public static void maxSocketTimeout(long milliseconds) {
        System.setProperty("mockserver.maxSocketTimeout", "" + milliseconds);
    }

    public static int socketConnectionTimeout() {
        return readIntegerProperty("mockserver.socketConnectionTimeout", DEFAULT_CONNECT_TIMEOUT);
    }

    public static void socketConnectionTimeout(int milliseconds) {
        System.setProperty("mockserver.socketConnectionTimeout", "" + milliseconds);
    }

    public static String javaKeyStoreFilePath() {
        return readPropertyHierarchically("mockserver.javaKeyStoreFilePath", KeyStoreFactory.defaultKeyStoreFileName());
    }

    public static void javaKeyStoreFilePath(String keyStoreFilePath) {
        System.setProperty("mockserver.javaKeyStoreFilePath", keyStoreFilePath);
        rebuildKeyStore(true);
    }

    public static String javaKeyStorePassword() {
        return readPropertyHierarchically("mockserver.javaKeyStorePassword", KeyStoreFactory.KEY_STORE_PASSWORD);
    }

    public static void javaKeyStorePassword(String keyStorePassword) {
        System.setProperty("mockserver.javaKeyStorePassword", keyStorePassword);
        rebuildKeyStore(true);
    }

    public static String javaKeyStoreType() {
        return readPropertyHierarchically("mockserver.javaKeyStoreType", KeyStore.getDefaultType());
    }

    public static void javaKeyStoreType(String keyStoreType) {
        System.setProperty("mockserver.javaKeyStoreType", keyStoreType);
        rebuildKeyStore(true);
    }

    public static boolean deleteGeneratedKeyStoreOnExit() {
        return Boolean.parseBoolean(readPropertyHierarchically("mockserver.deleteGeneratedKeyStoreOnExit", "" + true));
    }

    public static void deleteGeneratedKeyStoreOnExit(boolean deleteGeneratedKeyStoreOnExit) {
        System.setProperty("mockserver.deleteGeneratedKeyStoreOnExit", "" + deleteGeneratedKeyStoreOnExit);
        rebuildKeyStore(true);
    }

    public static String sslCertificateDomainName() {
        return readPropertyHierarchically("mockserver.sslCertificateDomainName", KeyStoreFactory.CERTIFICATE_DOMAIN);
    }

    public static void sslCertificateDomainName(String domainName) {
        System.setProperty("mockserver.sslCertificateDomainName", domainName);
        rebuildKeyStore(true);
    }

    public static String[] sslSubjectAlternativeNameDomains() {
        return ALL_SUBJECT_ALTERNATIVE_DOMAINS.toArray(new String[ALL_SUBJECT_ALTERNATIVE_DOMAINS.size()]);
    }

    public static void addSslSubjectAlternativeNameDomains(String... newSubjectAlternativeNameDomains) {
        boolean subjectAlternativeDomainsModified = false;
        for (String subjectAlternativeDomain : newSubjectAlternativeNameDomains) {
            if (ALL_SUBJECT_ALTERNATIVE_DOMAINS.add(subjectAlternativeDomain.trim())) {
                subjectAlternativeDomainsModified = true;
            }
        }
        if (subjectAlternativeDomainsModified) {
            System.setProperty("mockserver.sslSubjectAlternativeNameDomains", Joiner.on(",").join(new TreeSet<>(ALL_SUBJECT_ALTERNATIVE_DOMAINS)));
            rebuildKeyStore(true);
        }
    }

    public static void clearSslSubjectAlternativeNameDomains() {
        ALL_SUBJECT_ALTERNATIVE_DOMAINS.clear();
    }

    public static boolean containsSslSubjectAlternativeName(String domainOrIp) {
        return ALL_SUBJECT_ALTERNATIVE_DOMAINS.contains(domainOrIp) || ALL_SUBJECT_ALTERNATIVE_IPS.contains(domainOrIp);
    }

    public static String[] sslSubjectAlternativeNameIps() {
        return ALL_SUBJECT_ALTERNATIVE_IPS.toArray(new String[ALL_SUBJECT_ALTERNATIVE_IPS.size()]);
    }

    public static void addSslSubjectAlternativeNameIps(String... newSubjectAlternativeNameIps) {
        boolean subjectAlternativeIpsModified = false;
        for (String subjectAlternativeDomain : newSubjectAlternativeNameIps) {
            if (ALL_SUBJECT_ALTERNATIVE_IPS.add(subjectAlternativeDomain.trim())) {
                subjectAlternativeIpsModified = true;
            }
        }
        if (subjectAlternativeIpsModified) {
            System.setProperty("mockserver.sslSubjectAlternativeNameIps", Joiner.on(",").join(new TreeSet<>(ALL_SUBJECT_ALTERNATIVE_IPS)));
            rebuildKeyStore(true);
        }
    }

    public static void clearSslSubjectAlternativeNameIps() {
        ALL_SUBJECT_ALTERNATIVE_IPS.clear();
        addSslSubjectAlternativeNameIps(readPropertyHierarchically("mockserver.sslSubjectAlternativeNameIps", "127.0.0.1,0.0.0.0").split(","));
    }

    public static boolean rebuildKeyStore() {
        return REBUILD_KEY_STORE.get();
    }

    public static void rebuildKeyStore(boolean rebuildKeyStore) {
        ConfigurationProperties.REBUILD_KEY_STORE.set(rebuildKeyStore);
    }

    public static List<Integer> mockServerPort() {
        return readIntegerListProperty("mockserver.mockServerPort", -1);
    }

    public static void mockServerPort(Integer... port) {
        System.setProperty("mockserver.mockServerPort", INTEGER_STRING_LIST_PARSER.toString(port));
    }

    public static Level logLevel() {
        return Level.valueOf(System.getProperty("mockserver.logLevel", DEFAULT_LOG_LEVEL));
    }

    /**
     * Override the debug WARN logging level
     *
     * @param level the log level, which can be TRACE, DEBUG, INFO, WARN, ERROR, OFF
     */
    public static void logLevel(String level) {
        if (!Arrays.asList("TRACE", "DEBUG", "INFO", "WARN", "ERROR", "OFF").contains(level)) {
            throw new IllegalArgumentException("log level \"" + level + "\" is not legal it must be one of \"TRACE\", \"DEBUG\", \"INFO\", \"WARN\", \"ERROR\", \"OFF\"");
        }
        System.setProperty("mockserver.logLevel", level);
    }

    public static boolean disableRequestAudit() {
        return Boolean.parseBoolean(readPropertyHierarchically("mockserver.disableRequestAudit", "" + false));
    }

    public static void disableRequestAudit(boolean disableRequestAudit) {
        System.setProperty("mockserver.disableRequestAudit", "" + disableRequestAudit);
    }

    public static boolean disableSystemOut() {
        return Boolean.parseBoolean(readPropertyHierarchically("mockserver.disableSystemOut", "" + false));
    }

    public static void disableSystemOut(boolean disableSystemOut) {
        System.setProperty("mockserver.disableSystemOut", "" + disableSystemOut);
    }

    public static InetSocketAddress httpProxy() {
        return readInetSocketAddressProperty("mockserver.httpProxy");
    }

    public static InetSocketAddress httpsProxy() {
        return readInetSocketAddressProperty("mockserver.httpsProxy");
    }

    public static InetSocketAddress socksProxy() {
        return readInetSocketAddressProperty("mockserver.socksProxy");
    }

    private static InetSocketAddress readInetSocketAddressProperty(String s) {
        InetSocketAddress inetSocketAddress = null;
        String proxy = readPropertyHierarchically(s, null);
        if (!Strings.isNullOrEmpty(proxy)) {
            String[] proxyParts = proxy.split(":");
            if (proxyParts.length > 1) {
                try {
                    inetSocketAddress = new InetSocketAddress(proxyParts[0], Integer.parseInt(proxyParts[1]));
                } catch (NumberFormatException nfe) {
                    MockServerLogger.MOCK_SERVER_LOGGER.error("NumberFormatException converting value \"" + proxyParts[1] + "\" into an integer", nfe);
                }
            }
        }
        return inetSocketAddress;
    }

    private static List<Integer> readIntegerListProperty(String key, Integer defaultValue) {
        try {
            return INTEGER_STRING_LIST_PARSER.toList(readPropertyHierarchically(key, "" + defaultValue));
        } catch (NumberFormatException nfe) {
            MockServerLogger.MOCK_SERVER_LOGGER.error("NumberFormatException converting " + key + " with value [" + readPropertyHierarchically(key, "" + defaultValue) + "]", nfe);
            return Collections.emptyList();
        }
    }

    private static Integer readIntegerProperty(String key, int defaultValue) {
        try {
            return Integer.parseInt(readPropertyHierarchically(key, "" + defaultValue));
        } catch (NumberFormatException nfe) {
            MockServerLogger.MOCK_SERVER_LOGGER.error("NumberFormatException converting " + key + " with value [" + readPropertyHierarchically(key, "" + defaultValue) + "]", nfe);
            return defaultValue;
        }
    }

    private static Long readLongProperty(String key, long defaultValue) {
        try {
            return Long.parseLong(readPropertyHierarchically(key, "" + defaultValue));
        } catch (NumberFormatException nfe) {
            MockServerLogger.MOCK_SERVER_LOGGER.error("NumberFormatException converting " + key + " with value [" + readPropertyHierarchically(key, "" + defaultValue) + "]", nfe);
            return defaultValue;
        }
    }

    private static Properties readPropertyFile() {

        Properties properties = new Properties();

        try (InputStream inputStream = ConfigurationProperties.class.getClassLoader().getResourceAsStream(propertyFile())) {
            if (inputStream != null) {
                try {
                    properties.load(inputStream);
                } catch (IOException e) {
                    e.printStackTrace();
                    if (MockServerLogger.MOCK_SERVER_LOGGER != null) {
                        MockServerLogger.MOCK_SERVER_LOGGER.error("Exception loading property file [" + propertyFile() + "]", e);
                    }
                }
            } else {
                if (MockServerLogger.MOCK_SERVER_LOGGER != null) {
                    MockServerLogger.MOCK_SERVER_LOGGER.debug(SERVER_CONFIGURATION, "Property file not found on classpath using path [" + propertyFile() + "]");
                }
                try {
                    properties.load(new FileInputStream(propertyFile()));
                } catch (FileNotFoundException e) {
                    if (MockServerLogger.MOCK_SERVER_LOGGER != null) {
                        MockServerLogger.MOCK_SERVER_LOGGER.debug(SERVER_CONFIGURATION, "Property file not found using path [" + propertyFile() + "]");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    if (MockServerLogger.MOCK_SERVER_LOGGER != null) {
                        MockServerLogger.MOCK_SERVER_LOGGER.error("Exception loading property file [" + propertyFile() + "]", e);
                    }
                }
            }
        } catch (IOException ioe) {
            // ignore
        }

        if (!properties.isEmpty()) {
            Enumeration<?> propertyNames = properties.propertyNames();

            StringBuilder propertiesLogDump = new StringBuilder();
            propertiesLogDump.append("Reading properties from property file [").append(propertyFile()).append("]:" + NEW_LINE);
            while (propertyNames.hasMoreElements()) {
                String propertyName = String.valueOf(propertyNames.nextElement());
                propertiesLogDump.append("\t").append(propertyName).append(" = ").append(properties.getProperty(propertyName)).append(NEW_LINE);
            }
            if (MockServerLogger.MOCK_SERVER_LOGGER != null) {
                MockServerLogger.MOCK_SERVER_LOGGER.debug(SERVER_CONFIGURATION, propertiesLogDump.toString());
            }
        }

        return properties;
    }

    private static String readPropertyHierarchically(String key, String defaultValue) {
        return System.getProperty(key, PROPERTIES != null ? PROPERTIES.getProperty(key, defaultValue) : defaultValue);
    }
}
