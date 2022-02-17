package nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class NIOServer implements Runnable{
    ServerSocketChannel serverSocketChannel;
    Selector selector;
    int serverSocketPort;
    int numberOfBytesToReceive;

    public NIOServer(int serverSocketPort, int numberOfBytesToReceive) {
        this.serverSocketPort = serverSocketPort;
        this.numberOfBytesToReceive = numberOfBytesToReceive;
    }

    @Override
    public void run() {
        try {
            serverSocketChannel = ServerSocketChannel.open();
            selector = Selector.open();
            serverSocketChannel.bind(new InetSocketAddress("localhost", serverSocketPort));
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(selector, serverSocketChannel.validOps());

            while(true) {
                System.out.println("[Server] Waiting for at least one channel to be ready for I/O operation");
                // Block until at least one of the registered channel has an i/o operation to perform
                selector.select();

                // Fetches the keys that are ready for processing
                Set<SelectionKey> selectionKeys = selector.selectedKeys();

                Iterator<SelectionKey> it = selectionKeys.iterator();

                // Trigger the selector event loop
                while (it.hasNext()) {
                    SelectionKey key = it.next();

                    // In netty, there are separate event loops for the acceptor and the workers
                    if (key.isAcceptable()) {
                        // Accept and register the connected client to the selector's event loop
                        SocketChannel clientSocketChannel = serverSocketChannel.accept();
                        clientSocketChannel.configureBlocking(false);
                        clientSocketChannel.register(selector, SelectionKey.OP_READ);
                        System.out.println("[Server] Connection Accepted: " + clientSocketChannel.getLocalAddress());
                    }

                    if (key.isReadable()) {
                        // Read in a non-blocking fashion
                        ByteBuffer byteBuffer = ByteBuffer.allocate(numberOfBytesToReceive);
                        SocketChannel clientSocketChannel = (SocketChannel) key.channel();
                        int bytesRead = clientSocketChannel.read(byteBuffer);
                        System.out.println("[Server] Number of bytes read: " + bytesRead);
                        String messagePart = new String(byteBuffer.array()).trim();

                        if (messagePart.contains("d")) {
                            System.out.println("[Server] Received full message. Closing communication.");
                            clientSocketChannel.close();
                        }
                    }
                }

                it.remove();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
