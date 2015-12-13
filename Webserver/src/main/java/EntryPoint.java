import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;

public class EntryPoint {
    public static void main(String[] args) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        rabbitChannel = connection.createChannel();
        rabbitChannel.queueDeclare(QUEUE_NAME, false, false, false, null);

        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/watch_and_notify", new MyHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
    }
}
