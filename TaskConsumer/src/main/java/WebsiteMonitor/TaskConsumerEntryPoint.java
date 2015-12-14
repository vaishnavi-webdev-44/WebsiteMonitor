package WebsiteMonitor;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.gson.Gson;

import java.net.URL;

public class TaskConsumerEntryPoint {
    public static void main(String[] args) throws Exception {

        URL url = Resources.getResource("config.json");
        String text = Resources.toString(url, Charsets.UTF_8);

        Gson gson = new Gson();
        Config config = gson.fromJson(text, Config.class);

        Mailer mailer =
                new WebsiteMonitor.Mailer(config.MailerEmail, config.MailerPassword);
        RabbitPublisher rabbitPublisher =
                new RabbitPublisher(config.RabbitHostName, config.QueueName, config.ExchangeName);

        TaskConsumer taskConsumer = new TaskConsumer(mailer, rabbitPublisher);
        taskConsumer.RunForever();
    }
}
