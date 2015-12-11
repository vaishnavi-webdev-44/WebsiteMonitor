import junit.framework.Assert;
import org.junit.Test;

import java.io.IOException;

public class TaskConsumerTest {
//
//    @Test
//    public void TestSendEmail()
//    {
//        Mailer mailer = new Mailer();
//        mailer.SendMail();
//    }

    @Test
    public void TestFetchWebsite()
    {
        TaskConsumer taskConsumer = new TaskConsumer();
        try
        {
            String content = taskConsumer.FetchContent("https://www.wikipedia.org/");
            assert(content != null);
        }
        catch (IOException ex)
        {
            assert(false);
        }
    }
}
