package nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

public class NIOClient implements Runnable {
    SocketChannel socketChannel;
    int serverSocketPort;
    Selector selector;
    ByteBuffer messageBytesToSend;

    public NIOClient(int serverSocketPort, int numberOfBytesToSend) {
        this.serverSocketPort = serverSocketPort;
        String message = constructMessageToSend(numberOfBytesToSend);
        messageBytesToSend = ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void run() {
        try {
            // Initialize selector
            this.selector = Selector.open();

            // Initialize client channel
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel.setOption(StandardSocketOptions.SO_SNDBUF, 90000);

            // Register channel to selector
            socketChannel.register(selector, SelectionKey.OP_CONNECT | SelectionKey.OP_WRITE);

            // Connect to server socket
            SocketAddress serverAddress = new InetSocketAddress("localhost", serverSocketPort);
            socketChannel.connect(serverAddress);

            // Trigger the selector event loop
            while (true) {
                // Block until at least one of the registered channel has an i/o operation to perform
                selector.select();

                // Fetches the keys that are ready for processing
                Set<SelectionKey> selectionKeys = selector.selectedKeys();

                Iterator<SelectionKey> it = selectionKeys.iterator();

                while (it.hasNext()) {
                    SelectionKey key = it.next();

                    // Start with connect first
                    if (key.isConnectable()) {
                        SocketChannel ch = (SocketChannel) key.channel();

                        if (ch.finishConnect()) {
                            // Done connecting! Not interested in that op anymore.
                            int ops = key.interestOps();
                            ops &= ~SelectionKey.OP_CONNECT;
                            key.interestOps(ops);
                            System.out.println("[Client] Successfully connected");

                            // Trigger write to server (In netty this is done via an enqueued task)
                            doWrite(messageBytesToSend, key);
                        }
                    }

                    // There is an incomplete write
                    if (key.isWritable()) {
                        System.out.println("[Client] Write operation started");
                        doWrite(messageBytesToSend, key);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Write in a non-blocking fashion the data from the buffer into the socket channel of the selection key.
     *
     * @param data data to write as a bytebuffer
     * @param key selection key for which the socket channel will have the write operation performed
     * @throws IOException
     */
    private void doWrite(ByteBuffer data, SelectionKey key) throws IOException {
        System.out.println("[Client] remaining bytes to write: " + data.remaining());
        int bytesWritten = socketChannel.write(data);
        System.out.println("[Client] Number of bytes written: " + bytesWritten);

        if (data.remaining() == 0) {
            key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
        }
    }

    /**
     * Constructs a message made of numberOfBytesToSend bytes all set to byte 'm'.
     * A byte 'd' is appended at the end to mark the end of the message
     *
     * @param numberOfBytesToSend Number of bytes the message should contain
     * @return The constructed message
     */
    private String constructMessageToSend(int numberOfBytesToSend) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < numberOfBytesToSend; i++) {
            sb.append("m");
        }
        sb.append("d");

        return sb.toString();
    }
}
