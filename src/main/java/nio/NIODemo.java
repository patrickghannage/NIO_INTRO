package nio;

import static java.lang.Thread.sleep;

public class NIODemo {
    public static void main(String[] args) throws InterruptedException {
        int serverSocketPort = 5454;
        int numberOfBytesToSend = 1000000;
        NIOServer server = new NIOServer(serverSocketPort, numberOfBytesToSend);
        Thread serverThread = new Thread(server);
        serverThread.start();
        sleep(1000);
        NIOClient client = new NIOClient(serverSocketPort, numberOfBytesToSend);
        Thread clientThread = new Thread(client);
        clientThread.start();
    }
}
