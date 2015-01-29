/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package avalon.net.handler;

import java.net.InetSocketAddress;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOption;

/**
 * Created by lizhuyang on 2015/1/28.
 */
public class FrontConnHandler extends ChannelInboundHandlerAdapter {

    private Channel inboundChannel;
    private Channel outboundChannel;
    private boolean outBoundChnnlReady =false;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //入口连接
        inboundChannel = ctx.channel();
        InetSocketAddress localAddress = (InetSocketAddress)inboundChannel.localAddress();
        int port = localAddress.getPort();
        Bootstrap b = new Bootstrap();
        b.group(inboundChannel.eventLoop())
                .channel(ctx.channel().getClass())
                .handler(new BackConnFactory(this))
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.SO_KEEPALIVE,true);
        ChannelFuture f = b.connect("127.0.0.1",3306);
        //出口连接
        outboundChannel = f.channel();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(outBoundChnnlReady){
            outboundChannel.writeAndFlush(msg);
            return ;
        }
        System.out.println("we lost this information");

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
       if(outboundChannel != null){
           closeOnFlush(outboundChannel);
       }
    }

    public void close() {
        closeOnFlush(inboundChannel);
        closeOnFlush(outboundChannel);
    }

    public void closeOnFlush(Channel ch) {
        if (ch.isActive()) {
            ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }

    public void outBoundChannelReady(){
        outBoundChnnlReady = true;
    }

    public Channel getInboundChannel() {
        return inboundChannel;
    }

    public void setInboundChannel(Channel inboundChannel) {
        this.inboundChannel = inboundChannel;
    }

    public Channel getOutboundChannel() {
        return outboundChannel;
    }

    public void setOutboundChannel(Channel outboundChannel) {
        this.outboundChannel = outboundChannel;
    }
}
