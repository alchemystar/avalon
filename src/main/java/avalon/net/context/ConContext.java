/*
 * Copyright (C) 2015 lizhuyang, Inc. All Rights Reserved.
 */
package avalon.net.context;

import java.util.ArrayList;

import avalon.mysql.proto.Flags;
import avalon.mysql.proto.HandshakeResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * Connection Context
 * Created by lizhuyang on 2015/3/24.
 */
public class ConContext {
    /**
     * 偏移量
     */
    public int offset = 0;

    /**
     * MySql Conn运行标识
     */
    public boolean running = true;

    /**
     * 期望获取的ResultSet类型
     */
    public int expectedResultSet = Flags.RS_OK;
    /**
     * HandShakeResponse
     */
    public HandshakeResponse authReply = null;
    /**
     * 当前实例的schema
     */
    public String schema = "";
    /**
     * 当前的query语句
     */
    public String query = "";
    /**
     * 状态标识
     */
    public long statusFlags = 0;
    public long sequenceId = 0;
    /**
     * 是否透传标识
     */
    public boolean bufferResultSet = true;
    public boolean packResultSet = true;
    /**
     * MySql 半双工协议状态
     */
    public int mode = Flags.MODE_INIT;

    public int subMode = Flags.MODE_COL_COUNT;
    public ChannelHandlerContext ctx;
    /**
     * 当前packet字节包
     */
    public byte[] packet;

    public ArrayList<byte[]> buffer = new ArrayList<byte[]>();

    public ByteBuf halfbuf ;

    public void clear_buffer() {
        this.offset = 0;
        this.buffer = new ArrayList<byte[]>();
    }

}
