package com.itorix.hyggee.mockserver.client.netty;

/**
 * This exception indicates there was an error while trying to communicate over a socket
 *
 *   
 */
public class SocketConnectionException extends RuntimeException {
    public SocketConnectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public SocketConnectionException(String message) {
        super(message);
    }
}
