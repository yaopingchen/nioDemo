package file;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.Pipe;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;

public class FileTest {
    public static void main(String[] args) throws IOException {
        Path path=Paths.get("C:\\Users\\yaopchen\\Downloads\\jquery.fileupload.js");
        FileChannel fileChannel=FileChannel.open(path, EnumSet.of(StandardOpenOption.READ,StandardOpenOption.WRITE));
        transferTo(fileChannel);
//        filePipe(fileChannel);
//        pipe();
//        mapRead(fileChannel);
    }

    private static void transferTo(FileChannel fileChannel) throws IOException {
        Path targetPath=Paths.get("C:\\Users\\yaopchen\\Desktop\\jquery.fileupload.js");
        FileChannel target=FileChannel.open(targetPath, EnumSet.of(StandardOpenOption.CREATE,StandardOpenOption.WRITE));
        fileChannel.transferTo(0,fileChannel.size(),target);
    }

    private static void filePipe(FileChannel fileChannel) throws IOException {
        ByteBuffer byteBuffer=ByteBuffer.allocate(1024*8);
        Pipe pipe=Pipe.open();
        Pipe.SourceChannel sourceChannel=pipe.source();
        Pipe.SinkChannel sinkChannel=pipe.sink();
        new Runnable(){

            @Override
            public void run() {
                try{
                    while(fileChannel.read(byteBuffer)>0){
                        //从fileChannel中read完之后，position的值在limit处，写之前一定要翻转
                        byteBuffer.flip();
                        sinkChannel.write(byteBuffer);
                        //写完之后，position的值在limit处，马上要再读下一部分了，一定要翻转
                        byteBuffer.flip();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    try {
                        sinkChannel.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        }.run();
        new Runnable() {
            @Override
            public void run() {
                ByteBuffer source=ByteBuffer.allocate(1024);
                int len;
                try{
                    while((len=sourceChannel.read(source))>0){
                        System.out.println(new String(source.array(), 0, len));
                        source.flip();
                        Thread.sleep(100);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        sourceChannel.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.run();

    }

    private static void printBuffer(ByteBuffer buffer) {
        while(buffer.hasRemaining()) {
            System.out.print((char)buffer.get());
        }
    }

    private static void mapRead(FileChannel fileChannel) throws IOException {
        MappedByteBuffer buffer=fileChannel.map(FileChannel.MapMode.READ_ONLY,0L,fileChannel.size());
        printBuffer(buffer);
    }
    private static void pipe() throws IOException {
        Pipe pipe = Pipe.open();

        // 2. 将缓冲区数据写入到管道
        // 2.1 获取一个通道
        Pipe.SinkChannel sinkChannel = pipe.sink();
        // 2.2 定义缓冲区
        ByteBuffer buffer = ByteBuffer.allocate(48);
        buffer.put("发送数据".getBytes());
        buffer.flip(); // 切换数据模式
        // 2.3 将数据写入到管道
        sinkChannel.write(buffer);

        // 3. 从管道读取数据
        Pipe.SourceChannel sourceChannel = pipe.source();
        ByteBuffer source=ByteBuffer.allocate(48);
        int len = sourceChannel.read(source);
        System.out.println(new String(source.array(),0,len));

        // 4. 关闭管道
        sinkChannel.close();
        sourceChannel.close();

    }
}
