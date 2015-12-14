package WebsiteMonitor;

import com.google.gson.Gson;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class TaskConsumer implements Consumer {

    private Mailer mailer;
    private RabbitPublisher rabbitPublisher;
    private Gson gson = new Gson();

    // For testing purposes we'll track if we've ever consumed any messages.
    // This allows us to easily write tests that consume one message.
    private static Boolean hasConsumedMessages = false;

    public TaskConsumer(Config config) throws IOException, TimeoutException {
        mailer = new Mailer(config.MailerEmail, config.MailerPassword);
        rabbitPublisher = new RabbitPublisher(config.RabbitHostName, config.QueueName, config.ExchangeName);
    }

    public void ConsumeOneMessage() throws IOException, InterruptedException {
        hasConsumedMessages = false;
        rabbitPublisher.rabbitChannel.basicConsume(
                rabbitPublisher.RabbitQueueName(), true, this);
        while (hasConsumedMessages == false)
        {
            Thread.sleep(100);
        }
    }

    public void RunForever() throws IOException {
        rabbitPublisher.rabbitChannel.basicConsume(
                rabbitPublisher.RabbitQueueName(), true, this);
        while (true)
        {
            // More elegant way to wait and consume messages indefinitely?
            // Looks like there's a blocking rabbit connection, but then I'd need
            // to be able to configure that within my publisher... it's an option...
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    public void handleConsumeOk(String s) { }

    public void handleCancelOk(String s) { }

    public void handleCancel(String reason) { }

    public void handleShutdownSignal(String s, ShutdownSignalException e) { }

    public void handleRecoverOk(String s) { }

    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
            throws IOException {
        String taskBytes = new String(body, "UTF-8");
        Task task = gson.fromJson(taskBytes, Task.class);
        System.out.println("Processing task" + taskBytes);

        // Set the current hash = last hash, so that if we fail to fetch the content
        // and we haven't hit TTL = 0, we treat this as no-change.
        int contentHash = task.LastContentHash;
        try
        {
            String content = WebsiteFetcher.FetchContent(task.WebsiteUrl);
            contentHash = content.hashCode();
            System.out.println("Succesfully fetched website, content hash: " + contentHash);
            task.TimeToLive = 5;
        }
        catch (IOException ex)
        {
            System.out.println("Failed to fetch site content, error" + ex);
            --task.TimeToLive;
            if (task.TimeToLive == 0)
            {
                String errorSubject = "Website watch cancelled";
                String errorMessage = String.format(
                        "After repeated attempts we were unable to fetch content from %1$s. "
                        + "We have cancelled the monitoring of it. Please re-submit the job "
                        + "if the website is functioning again and you wish to resume the watch.",
                        task.WebsiteUrl);
                mailer.SendMail(task.ListenerEmail, errorSubject, errorMessage);
                // Note we're not re-queue'ing the task.
                return;
            }
            // Potentially log here - this is expected behavior, which is why I hesitate, but
            // a flurry of such messages would indicate network errors on our side.
        }

        if (contentHash != task.LastContentHash)
        {
            System.out.println("Change detected");
            String subject = "Website content has changed";
            String message = String.format(
                    "Dear %1$s, we have detected a change in content of website %2$s.",
                    task.ListenerEmail, task.WebsiteUrl);
            mailer.SendMail(task.ListenerEmail, subject, message);
        }

        task.LastContentHash = contentHash;
        rabbitPublisher.EnqueueTask(task, 5 * 60 * 1000);

        hasConsumedMessages = true;

        // NOTE! There is a concerning race right here, after enqueue'ing the next iteration
        // of the monitor task and before the closing brace, where the function will end, rabbit
        // will consider the task complete, and remove it from the queue. If the process were
        // to crash after placing the next scheduled iteration of the job on the queue and before
        // it removed the current job, we would end up with 2 tasks in the queue for the same
        // job. You'd start getting double notifications about the website content changing.
        //
        // Sadly without an atomic method from rabbit to remove the task from the queue and
        // put a new one in, the solutions become non-trivial.
    }

}
