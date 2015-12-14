package WebsiteMonitor;

import java.io.IOException;

public interface RabbitPublisherInterface {
    void EnqueueTask(Task task, long taskDelayMs) throws IOException;
}
