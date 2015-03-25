/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package avalon.net.handler;

import java.util.ArrayList;

import avalon.plugin.plugins.AvalonPluginBase;
import avalon.plugin.plugins.AvalonProxy;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

/**
 * Created by lizhuyang on 2015/1/28.
 */
public class BackConnFactory extends ChannelInitializer<SocketChannel> {

    private FrontConnHandler frontConnHandler;

    public BackConnFactory(FrontConnHandler handler) {
        super();
        this.frontConnHandler = handler;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ArrayList<AvalonPluginBase> baseList = new ArrayList<AvalonPluginBase>();
        BackConnHandler handler = new BackConnHandler(frontConnHandler);
        baseList.add(new AvalonProxy());
        handler.setPlugins(baseList);
        ch.pipeline().addLast(new PacketDecoder(),handler);

    }
}
