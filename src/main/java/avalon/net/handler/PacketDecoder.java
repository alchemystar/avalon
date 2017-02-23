/*
 * Copyright (C) 2015 alchemystar, Inc. All Rights Reserved.
 */
package avalon.net.handler;

import java.util.List;

import avalon.mysql.proto.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

/**
 * Created by lizhuyang on 2015/3/24.
 */
public class PacketDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        byte[] packet = Packet.read_packet(in);
        if (packet == null) {
            return;
        }
        out.add(packet);
    }
}
