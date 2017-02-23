/*
 * Copyright (C) 2015 alchemystar, Inc. All Rights Reserved.
 */
package avalon.nio;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by lizhuyang on 2015/1/28.
 */
public class NioConn extends Thread{
    SocketChannel serverChannel;
    SocketChannel clientChannel;
    Selector selector;

    /*缓冲区大小*/
    private static int BLOCK = 4096;
    /*接受数据缓冲区*/
 //   private  TcpProxyBuffer clientBuffer = new TcpProxyBuffer();

 //   private TcpProxyBuffer serverBuffer = new TcpProxyBuffer();

    ByteBuffer clientBuffer = ByteBuffer.allocate(1000);
    ByteBuffer serverBuffer = ByteBuffer.allocate(1000);

    public NioConn(SocketChannel channel) throws IOException {
        this.serverChannel = channel;
        serverChannel.configureBlocking(false);
        selector = Selector.open();
        clientChannel = SocketChannel.open();
        clientChannel.configureBlocking(false);
        clientChannel.connect(new InetSocketAddress("127.0.0.1", 3306));
        while(!clientChannel.finishConnect());
        clientChannel.register(selector, SelectionKey.OP_READ);
        serverChannel.register(selector, SelectionKey.OP_READ);
    }

    @Override
    public void run() {
        final Selector selector = this.selector;
        for(;;){
            try {
                if(selector.select(1000L) == 0){
                    System.out.print(".");
                    continue;
                }
                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = keys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey selectionKey = iterator.next();
                    iterator.remove();
                    handleKey(selectionKey);

                }
                selector.selectedKeys().clear();
            } catch (final ClosedChannelException exception){
                destroy();
                break;
            }catch(IOException e){
                destroy();
                e.printStackTrace();
                break;
            }

        }
    }


    private void handleKey(SelectionKey key) throws IOException {
        if (key.channel() == clientChannel) {
            if (key.isValid() && key.isReadable()) readFromClient();
            if (key.isValid() && key.isWritable()) writeToClient();
        }

        if (key.channel() == serverChannel) {
            if (key.isValid() && key.isReadable()) readFromServer();
            if (key.isValid() && key.isWritable()) writeToServer();
        }

    }

    public void readFromClient() throws IOException {
       int count = clientChannel.read(serverBuffer);
       if(count < 0){
           throw new ClosedChannelException();
       }
       System.out.println("readFromClient");
       serverBuffer.flip();
       serverChannel.register(selector,SelectionKey.OP_WRITE);
    }

    public void readFromServer() throws IOException {
        int count = serverChannel.read(clientBuffer);
        if(count < 0){
            throw new ClosedChannelException();
        }
        System.out.println("readFromServer");
        clientBuffer.flip();
        clientChannel.register(selector,SelectionKey.OP_WRITE);
    }

    public void writeToClient() throws IOException {
        clientChannel.write(clientBuffer);
        System.out.println("writeToClient");
        clientBuffer.clear();
        clientChannel.register(selector,SelectionKey.OP_READ);
    }

    public void writeToServer() throws IOException {
        serverChannel.write(serverBuffer);
        System.out.println("writeToServer");
        serverBuffer.clear();
        serverChannel.register(selector,SelectionKey.OP_READ);
    }

    @Override
    public void destroy() {
        closeQuietly(clientChannel);
        closeQuietly(serverChannel);
    }

    private static void closeQuietly(SocketChannel channel){
        if (channel != null) {
            try {
                channel.close();
            } catch (IOException exception) {
            }
        }


    }


}
