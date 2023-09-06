package com.itorix.apiwiz.databaseConfigurations.Utils;

import com.itorix.apiwiz.common.model.databaseconfigs.mongodb.MongoDBConfiguration;
import com.itorix.apiwiz.common.model.databaseconfigs.mongodb.MongoSSH;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoIterable;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class MongoSOCKS5Connector {

    public MongoClient connect(MongoDBConfiguration mongoDBConfiguration){
        String mongoUrl = mongoDBConfiguration.getUrl();

        MongoSSH mongoSSH = mongoDBConfiguration.getSsh();
        String proxyHost = mongoSSH.getProxyHostname();
        int proxyPort = mongoSSH.getProxyTunnelPort() == null ? 1080 : Integer.parseInt(mongoSSH.getProxyTunnelPort());
        String proxyUsername = mongoSSH.getProxyUserName();
        String proxyPassword = mongoSSH.getProxypassword();

        InetSocketAddress socksAddress = new InetSocketAddress(proxyHost, proxyPort);
        Proxy proxy = new Proxy(Proxy.Type.SOCKS, socksAddress);
        if(proxyUsername != null && proxyPassword != null) {
            Authenticator.setDefault(new ProxyAuthenticator(proxyUsername, proxyPassword));
        }
        ProxySelector.setDefault(new FixedProxySelector(proxy));

        MongoClientURI mongoURI = new MongoClientURI(mongoUrl);
        MongoClient mongoClient = new MongoClient(mongoURI);

        return mongoClient;
    }

    private class FixedProxySelector extends ProxySelector {
        private final Proxy proxy;

        public FixedProxySelector(Proxy proxy) {
            this.proxy = proxy;
        }

        @Override
        public List<Proxy> select(URI uri) {
            return Collections.singletonList(proxy);
        }

        @Override
        public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
            // do nothing
        }
    }

    private class ProxyAuthenticator extends Authenticator {
        private final String username;
        private final String password;

        public ProxyAuthenticator(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(username, password.toCharArray());
        }
    }
}
