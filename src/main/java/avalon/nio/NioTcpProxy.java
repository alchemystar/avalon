/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package avalon.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Executor;


/**
 * Created by lizhuyang on 2015/1/28.
 */
public class NioTcpProxy {

    private  int BLOCK = 4096;
    private  Selector selector;

    private Executor executor;
    public void start(){
        try {
            ServerSocketChannel acceptor = ServerSocketChannel.open();
            acceptor.socket().bind(new InetSocketAddress("127.0.0.1",8081));
            acceptor.configureBlocking(false);
            selector = Selector.open();
            acceptor.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("Start server");
            listen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void listen() throws IOException{
        for(;;){
            selector.select(1000L);
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();
                iterator.remove();
                handleKey(selectionKey);
            }
        }
    }

    private void handleKey(SelectionKey selectionKey) throws IOException {
         if(selectionKey.isAcceptable()){
            ServerSocketChannel server = (ServerSocketChannel)selectionKey.channel();
            SocketChannel client = server.accept();
            System.out.println("accept");
            NioConn conn = new NioConn(client);
            conn.start();
         }
    }

    public static void main(String args[]) throws IOException, InterruptedException {
        NioTcpProxy tcpProxy = new NioTcpProxy();
        tcpProxy.start();

    }

}
