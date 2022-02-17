package io;

import java.io.IOException;

public class IODemo {
    public static void main(String[] args) throws IOException {
        int port = 30501;
        int numberOfBytesToSend = 1000000;
        IOServer server = new IOServer(port, numberOfBytesToSend);
        Thread serverThread = new Thread(server);
        serverThread.start();
        IOClient client = new IOClient(port, numberOfBytesToSend);
        Thread clientThread = new Thread(client);
        clientThread.start();
    }
}
