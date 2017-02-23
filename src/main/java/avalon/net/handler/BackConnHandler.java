/*
 * Copyright (C) 2015 lizhuyang, Inc. All Rights Reserved.
 */
package avalon.net.handler;

import java.util.ArrayList;

import avalon.mysql.proto.ColCount;
import avalon.mysql.proto.Column;
import avalon.mysql.proto.EOF;
import avalon.mysql.proto.Flags;
import avalon.mysql.proto.Packet;
import avalon.mysql.proto.Row;
import avalon.net.context.ConContext;
import avalon.plugin.plugins.AvalonPluginBase;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Created by lizhuyang on 2015/1/28.
 */
public class BackConnHandler extends ChannelInboundHandlerAdapter {

    private FrontConnHandler frontConnHandler;

    private ConContext context = new ConContext();

    public ArrayList<AvalonPluginBase> plugins = new ArrayList<AvalonPluginBase>();

    public BackConnHandler(FrontConnHandler handler) {
        this.frontConnHandler = handler;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        context.mode = Flags.MODE_READ_HANDSHAKE;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        frontConnHandler.close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        context.buffer.clear();
        byte[] packet = (byte[]) msg;//Packet.read_packet(in, context);
        context.buffer.add(packet);
        context.packet = packet;
        switch (context.mode) {
            case Flags.MODE_READ_HANDSHAKE:
                context.mode = Flags.MODE_READ_AUTH_RESULT;
                for (AvalonPluginBase plugin : plugins) {
                    plugin.read_handshake(context);
                }
                break;
            case Flags.MODE_READ_AUTH_RESULT:
                context.mode = Flags.MODE_READ_QUERY_RESULT;
                if (Packet.getType(packet) != Flags.OK) {
                    System.out.println("Auth is not okay!");
                } else {
                    System.out.println("Auth is Okay!");
                }
                break;
            case Flags.MODE_READ_QUERY_RESULT:
                switch (Packet.getType(packet)) {
                    case Flags.OK:
                        System.out.println("OKay");
                        break;
                    case Flags.ERR:
                        System.out.println("False");
                        break;
                    case Flags.EOF:
                        if (context.subMode == Flags.MODE_COLUMN_NAMES) {
                            context.subMode = Flags.MODE_ROWS;
                            System.out.println("EOF to COLUMN NAMES");
                        } else {
                            if (EOF.loadFromPacket(packet).hasStatusFlag(Flags.SERVER_MORE_RESULTS_EXISTS)) {
                                context.subMode = Flags.MODE_ROWS;
                                System.out.println("EOF to Begin");
                            } else {
                                context.subMode = Flags.RESULT_SET_EOF;
                                System.out.println("EOF to end");
                            }
                        }
                        System.out.println("EOF");
                        break;
                    default:
                        switch (context.subMode) {
                            case Flags.MODE_COL_COUNT:
                                long colCount = ColCount.loadFromPacket(packet).colCount;
                                context.subMode = Flags.MODE_COLUMN_NAMES;
                                System.out.println("ColCount=" + colCount);
                                break;
                            case Flags.MODE_COLUMN_NAMES:
                                try {
                                    System.out.println(Column.loadFromPacket(packet).name);
                                } catch (Exception e) {
                                    System.out.println("Type=" + Packet.getType(packet) + "!!!!!!!!!!!!!!!!!!!!");
                                    e.printStackTrace();
                                }
                                break;
                            case Flags.MODE_ROWS:
                                Row row = Row.loadFromPacket(packet);
                                System.out.println(row.data);
                                break;
                            case Flags.RESULT_SET_EOF:
                                context.subMode = Flags.MODE_COL_COUNT;
                                break;
                            default:
                                break;
                        }
                }
            default:
                break;
        }

        writePacketToInBoundChannel(ctx, context.buffer);
    }

    private void writePacketToInBoundChannel(ChannelHandlerContext ctx, byte[] buffer) {

        ByteBuf bufferToFlash = ctx.alloc().buffer(Packet.getSize(buffer));
        frontConnHandler.getInboundChannel().writeAndFlush(bufferToFlash);

    }

    private void writePacketToInBoundChannel(ChannelHandlerContext ctx, ArrayList<byte[]> buffer) {
        ByteBuf bufferToFlash = null;
        for (byte[] buf : buffer) {
            bufferToFlash = ctx.alloc().buffer(buf.length);
            bufferToFlash.writeBytes(buf);
            frontConnHandler.getInboundChannel().writeAndFlush(bufferToFlash);
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        frontConnHandler.close();
    }

    public FrontConnHandler getFrontConnHandler() {
        return frontConnHandler;
    }

    public void setFrontConnHandler(FrontConnHandler frontConnHandler) {
        this.frontConnHandler = frontConnHandler;
    }

    public ConContext getContext() {
        return context;
    }

    public void setContext(ConContext context) {
        this.context = context;
    }

    public ArrayList<AvalonPluginBase> getPlugins() {
        return plugins;
    }

    public void setPlugins(ArrayList<AvalonPluginBase> plugins) {
        this.plugins = plugins;
    }
}
