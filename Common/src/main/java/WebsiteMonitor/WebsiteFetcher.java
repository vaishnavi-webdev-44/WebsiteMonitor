package WebsiteMonitor;

import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.SocketTimeoutException;

public class WebsiteFetcher
{
        public static String FetchContent(String websiteUrl) throws IOException {
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
}
