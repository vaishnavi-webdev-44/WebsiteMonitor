package WebsiteMonitor;

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
        rabbitPublisher.EnqueueTask(task);
    }

    @Test
    public void TestDelayedPublishReceive()
    {

    }
}
