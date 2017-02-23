/*
 * Copyright (C) 2015 alchemystar, Inc. All Rights Reserved.
 */
package avalon.net.handler;

import java.util.ArrayList;

import avalon.plugin.example.ResultSetExample;
import avalon.plugin.plugins.AvalonPluginBase;
import avalon.plugin.plugins.AvalonProxy;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

/**
 * Created by lizhuyang on 2015/1/28.
 */
public class FrontConnFactory  extends ChannelInitializer<SocketChannel> {
    //这边的pipeline类似tomcat的管道
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ArrayList<AvalonPluginBase> baseList = new ArrayList<AvalonPluginBase>();
        FrontConnHandler handler = new FrontConnHandler();
        baseList.add(new AvalonProxy());
        baseList.add(new ResultSetExample());
        handler.setPlugins(baseList);
        ch.pipeline().addLast(new PacketDecoder(),handler);
    }
}
