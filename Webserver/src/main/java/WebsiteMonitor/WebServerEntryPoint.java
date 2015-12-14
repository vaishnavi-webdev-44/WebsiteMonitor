package WebsiteMonitor;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import java.net.URL;

// http://stackoverflow.com/questions/6068197/utils-read-resource-text-file-to-string-java

public class WebServerEntryPoint {
    public static void main(String[] args) throws Exception {

        URL url = Resources.getResource("config.json");
        String text = Resources.toString(url, Charsets.UTF_8);

        Gson gson = new Gson();
        Config config = gson.fromJson(text, Config.class);

        Mailer mailer =
                new WebsiteMonitor.Mailer(config.MailerEmail, config.MailerPassword);
        RabbitPublisher rabbitPublisher =
                new RabbitPublisher(config.RabbitHostName, config.QueueName, config.ExchangeName);

        WebServer webServer = new WebServer(mailer, rabbitPublisher);
        webServer.StartServer();
    }
}
