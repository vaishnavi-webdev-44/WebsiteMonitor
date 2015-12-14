package WebsiteMonitor;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class WebServerTest {

    // Probably need a helper method to clear/test the queue
    @Test
    public void TestRequestValid()
    {
        // Reference for some simple threading in a JUnit test:
        //   http://stackoverflow.com/questions/30403913/how-can-i-test-a-blocking-method-using-junit
        Thread serverThread = new Thread(() -> {
            Config config = new Config();
            config.RabbitHostName = "localhost";
            config.QueueName = "TEST_QUEUE";
            config.ExchangeName = "TEST_EXCHANGE";
            config.MailerEmail = "coding.challenge.2015@gmail.com";
            config.MailerPassword = "thisisasimplepassword";

            WebServer webServer = null;
            try {
                webServer = new WebServer(config);
                webServer.StartServer();
            } catch (Exception ex) {
                assert false;
            }
        });

        serverThread.start();
        try {
            // Reference for simple http post with blocking for response:
            //   http://unirest.io/java.html
            HttpResponse<JsonNode> jsonResponse = Unirest.post("http://localhost:8080/monitor")
                    .header("accept", "application/json")
                    .field("urlToMonitor", "https://www.wikipedia.org/")
                    .field("emailToNotify", "coding.challenge.2015@gmail.com")
                    .asJson();
            System.out.println(jsonResponse.getBody().toString());
        } catch (UnirestException e) {
            e.printStackTrace();
        }
        serverThread.interrupt();
    }
}
