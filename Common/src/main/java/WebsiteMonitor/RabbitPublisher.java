package WebsiteMonitor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import com.google.gson.Gson;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;

public class RabbitPublisher {

    private Channel rabbitChannel;
    private String rabbitQueueName;

    // The following method uses the RabbitMQ tutorials for code snippets.
    // https://www.rabbitmq.com/tutorials/tutorial-one-java.html
    // https://www.rabbitmq.com/blog/2015/04/16/scheduling-messages-with-rabbitmq/
    public RabbitPublisher(String hostName, String queueName)
            throws IOException, TimeoutException {

        // Open connection to rabbit
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(hostName);
        Connection connection = factory.newConnection();
        rabbitChannel = connection.createChannel();

        // Set up the delayed message exchange
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("x-delayed-type", "direct");
        // TODO configure the exchange name
        rabbitChannel.exchangeDeclare("my-exchange", "x-delayed-message", true, false, args);

        // Set up the queue on the exchange?
        rabbitChannel.queueDeclare(queueName, false, false, false, null);
        rabbitChannel.queueBind(queueName, "my-exchange", "");
        rabbitQueueName = queueName;
    }

    public void EnqueueTask(Task task)
    {
        Gson gson = new Gson();
        String taskAsJson = gson.toJson(task);

        byte[] messageBodyBytes = "delayed payload".getBytes();
        AMQP.BasicProperties.Builder props = new AMQP.BasicProperties.Builder();
        HashMap<String, Object> headers = new HashMap<String, Object>();
        headers.put("x-delay", 10000); // x-delay is in milliseconds
        props.headers(headers);

        try {
            rabbitChannel.basicPublish("my-exchange", "", props.build(), taskAsJson.getBytes());
        }
        catch (IOException ex)
        {
            System.out.print("Wut?");
        }
        System.out.println(" Message sent");
    }
}
