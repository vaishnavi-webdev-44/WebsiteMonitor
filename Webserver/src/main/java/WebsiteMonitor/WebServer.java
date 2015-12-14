package WebsiteMonitor;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.apache.log4j.Logger;
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

// A WebServer that implements a single POST endpoint; monitor
public class WebServer implements Container {

    private MailerInterface mailer;
    private RabbitPublisherInterface rabbitPublisher;
    private Logger log = Logger.getLogger(this.getClass().getName());

    public WebServer(
            MailerInterface newMailer,
            RabbitPublisherInterface newPublisher)
    {
        mailer = newMailer;
        rabbitPublisher = newPublisher;
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
            // The conditions under which this throws IOException aren't well specified
            // in the SimpleFramework documentation. Nothing to do but log and drop
            // the request.
            log.warn("Received IOException while getting response printStream. Exception" + ex);
            return;
        }

        // Following the pattern from this link:
        //   http://www.simpleframework.org/doc/tutorial/tutorial.php
        long time = System.currentTimeMillis();

        response.setValue("Content-Type", "text/plain");
        response.setValue("Server", "WebsiteMonitor");
        response.setDate("Date", time);
        response.setDate("Last-Modified", time);

        // TODO: easy way to find the HTTP verb from the request? We only support post.

        // We only have one endpoint; normally we'd have a map of handlers, and
        // no handler in the map would return forbidden.
        if (!request.getPath().toString().equals("/monitor"))
        {
            log.info("Attempted access on unsupported path " + request.getPath().toString());
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
            body.close();
            return;
        }

        // The request is valid, let's make sure the website is reachable.
        int contentHash;
        try
        {
            String content = WebsiteFetcher.FetchContent(urlToMonitor);
            contentHash = content.hashCode();
        }
        catch (IOException ex)
        {
            // Not quite sure which error code to set here. Their URL could be bad,
            // or the site could be temporarily unavailable. We could probably forward
            // more error information from the website fetch... The http error code if
            // it was an http error...
            // This is an expected error, so nothing to log about. Although a flurry of
            // network unavailables would be indiciative of a problem on our side...
            response.setStatus(Status.SERVICE_UNAVAILABLE);
            body.close();
            return;
        }

        try {
            RegisterTask(emailToNotify, urlToMonitor, contentHash);
        } catch (IOException e) {
            // It'd be reasonable to crash here if we had a process monitor that would
            // reboot us. Startup would reconnect to rabbit, which is about the only
            // option that we have.
            log.error("Error registering task with rabbitMq");
            response.setStatus(Status.INTERNAL_SERVER_ERROR);
            body.close();
            return;
        }

        NotifyWatchRegistered(emailToNotify, urlToMonitor);

        response.setStatus(Status.OK);
        body.close();
    }

    private void NotifyWatchRegistered(String email, String websiteUrl)
    {
        System.out.println("Sending success email");
        String successSubject = "Watch registered";
        String successMessage = String.format("Successfully registered a watch on website %1$s", websiteUrl);
        mailer.SendMail(email, successSubject, successMessage);
    }

    private void RegisterTask(String email, String websiteuUrl, int contentHash)
            throws IOException {
        Task task = new Task();
        task.LastContentHash = contentHash;
        task.ListenerEmail = email;
        task.WebsiteUrl = websiteuUrl;
        task.TimeToLive = 5; // Could parameterize the TTL
        rabbitPublisher.EnqueueTask(task, 5 * 60 * 1000); // Could parameterize the poll period
    }
}
