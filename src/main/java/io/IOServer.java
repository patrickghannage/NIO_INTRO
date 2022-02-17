package io;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class IOServer implements Runnable {
    int port;
    int numberOfBytesToReceive;

    public IOServer(int serverSocketPort, int numberOfBytesToReceive) {
        this.port = serverSocketPort;
        this.numberOfBytesToReceive = numberOfBytesToReceive;
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            Socket clientSocket = serverSocket.accept();
            DataInputStream in = new DataInputStream(clientSocket.getInputStream());
            System.out.println("[Server] Starting to read message from socket");
            readMessage(in, numberOfBytesToReceive); // Blocks until packets arrives on socket
            System.out.println("[Server] Done reading message from socket");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads messages from socket input stream in a blocking fashion
     * @param in Socket input stream
     * @param numberOfBytesToReceive
     * @return Final message sent by client
     * @throws IOException
     */
    private String readMessage(DataInputStream in, int numberOfBytesToReceive) throws IOException {
        // Container in which bytes read will be stored
        byte[] messageByte = new byte[numberOfBytesToReceive];

        boolean readComplete = false;

        // Placeholder in which client message will be cumulated
        StringBuilder messageToBuildFromClient = new StringBuilder();

        while (!readComplete) {
            // Read blocks until data is available
            int bytesRead = in.read(messageByte);

            String partOfMessageRead = new String(messageByte, 0, bytesRead);
            messageToBuildFromClient.append(partOfMessageRead);
            if (messageToBuildFromClient.length() == numberOfBytesToReceive) {
                readComplete = true;
            }

            System.out.printf("[Server] Read %d bytes from socket %n", bytesRead);
        }
        return messageToBuildFromClient.toString();
    }
}
