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

    public void ProcessTask()
    {

    }

    // Throws IOException on any fatal error
    public String FetchContent(String websiteUrl) throws IOException
    {
        // Query the target host
        Connection connection = Jsoup.connect(websiteUrl);
        Document document = null;
        try {
            document = connection.get();
        }
        catch (SocketTimeoutException ex)
        {
            // Transient error. Maybe the website is down, maybe our network is down.
            return null;
        }
        catch (Exception ex)
        {
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
    
    private void CancelTask()
    {
        // There's a problem with the URL we're trying to monitor. The URL is
        // bad, the website doesn't exist, we don't have access to the webpage...
        // These errors should be permanent. We'll cancel the monitor and inform
        // the listener that we're no longer monitoring.
    }

    private void Reschedule()
    {

    }

}
