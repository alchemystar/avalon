/*
 * Copyright (C) 2015 alchemystar, Inc. All Rights Reserved.
 */
package avalon.net.handler;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.LinkedList;

import org.apache.log4j.Logger;

import avalon.mysql.proto.Flags;
import avalon.mysql.proto.Handshake;
import avalon.mysql.proto.OK;
import avalon.mysql.proto.Packet;
import avalon.net.context.ConContext;
import avalon.plugin.plugins.AvalonPluginBase;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
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

    public static int sequenceId = 0;

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
        //出口连接
        //outboundChannel = f.channel();

        //Now we send HandShake包first
        writePacketToInBoundChannel(ctx, Handshake.init().toPacket());
        context.mode = Flags.MODE_READ_AUTH;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        context.clear_buffer();
        byte[] packet = (byte[]) msg;
        context.packet = packet;
        switch (context.mode) {
            case Flags.MODE_READ_AUTH:
                this.logger.trace("MODE_READ_HANDSHAKE");
                context.mode = Flags.MODE_READ_QUERY;

                //Now we get the auth packet
                //we send a fake auth
                ArrayList<byte[]> okPacket = new ArrayList<byte[]>();
                okPacket.add(OK.init(Packet.getSequenceId(context.packet) + 1).toPacket());
                context.buffer = okPacket;
                break;
            case Flags.MODE_READ_QUERY:
                this.logger.trace("MODE_READ_AUTH_RESULT");
                for (AvalonPluginBase plugin : plugins) {
                    if (!plugin.read_query(context)) {
                        break;
                    }
                }
                break;

        }
        writePacketToInBoundChannel(ctx, context.buffer);

    }

    private void writePacketToInBoundChannel(ChannelHandlerContext ctx, ArrayList<byte[]> buffer) {
        ByteBuf bufferToFlash = null;
        System.out.println("bufferSize=" + buffer.size());
        for (byte[] buf : buffer) {
            bufferToFlash = ctx.alloc().buffer(buf.length);
            bufferToFlash.writeBytes(buf);
            inboundChannel.writeAndFlush(bufferToFlash);
        }

    }

    private void writePacketToInBoundChannel(ChannelHandlerContext ctx, byte[] packet) {
        ByteBuf buffer = ctx.alloc().buffer(Packet.getSize(packet));
        buffer.writeBytes(packet);
        inboundChannel.writeAndFlush(buffer);

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
