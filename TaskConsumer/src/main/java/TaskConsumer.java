import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;

// Resources used:
//
// How to fetch webpage content in java:
//     http://stackoverflow.com/questions/238547/how-do-you-programmatically-download-a-webpage-in-java
//
//

public class TaskConsumer {

    // My inputs are:
    // Hash of last webpage content
    // Email address of listener to inform
    // Potentially a TTL?
    public void ProcessTask(String url, int lastHash, String listenerEmail)
    {
        String content = null;
        try
        {
            content = FetchContent(url);
        }
        catch (IOException ex)
        {
            // Mailer.SendMail(listenerEmail, "I'm sorry but it's dead Jim.");
        }

        // Set the hashes equal if we had an intermittent error; this makes it a no-op.
        // We don't return because we need to reschedule.
        int newHash = lastHash;
        if (content != null)
        {
            newHash = content.hashCode();
        }

        // lastHash = 0 => we have never queried before, we don't know the content...
        // although...
        // in the http service we could query immediately, to ensure the website
        // exists, is reachable, and to get a starting hash. Ok, I like that.
        if (newHash != lastHash && lastHash != 0)
        {
            // We've detected a change
        }

        ScheduleTask(url, newHash, listenerEmail);
    }

    // Fetch the content of a given URL. This does not traverse the entire website.
    // This
    // Throws IOException on any fatal error
    public String FetchContent(String websiteUrl) throws IOException {
        // Query the target host
        Connection connection = Jsoup.connect(websiteUrl);
        Document document = null;
        try {
            document = connection.get();
        } catch (SocketTimeoutException ex) {
            // Transient error. Maybe the website is down, maybe our network is down.
            return null;
        } catch (Exception ex) {
            // Per jsoup's documentation this can be:
            // Malformed URL
            // HttpStatusError (most of these are fatal, but we could probably parse
            //   this further to identify some transient http error codes)
            // IOException (this is poorly defined by jsoup)
            // UnsupportedMimeType
            throw new IOException("Fatal error fetching website content");
        }

        // I think the body is the actual page content? I'm not very knowledgable about
        // html...
        return document.body().html();
    }

    private void ScheduleTask(String url, int lastHash, String listenerEmail)
    {

    }

}
