/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package avalon.net.handler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

/**
 * Created by lizhuyang on 2015/1/28.
 */
public class FrontConnFactory  extends ChannelInitializer<SocketChannel> {
    //这边的pipeline类似tomcat的管道
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline().addLast(new FrontConnHandler());
    }
}
