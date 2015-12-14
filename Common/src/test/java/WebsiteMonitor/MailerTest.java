package WebsiteMonitor;

import org.junit.Test;

public class MailerTest {

    // This is obviously a very crappy test; all it's really testing is that
    // we don't get any exceptions while sending the email. It doesn't verify
    // the email actually gets anywhere. Additionally, it depends on the google
    // email service.
    //
    // At the very least it serves me in development as I can trigger an email
    // and check the inbox manually.
    // I'm leaving it commented out unless someone wants to run it manually.
//    @Test
//    public void TestSendEmail()
//    {
//        Mailer mailer = new Mailer("coding.challenge.2015@gmail.com","thisisasimplepassword");
//        mailer.SendMail("coding.challenge.2015@gmail.com", "Test subject", "This is a test message");
//    }
}
