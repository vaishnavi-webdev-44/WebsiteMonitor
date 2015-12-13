package WebsiteMonitor;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class WebServerTest {

    private WebServer webServer;

    // Every test needs the webServer up and bound to RabbitMQ
    // This implicitly tests that RabbitMQ connection can be established
    // Although it's not great that it's outside of a @Test method, but
    // I don't really want to repeat this code in every test... sure I could
    // make it a function...
    @Before
    public void Initialize() throws IOException, TimeoutException
    {
        Config config = new Config();
        config.RabbitHostName = "localhost";
        config.QueueName = "TEST_QUEUE";

        webServer = new WebServer(config);
    }

    // Probably need a helper method to clear/test the queue

    // Just test that the thing starts up and binds the rabbitMQ queue
//    public void TestStartServer()
//    {
//
//    }

    // Test that we can we can queue an item on rabbitMQ
    @Test
    public void TestQueueTask()
    {
        Task task = new Task();
        task.WebsiteeUrl = "foo";
        task.ListenerEmail = "email";
        task.LastContentHash = 42;

        webServer.EnqueueTask(task);
    }

    // Test that we if we attempt to register a watch on a website that does
    // not respond, we get an email telling us so

    // Test that if we attempt to register a watch on a website that gives
    // us forbidden, we get an email telling us so

    // Test that registering an a watch in the valid case emails us success
    // -- We could also test that the message shows up on the queue in Rabbit

    // Test that providing a bad email address does NOT add a message to the queue
}
