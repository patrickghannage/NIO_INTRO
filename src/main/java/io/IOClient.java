package io;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class IOClient implements Runnable {
    Socket socket;
    int numberOfBytesToSend;

    public IOClient(int serverSocketPort, int numberOfBytesToSend) throws IOException {
        socket = new Socket();
        this.numberOfBytesToSend = numberOfBytesToSend;
        socket.connect(new InetSocketAddress("localhost", serverSocketPort));
    }

    @Override
    public void run() {
        try {
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            byte[] message = new byte[numberOfBytesToSend];
            System.out.println("[Client] Preparing to write message to socket");
            dataOutputStream.write(message); // Blocks until all packets are written on socket
            System.out.println("[Client] Done writing message to socket");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
