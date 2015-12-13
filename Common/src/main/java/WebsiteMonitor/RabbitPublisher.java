package WebsiteMonitor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import com.google.gson.Gson;
import com.rabbitmq.client.*;

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

    public void EnqueueTask(Task task, int taskDelayMs) throws IOException {
        Gson gson = new Gson();
        String taskAsJson = gson.toJson(task);

        byte[] messageBodyBytes = "delayed payload".getBytes();
        AMQP.BasicProperties.Builder props = new AMQP.BasicProperties.Builder();
        HashMap<String, Object> headers = new HashMap<String, Object>();
        headers.put("x-delay", taskDelayMs); // x-delay is in milliseconds
        props.headers(headers);

        rabbitChannel.basicPublish("my-exchange", "", props.build(), taskAsJson.getBytes());
    }

    public void ConsumeMessages(DefaultConsumer consumer) throws IOException {
        rabbitChannel.basicConsume(rabbitQueueName, true, consumer);
    }

    // The ConsumeMessages method blocks indefinitely. Aborting will interupt this.
    // This is needed from testing; we can block for 1 message, then abort.
    public void Abort() throws IOException {
        rabbitChannel.abort();
    }
}
