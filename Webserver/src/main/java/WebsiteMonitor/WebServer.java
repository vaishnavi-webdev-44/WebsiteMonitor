package WebsiteMonitor;

// Basic webserver example copied from
// http://stackoverflow.com/questions/3732109/simple-http-server-in-java-using-only-java-se-api
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeoutException;

import com.google.gson.Gson;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;

// A WebServer that implements a single POST endpoint; watch_and_notify
public class WebServer {

    private Mailer mailer;
    private RabbitPublisher rabbitPublisher;
    private HttpServer server;

    public WebServer(Config config) throws IOException, TimeoutException
    {
        mailer = new Mailer(config.MailerEmail, config.MailerPassword);
        rabbitPublisher = new RabbitPublisher(config.RabbitHostName, config.QueueName, config.ExchangeName);

        server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/watch_and_notify", new MyHandler());
        server.setExecutor(null); // creates a default executor
    }

    public void StartServer()
    {
        server.start();
    }

    static class MyHandler implements HttpHandler {
        // Override taken from code sample, intellij claims it's not allowed. Ok.
//        @Override
        public void handle(HttpExchange t) throws IOException {
            // Need to validate the request is a POST. That's the only verb we accept.
            // Need to extract the parameters from the request.
            //
            // Wow. How is it that implementing http servers in java is such a chore?
            // Manual parameter parsing from the URI?
            // Every code sample I've found for implementing a simple "Get a post,
            // get some parameters" has been 200ish lines of code. No way.
            String response = "This is the response";
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    public void PostTask(String listenerEmail, String websiteUrl) throws IOException {
        // Fetch the website: we want the original content hash, and we want to
        // verify the site is actually reachable.
        int contentHash;
        try
        {
            String content = WebsiteFetcher.FetchContent(websiteUrl);
            contentHash = content.hashCode();
        }
        catch (IOException ex)
        {
            // If the website was unreachable for any reason we'll inform the caller
            // of the failure and not schedule the task. We only want to monitor valid
            // websites.
            String errorSubject = "Could not monitor website";
            String errorMessage =
                    String.format("We were unable to create a watch on website %1$s as we received error %2$s",
                    websiteUrl, ex.toString());
            mailer.SendMail(listenerEmail, errorSubject, errorMessage);
            return;
        }

        // Send an email: we want to verify the email address is actually valid.
        String successSubject = "Watch registered";
        String successMessage = String.format("Successfully registered a watch on website %1$s", websiteUrl);
        mailer.SendMail(listenerEmail, successSubject, successMessage);

        // The website exists and is accessible, the email of the listener is good;
        // let's schedule the actual task now.
        Task task = new Task();
        task.LastContentHash = contentHash;
        task.ListenerEmail = listenerEmail;
        task.WebsiteUrl = websiteUrl;
        task.TimeToLive = 5;
        rabbitPublisher.EnqueueTask(task, 5 * 60 * 1000);
    }
}
