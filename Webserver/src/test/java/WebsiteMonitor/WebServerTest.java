package WebsiteMonitor;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.rabbitmq.client.Channel;
import org.junit.Test;
import org.simpleframework.http.Status;
import java.io.IOException;

public class WebServerTest {

    // I wish I could bind local variables in my lambda's so that each test could have
    // privately scoped variables so there's no chance of stomping eachother's results
    // or me forgetting to reset the vars, but c'est la vie.
    private static Boolean emailReceived = false;
    private static Boolean taskScheduled = false;

    private MailerInterface mockMailer = new MailerInterface() {
        @Override
        public void SendMail(String email, String subject, String content) {
            emailReceived = true;
        }
    };

    private RabbitPublisherInterface mockPublisher = new RabbitPublisherInterface() {
        @Override
        public String RabbitQueueName() {
            return null;
        }

        @Override
        public Channel GetChannel() {
            return null;
        }

        @Override
        public void EnqueueTask(Task task, long taskDelayMs) throws IOException {
            taskScheduled = true;
        }
    };

    private Thread CreateThreadedServer()
    {
        emailReceived = false;
        taskScheduled = false;

        // Reference for some simple threading in a JUnit test:
        //   http://stackoverflow.com/questions/30403913/how-can-i-test-a-blocking-method-using-junit
        Thread serverThread = new Thread(() -> {
            WebServer webServer = null;
            try {
                webServer = new WebServer(mockMailer, mockPublisher);
                webServer.StartServer();
            }
            catch (IOException ex)
            {
            }
        });

        return serverThread;
    }

    // Test that a valid request results in an email and a scheduled task
    @Test
    public void TestRequestValid()
    {
        Thread serverThread = CreateThreadedServer();

        serverThread.start();
        try {
            // Reference for simple http post with blocking for response:
            //   http://unirest.io/java.html
            HttpResponse<JsonNode> jsonResponse = Unirest.post("http://localhost:8080/monitor")
                    .header("accept", "application/json")
                    .field("urlToMonitor", "https://www.wikipedia.org/")
                    .field("emailToNotify", "coding.challenge.2015@gmail.com")
                    .asJson();
            assert emailReceived == true;
            assert taskScheduled == true;
        } catch (UnirestException e) {
            e.printStackTrace();
            assert false;
        }
        serverThread.interrupt();
    }

    // Test that a valid request results in an email and a scheduled task
    @Test
    public void TestBadParameters()
    {
        Thread serverThread = CreateThreadedServer();

        serverThread.start();
        try {
            HttpResponse<JsonNode> jsonResponse = Unirest.post("http://localhost:8080/monitor")
                    .header("accept", "application/json")
                    .field("foo", "https://www.wikipedia.org/")
                    .field("bar", "coding.challenge.2015@gmail.com")
                    .asJson();
            assert emailReceived == false;
            assert taskScheduled == false;
            assert jsonResponse.getStatus() == Status.BAD_REQUEST.code;
        } catch (UnirestException e) {
            e.printStackTrace();
            assert false;
        }
        serverThread.interrupt();
    }

    // Test that a valid request results in an email and a scheduled task
    @Test
    public void TestBadPath()
    {
        Thread serverThread = CreateThreadedServer();

        serverThread.start();
        try {
            HttpResponse<JsonNode> jsonResponse = Unirest.post("http://localhost:8080/bad_path")
                    .header("accept", "application/json")
                    .field("urlToMonitor", "https://www.wikipedia.org/")
                    .field("emailToNotify", "coding.challenge.2015@gmail.com")
                    .asJson();
            assert emailReceived == false;
            assert taskScheduled == false;
            assert jsonResponse.getStatus() == Status.FORBIDDEN.code;
        } catch (UnirestException e) {
            e.printStackTrace();
            assert false;
        }
        serverThread.interrupt();
    }
}
