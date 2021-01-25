package com.itorix.hyggee.mockserver.client.netty;

/**
 * This exception indicates there was an error while trying to communicate over a socket
 *
 *   
 */
public class SocketCommunicationException extends RuntimeException {
    public SocketCommunicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
