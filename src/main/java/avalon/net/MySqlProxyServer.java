/*
 * Copyright (C) 2015 lizhuyang, Inc. All Rights Reserved.
 */
package avalon.net;

import avalon.net.handler.FrontConnFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * Created by lizhuyang on 2015/1/28.
 */
public class MySqlProxyServer {
    private static final int port = 8080;

    public void start()  {
        EventLoopGroup bossGroup = new NioEventLoopGroup();// 通过nio方式来接收连接和处理连接
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            //这边的childHandler是用来管理accept的
            b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024).childHandler(new FrontConnFactory());
            ChannelFuture f = b.bind(port).sync();

            f.channel().closeFuture().sync();

        }catch(InterruptedException e){
            System.out.println("监听失败"+e);
        }
    }

    public static void main(String args[]){
        MySqlProxyServer proxyServer = new MySqlProxyServer();
        proxyServer.start();
    }
}
