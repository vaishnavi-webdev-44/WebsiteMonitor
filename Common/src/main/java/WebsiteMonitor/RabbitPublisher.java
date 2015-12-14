package WebsiteMonitor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import com.google.gson.Gson;
import com.rabbitmq.client.*;

// The following method uses the RabbitMQ tutorials for code snippets.
// https://www.rabbitmq.com/tutorials/tutorial-one-java.html
// https://www.rabbitmq.com/blog/2015/04/16/scheduling-messages-with-rabbitmq/
public class RabbitPublisher implements RabbitPublisherInterface {

    private Channel rabbitChannel;
    private String rabbitQueueName;
    private String rabbitExchangeName;

    public String RabbitQueueName() { return rabbitQueueName; }
    public Channel GetChannel() { return rabbitChannel; }

    public RabbitPublisher(String hostName, String queueName, String exchangeName)
            throws IOException, TimeoutException {

        // Open connection to rabbit
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(hostName);
        Connection connection = factory.newConnection();
        rabbitChannel = connection.createChannel();

        // Set up the delayed message exchange
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("x-delayed-type", "direct");
        rabbitChannel.exchangeDeclare(exchangeName, "x-delayed-message", true, false, args);

        // Set up the queue on the exchange?
        rabbitChannel.queueDeclare(queueName, false, false, false, null);
        rabbitChannel.queueBind(queueName, exchangeName, "");
        rabbitQueueName = queueName;
        rabbitExchangeName = exchangeName;
    }

    public void EnqueueTask(Task task, long taskDelayMs) throws IOException {
        Gson gson = new Gson();
        String taskAsJson = gson.toJson(task);

        byte[] messageBodyBytes = "delayed payload".getBytes();
        AMQP.BasicProperties.Builder props = new AMQP.BasicProperties.Builder();
        HashMap<String, Object> headers = new HashMap<String, Object>();
        headers.put("x-delay", taskDelayMs); // x-delay is in milliseconds
        props.headers(headers);

        rabbitChannel.basicPublish(rabbitExchangeName, "", props.build(), taskAsJson.getBytes());
    }
}
