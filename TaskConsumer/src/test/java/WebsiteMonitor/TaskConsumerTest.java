package WebsiteMonitor;

import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class TaskConsumerTest {

    // We need a way to purge the existing queue from code here as test setup.
    // Even worse, the delayed rabbitMQ exchange feature doesn't post tasks to
    // the queue until the time they are ready, meaning you can't purge them
    // until they're ready. Because our TaskConsumer reschedules things for
    // continued monitoring, it's hard to purge these...
    @Test
    public void TestEmailForWebsiteChanged() throws IOException, TimeoutException, InterruptedException {
        Config config = new Config();
        config.MailerEmail = "coding.challenge.2015@gmail.com";
        config.MailerPassword = "thisisasimplepassword";
        config.RabbitHostName = "localhost";
        config.QueueName = "TEST_QUEUE";
        config.ExchangeName = "TEST_EXCHANGE";

        RabbitPublisher rabbitPublisher = new RabbitPublisher(
                config.RabbitHostName, config.QueueName, config.ExchangeName);
        Task task = new Task();
        task.WebsiteUrl = "https://www.wikipedia.org/";
        task.ListenerEmail = "coding.challenge.2015@gmail.com";
        task.LastContentHash = 42;
        task.TimeToLive = 5;
        rabbitPublisher.EnqueueTask(task, 0);

        TaskConsumer taskConsumer = new TaskConsumer(config);
        taskConsumer.ConsumeOneMessage();
        // And right here, it'd be really handy to have a mock of the Mailer class.
        // It'd be easy enough to start passing a Mailer and a RabbitPublisher instead
        // of the config for the two...
    }

    public void TestFailureEmail()
    {

    }

}
