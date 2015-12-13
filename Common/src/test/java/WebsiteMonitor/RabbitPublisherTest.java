package WebsiteMonitor;

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
    public void TestDelayedPublishAndReceive()
    {
        bufferedMessages = 0;
        final long startTimeMs = System.currentTimeMillis();

        Task task = new Task();
        task.LastContentHash = 1;
        task.ListenerEmail = "foo@foo.com";
        task.WebsiteeUrl = "somewhere";
        try {
            RabbitPublisher rabbitPublisher = new RabbitPublisher("localhost", "TEST_QUEUE");
            rabbitPublisher.EnqueueTask(task, 0);
            ++bufferedMessages;
            Consumer consumer = new DefaultConsumer(rabbitPublisher.rabbitChannel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                        throws IOException {
                    --bufferedMessages;
                }
            };
            rabbitPublisher.rabbitChannel.basicConsume(
                    rabbitPublisher.RabbitQueueName(), true, consumer);
            while (bufferedMessages != 0)
            {
                if (System.currentTimeMillis() > startTimeMs + 5000)
                {
                    throw new TimeoutException("Message should have been consumed by now");
                }
                Thread.sleep(100);
            }
        } catch (Exception ex) {
            assert false;
        }
    }

    @Test
    public void TestDelayedPublishReceive()
    {

    }
}
