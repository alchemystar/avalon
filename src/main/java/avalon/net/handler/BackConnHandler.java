/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package avalon.net.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Created by lizhuyang on 2015/1/28.
 */
public class BackConnHandler extends ChannelInboundHandlerAdapter {

    private FrontConnHandler frontConnHandler;

    public BackConnHandler(FrontConnHandler handler){
        this.frontConnHandler = handler;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        frontConnHandler.outBoundChannelReady();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        frontConnHandler.close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        frontConnHandler.getInboundChannel().writeAndFlush(msg);
    }
}
