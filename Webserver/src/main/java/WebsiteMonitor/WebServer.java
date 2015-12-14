package WebsiteMonitor;

// Basic webserver example copied from
// http://stackoverflow.com/questions/3732109/simple-http-server-in-java-using-only-java-se-api
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.TimeoutException;

import org.simpleframework.http.Query;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerServer;
import org.simpleframework.transport.Server;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;


// Reference:
//     http://www.simpleframework.org/doc/tutorial/tutorial.php


// A WebServer that implements a single POST endpoint; watch_and_notify
public class WebServer implements Container {

    private Mailer mailer;
    private RabbitPublisher rabbitPublisher;

    public WebServer(WebsiteMonitor.Config config)
            throws IOException, TimeoutException
    {
        mailer = new WebsiteMonitor.Mailer(config.MailerEmail, config.MailerPassword);
        rabbitPublisher = new RabbitPublisher(config.RabbitHostName, config.QueueName, config.ExchangeName);
    }

    public void StartServer() throws IOException {
        Server server = new ContainerServer(this);
        Connection connection = new SocketConnection(server);
        SocketAddress address = new InetSocketAddress(8080);

        connection.connect(address);
    }

    public void handle(Request request, Response response) {
        PrintStream body;
        try
        {
            body = response.getPrintStream();
        }
        catch (IOException ex)
        {
            // log error, how does this happen?
            return;
        }

        // Following the pattern from this link:
        //   http://www.simpleframework.org/doc/tutorial/tutorial.php
        long time = System.currentTimeMillis();

        response.setValue("Content-Type", "text/plain");
        response.setValue("Server", "WebsiteMonitor");
        response.setDate("Date", time);
        response.setDate("Last-Modified", time);

        // We only have one endpoint; normally we'd have a map of handlers, and
        // no handler in the map would return forbidden.
        if (!request.getPath().toString().equals("/monitor"))
        {
            System.out.println("Forbidden, path was " + request.getPath().toString());
            response.setStatus(Status.FORBIDDEN);
            body.close();
            return;
        }

        // Let's extract the parameters from the request and provide some meaningful
        // errors if it's not usable. I'm not great with java collection manipulation,
        // but if I was in C# I would do some kind of filter to find the  missing
        // elements of the request parameters so I could easily call them out
        // in the response. I'll keep it simple here.
        Query query = request.getQuery();

        String urlToMonitor = query.get("urlToMonitor");
        String emailToNotify = query.get("emailToNotify");

        if (urlToMonitor == null || emailToNotify == null)
        {
            response.setStatus(Status.BAD_REQUEST);
            body.println("Required parameters: urlToMonitor, emailToNotify");
            body.close();
            return;
        }

        System.out.println("Got request for email " + emailToNotify + " url " + urlToMonitor);

        try {
            PostTask(emailToNotify, urlToMonitor);
        } catch (IOException e) {
            // Think about this more...
            e.printStackTrace();
        }

        response.setStatus(Status.OK);
        body.close();
    }

    public void PostTask(String listenerEmail, String websiteUrl) throws IOException {
        // Fetch the website: we want the original content hash, and we want to
        // verify the site is actually reachable.
        int contentHash;
        try
        {
            System.out.println("Fetching website");
            String content = WebsiteFetcher.FetchContent(websiteUrl);
            contentHash = content.hashCode();
        }
        catch (IOException ex)
        {
            // If the website was unreachable for any reason we'll inform the caller
            // of the failure and not schedule the task. We only want to monitor valid
            // websites.

            System.out.println("Sending error email");
            String errorSubject = "Could not monitor website";
            String errorMessage =
                    String.format("We were unable to create a watch on website %1$s as we received error %2$s",
                    websiteUrl, ex.toString());
            mailer.SendMail(listenerEmail, errorSubject, errorMessage);
            return;
        }

        // Send an email: we want to verify the email address is actually valid.

        System.out.println("Sending success email");
        String successSubject = "Watch registered";
        String successMessage = String.format("Successfully registered a watch on website %1$s", websiteUrl);
        mailer.SendMail(listenerEmail, successSubject, successMessage);

        // The website exists and is accessible, the email of the listener is good;
        // let's schedule the actual task now.
        System.out.println("Posting task");
        Task task = new Task();
        task.LastContentHash = contentHash;
        task.ListenerEmail = listenerEmail;
        task.WebsiteUrl = websiteUrl;
        task.TimeToLive = 5;
        rabbitPublisher.EnqueueTask(task, 5 * 60 * 1000);
    }
}
