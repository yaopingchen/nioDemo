package selector;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class ClientChannelDemo {
    public static void main(String[] args) throws IOException {
        SocketChannel socketChannel=SocketChannel.open(new InetSocketAddress("127.0.0.1",3344));
        socketChannel.configureBlocking(false);
        ByteBuffer byteBuffer=ByteBuffer.allocate(1024);
        for(int i=0;i<1000;i++){
            byteBuffer.put(("aaa"+i).getBytes());
            //put完一定要翻转一下才能write
            byteBuffer.flip();
            socketChannel.write(byteBuffer);
            //每次发送完需要清空,否则容易缓冲区溢出
            byteBuffer.clear();
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
