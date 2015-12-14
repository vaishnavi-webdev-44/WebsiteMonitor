package WebsiteMonitor;

import com.google.gson.Gson;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RabbitPublisherTest {

    private static int bufferedMessages = 0;

    @Test
    public void TestDelayedPublishAndReceive() throws IOException, TimeoutException, InterruptedException {
        bufferedMessages = 0;
        final long startTimeMs = System.currentTimeMillis();
        final long delayMs = 100;

        Task task = new Task();
        task.LastContentHash = 1;
        task.ListenerEmail = "foo@foo.com";
        task.WebsiteUrl = "somewhere";

        RabbitPublisher rabbitPublisher = new RabbitPublisher("localhost", "TEST_QUEUE", "TEST_EXCHANGE");
        rabbitPublisher.EnqueueTask(task, delayMs);
        ++bufferedMessages;
        Consumer consumer = new DefaultConsumer(rabbitPublisher.GetChannel()) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                    throws IOException {
                // Ensure the message was properly delayed
                assert System.currentTimeMillis() >= startTimeMs + delayMs;
                String message = new String(body, "UTF-8");
                Gson gson = new Gson();
                Task receivedTask = gson.fromJson(message, Task.class);
                assert receivedTask.LastContentHash == 1;
                --bufferedMessages;
            }
        };
        rabbitPublisher.GetChannel().basicConsume(
                rabbitPublisher.RabbitQueueName(), true, consumer);
        while (bufferedMessages != 0)
        {
            if (System.currentTimeMillis() > startTimeMs + 5000)
            {
                throw new TimeoutException("Message should have been consumed by now");
            }
            Thread.sleep(10);
        }
    }
}
