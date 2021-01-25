package com.itorix.hyggee.mockserver.client.netty.codec;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.FullHttpResponse;

import java.util.List;

import com.itorix.hyggee.mockserver.client.netty.codec.mappers.FullHttpResponseToMockServerResponse;

/**
 *   
 */
public class MockServerResponseDecoder extends MessageToMessageDecoder<FullHttpResponse> {

    @Override
    protected void decode(ChannelHandlerContext ctx, FullHttpResponse fullHttpResponse, List<Object> out) {
        out.add(new FullHttpResponseToMockServerResponse().mapMockServerResponseToFullHttpResponse(fullHttpResponse));
    }

}
