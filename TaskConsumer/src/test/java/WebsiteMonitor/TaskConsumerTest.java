package WebsiteMonitor;

import com.rabbitmq.client.Channel;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class TaskConsumerTest {

    private static Boolean emailReceived = false;
    private static Task lastPostedTask = null;
    private static RabbitPublisher rabbitPublisher = null;

    private MailerInterface mockMailer = new MailerInterface() {
        // Why doesn't intellij @Override this like it did in WebServerTest?...
        public void SendMail(String email, String subject, String content) {
            emailReceived = true;
        }
    };

    // We need a real rabbit connection, because TaskConsumer is the only thing that
    // runs the consuming messages code. We therefore don't want to mock that, we want
    // to test it. We do however want to intercept Enqueue'ing tasks, as we'll use that
    // to validate the TaskConsumer behavior, and to prevent us from polluting the queue.
    private RabbitPublisherInterface mockPublisher = new RabbitPublisherInterface() {

        public String RabbitQueueName() {
            return rabbitPublisher.RabbitQueueName();
        }

        public Channel GetChannel() {
            return rabbitPublisher.GetChannel();
        }

        public void EnqueueTask(Task task, long taskDelayMs) throws IOException {
            lastPostedTask = task;
        }
    };

    public TaskConsumerTest() throws IOException, TimeoutException {
        rabbitPublisher = new RabbitPublisher("localhost", "TEST_QUEUE", "TEST_EXCHANGE");
    }

    private Thread CreateThreadedConsumer()
    {
        emailReceived = false;
        lastPostedTask = null;

        // Reference for some simple threading in a JUnit test:
        //   http://stackoverflow.com/questions/30403913/how-can-i-test-a-blocking-method-using-junit
        Thread consumerThread = new Thread(() -> {
            TaskConsumer consumer = null;
            try {
                consumer = new TaskConsumer(mockMailer, mockPublisher);
                consumer.RunForever();
            }
            catch (IOException ex)
            {
            }
        });

        return consumerThread;
    }

    @Test
    public void TestEmailRules() throws IOException, InterruptedException, TimeoutException {
        // Create the consumer with our mocks
        Thread consumerThread = CreateThreadedConsumer();
        consumerThread.start();

        // Publish a task for it to work on. This should generate an email as the
        // content has changed.
        Task task = new Task();
        task.WebsiteUrl = "https://www.wikipedia.org/";
        task.ListenerEmail = "coding.challenge.2015@gmail.com";
        task.LastContentHash = 42;
        task.TimeToLive = 5;
        rabbitPublisher.EnqueueTask(task, 0);

        // Ensure that a new task is scheduled, and that no email is sent.
        // Give this 5 seconds to work.
        long startTime = System.currentTimeMillis();
        while (lastPostedTask == null)
        {
            Thread.sleep(10);
            if (System.currentTimeMillis() > startTime + 5000)
            {
                throw new TimeoutException("Failed waiting for new task to be posted.");
            }
        }
        // We should have received an email about this
        assert emailReceived == true;
        emailReceived = false;
        // Store the last content hash, so we can put it in the next piece of scheduled work.
        // They will hopefully match... But I need to mock the website fetcher if I'm going
        // to actually guarantee this.
        task.LastContentHash = lastPostedTask.LastContentHash;
        lastPostedTask = null;

        // Republish the task but with the last known hash; the task should be requeued and
        // no email should be sent.
        rabbitPublisher.EnqueueTask(task, 0);

        startTime = System.currentTimeMillis();
        while (lastPostedTask == null)
        {
            Thread.sleep(10);
            if (System.currentTimeMillis() > startTime + 5000)
            {
                throw new TimeoutException("Failed waiting for new task to be posted.");
            }
        }
        assert emailReceived == false;
        consumerThread.interrupt();
    }

    @Test
    public void TestTtlExpired() throws IOException, InterruptedException, TimeoutException {
        // Create the consumer with our mocks
        Thread consumerThread = CreateThreadedConsumer();
        consumerThread.start();

        // Publish a task for it to work on. This should generate an email as the
        // content has changed.
        Task task = new Task();
        task.WebsiteUrl = "htt:/bogus/url/";
        task.ListenerEmail = "coding.challenge.2015@gmail.com";
        task.LastContentHash = 42;
        task.TimeToLive = 1;
        rabbitPublisher.EnqueueTask(task, 0);

        // Ensure that a new task is scheduled, and that no email is sent.
        // Give this 5 seconds to work.
        long startTime = System.currentTimeMillis();
        while (emailReceived == false)
        {
            Thread.sleep(10);
            if (System.currentTimeMillis() > startTime + 5000)
            {
                throw new TimeoutException("Failed waiting for new task to be posted.");
            }
        }
        assert lastPostedTask == null;
    }

}
