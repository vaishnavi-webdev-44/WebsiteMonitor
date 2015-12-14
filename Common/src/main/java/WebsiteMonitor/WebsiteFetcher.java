package WebsiteMonitor;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.SocketTimeoutException;

// Resources used:
//
// How to fetch webpage content in java:
//     http://stackoverflow.com/questions/238547/how-do-you-programmatically-download-a-webpage-in-java
//

public class WebsiteFetcher
{
        public static String FetchContent(String websiteUrl) throws IOException {
            // Query the target host
            Document document = null;
            try {
                Connection connection = Jsoup.connect(websiteUrl);
                document = connection.get();
            } catch (SocketTimeoutException ex) {
                // Transient error. Maybe the website is down, maybe our network is down.
                return null;
            } catch (Exception ex) {
                // I'm not happy with how I've handled this; I should probably let all
                // the errors up and force the caller to deal with them. I'm really
                // trying to group together a class of errors that indicate the user
                // may have entered bad info; an invalid URL, a website that doesn't exist,
                // a website that we don't have access to, etc.

                // Per jsoup's documentation this can be:
                // Malformed URL
                // HttpStatusError (most of these are fatal, but we could probably parse
                //   this further to identify some transient http error codes)
                // IOException (this is poorly defined by jsoup)
                // UnsupportedMimeType
                throw new IOException("Fatal error fetching website content");
            }

            // We want to monitor the content, and 'body' seems the correct piece for this.
            // Changing headers aren't really a difference in the website content.
            return document.body().html();
        }
}
