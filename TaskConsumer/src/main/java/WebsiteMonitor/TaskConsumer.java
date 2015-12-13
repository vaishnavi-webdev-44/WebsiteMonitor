package WebsiteMonitor;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;


public class TaskConsumer {

    private final static String QUEUE_NAME = "TEST_QUEUE";

    public static void main(String[] argv)
            throws java.io.IOException, java.lang.InterruptedException, TimeoutException {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
        // ... ? Suggested pattern for sleeping while consuming messages?

        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                    throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println(" [x] Received '" + message + "'");
            }
        };
        channel.basicConsume(QUEUE_NAME, true, consumer);
    }

    // My inputs are:
    // Hash of last webpage content
    // Email address of listener to inform
    // Potentially a TTL?
    public void ProcessTask(String url, int lastHash, String listenerEmail) {
//        String content = null;
//        try {
//            content = FetchContent(url);
//        } catch (IOException ex) {
//            // Mailer.SendMail(listenerEmail, "I'm sorry but it's dead Jim.");
//        }
//
//        // Set the hashes equal if we had an intermittent error; this makes it a no-op.
//        // We don't return because we need to reschedule.
//        int newHash = lastHash;
//        if (content != null) {
//            newHash = content.hashCode();
//        }
//
//        // lastHash = 0 => we have never queried before, we don't know the content...
//        // although...
//        // in the http service we could query immediately, to ensure the website
//        // exists, is reachable, and to get a starting hash. Ok, I like that.
//        if (newHash != lastHash && lastHash != 0) {
//            // We've detected a change
//        }
//
//        ScheduleTask(url, newHash, listenerEmail);
    }

    private void ScheduleTask(String url, int lastHash, String listenerEmail)
    {

    }

}
