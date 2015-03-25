/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package avalon.net.handler;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.LinkedList;

import org.apache.log4j.Logger;

import avalon.mysql.proto.Flags;
import avalon.mysql.proto.Handshake;
import avalon.mysql.proto.Packet;
import avalon.net.context.ConContext;
import avalon.plugin.plugins.AvalonPluginBase;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
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

    public Logger logger = Logger.getLogger("FrontConnHandler");

    private LinkedList<Object> buffer = new LinkedList<Object>();
    private Channel inboundChannel;
    private Channel outboundChannel;
    private boolean outBoundChnnlReady = false;

    public ArrayList<AvalonPluginBase> plugins = new ArrayList<AvalonPluginBase>();

    public Handshake handshake = null;
    //Con连接属性
    public ConContext context = new ConContext();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //入口连接
        inboundChannel = ctx.channel();
        InetSocketAddress localAddress = (InetSocketAddress) inboundChannel.localAddress();
        Bootstrap b = new Bootstrap();
        b.group(inboundChannel.eventLoop()).channel(ctx.channel().getClass()).handler(new BackConnFactory(this))
                .option(ChannelOption.SO_REUSEADDR, true).option(ChannelOption.SO_KEEPALIVE, true);
        ChannelFuture f = b.connect("127.0.0.1", 3306);
        //出口连接
        outboundChannel = f.channel();
        context.mode = Flags.MODE_READ_AUTH;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!outBoundChnnlReady) {
            return;
        }
        byte[] packet = (byte[]) msg;
        context.packet = packet;
        switch (context.mode) {
            case Flags.MODE_READ_AUTH:
                this.logger.trace("MODE_READ_HANDSHAKE");
                context.mode = Flags.MODE_READ_QUERY;
                for (AvalonPluginBase plugin : plugins) {
                    plugin.read_auth(context);
                }
                break;
            case Flags.MODE_READ_QUERY:
                this.logger.trace("MODE_READ_AUTH_RESULT");
                for (AvalonPluginBase plugin : plugins) {
                    plugin.read_query(context);
                }
                packet = context.packet;
                break;

        }
        writePacketToOutBoundChannel(ctx, packet);
    }

    private void writePacketToOutBoundChannel(ChannelHandlerContext ctx, byte[] packet) {
        ByteBuf buffer = ctx.alloc().buffer(Packet.getSize(packet));
        buffer.writeBytes(packet);
        outboundChannel.writeAndFlush(buffer);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (outboundChannel != null) {
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

    public void outBoundChannelReady() {
        synchronized(buffer) {
            if (outboundChannel.isActive()) {
                for (Object ojb : buffer) {
                    outboundChannel.writeAndFlush(ojb);
                }
                buffer.clear();
            }

            outBoundChnnlReady = true;
        }
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

    public ArrayList<AvalonPluginBase> getPlugins() {
        return plugins;
    }

    public void setPlugins(ArrayList<AvalonPluginBase> plugins) {
        this.plugins = plugins;
    }
}
