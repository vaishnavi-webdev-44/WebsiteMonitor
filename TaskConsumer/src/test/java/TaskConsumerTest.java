import junit.framework.Assert;
import org.junit.Test;

public class TaskConsumerTest {

//    @Test
//    public void TestSendEmail()
//    {
//        TaskConsumer consumer = new TaskConsumer();
//        consumer.setMailServerProperties();
//        try
//        {
//            consumer.sendEmail();
//        }
//        catch (Exception ex)
//        {
//            System.out.println(ex.toString());
//            assert false;
//        }
//    }

    @Test
    public void TestSendEmail()
    {
        Mailer mailer = new Mailer();
        mailer.SendMail();
    }
}
