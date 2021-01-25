package com.itorix.hyggee.mockserver.server.netty.codec;

import com.itorix.hyggee.mockserver.logging.MockServerLogger;

import io.netty.channel.CombinedChannelDuplexHandler;

/**
 *   
 */
public class MockServerServerCodec extends CombinedChannelDuplexHandler<MockServerRequestDecoder, MockServerResponseEncoder> {
    public MockServerServerCodec(MockServerLogger mockServerLogger, boolean isSecure) {
        init(new MockServerRequestDecoder(mockServerLogger, isSecure), new MockServerResponseEncoder());
    }
}
