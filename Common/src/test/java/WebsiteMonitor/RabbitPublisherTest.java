package WebsiteMonitor;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RabbitPublisherTest {

    private RabbitPublisher rabbitPublisher;

    @Before
    public void Setup() throws IOException, TimeoutException {
        rabbitPublisher = new RabbitPublisher("localhost", "TEST_QUEUE");
    }

    @Test
    public void TestPublishAndReceive()
    {
        Task task = new Task();
        task.LastContentHash = 1;
        task.ListenerEmail = "foo@foo.com";
        task.WebsiteeUrl = "somewhere";
        try
        {
            rabbitPublisher.EnqueueTask(task, 0);
            Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                        throws IOException {
                    String message = new String(body, "UTF-8");
                    System.out.println(" [x] Received '" + message + "'");
                }
            };
            rabbitPublisher.ConsumeMessages();
        }
        catch (Exception ex)
        {
            assert false;
        }


    }

    @Test
    public void TestDelayedPublishReceive()
    {

    }
}
