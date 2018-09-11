package selector;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerChannelDemo {
    public static void main(String[] args) throws IOException {
        Selector selector=Selector.open();
        ServerSocketChannel serverSocketChannel=ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(3344));
        //非阻塞模式
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector,SelectionKey.OP_ACCEPT);
        ExecutorService threadPoolExecutor=Executors.newFixedThreadPool(10);
        while (true){
           int select= selector.select();
           if(select>0){
               Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
               while(iterator.hasNext()){
                   SelectionKey key = iterator.next();
                       if (key.isAcceptable()) {
                           ServerSocketChannel channel = (ServerSocketChannel) key.channel();
                           SocketChannel socketChannel = channel.accept();
                           System.out.println("accept address=" + socketChannel.getRemoteAddress());
                           //非阻塞模式
                           socketChannel.configureBlocking(false);
                           //新接入的socket,注册读写操作
                           socketChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                           //accept的用完要删除
                           iterator.remove();
                       } else if (key.isReadable()) {
                           threadPoolExecutor.execute(new Runnable() {
                               @Override
                               public void run() {
                                   SocketChannel socketChannel = (SocketChannel) key.channel();
                                   ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                                   try {
                                       int len;
                                       while ((len = socketChannel.read(byteBuffer)) > 0) {
                                           System.out.println(new String(byteBuffer.array(), 0, len));
                                           byteBuffer.flip();
                                       }
                                   } catch (IOException e) {
                                       e.printStackTrace();
                                       try {
                                           key.cancel();
                                           socketChannel.close();
                                       } catch (IOException e1) {
                                           e1.printStackTrace();
                                       }
                                   }
                               }
                           });
                       }

               }
           }
        }
    }


}
