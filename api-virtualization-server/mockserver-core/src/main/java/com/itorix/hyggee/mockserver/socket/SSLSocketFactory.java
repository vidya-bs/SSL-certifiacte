package com.itorix.hyggee.mockserver.socket;

import javax.net.ssl.SSLSocket;

import static com.itorix.hyggee.mockserver.socket.KeyStoreFactory.keyStoreFactory;

import java.io.IOException;
import java.net.Socket;

/**
 *   
 */
public class SSLSocketFactory {

    public static SSLSocketFactory sslSocketFactory() {
        return new SSLSocketFactory();
    }

    public synchronized SSLSocket wrapSocket(Socket socket) throws IOException {
        // ssl socket factory
        javax.net.ssl.SSLSocketFactory sslSocketFactory = keyStoreFactory().sslContext().getSocketFactory();

        // ssl socket
        SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket(socket, socket.getInetAddress().getHostAddress(), socket.getPort(), true);
        sslSocket.setUseClientMode(true);
        sslSocket.startHandshake();
        return sslSocket;
    }
}
