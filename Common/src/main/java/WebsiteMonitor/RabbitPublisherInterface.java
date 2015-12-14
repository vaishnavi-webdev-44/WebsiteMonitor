package WebsiteMonitor;

import com.rabbitmq.client.Channel;

import java.io.IOException;

public interface RabbitPublisherInterface {
    String RabbitQueueName();
    Channel GetChannel();
    void EnqueueTask(Task task, long taskDelayMs) throws IOException;
}
