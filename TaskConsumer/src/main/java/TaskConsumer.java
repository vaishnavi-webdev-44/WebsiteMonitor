import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;

// Resources used - here's a list of the various web pages I used when learning how
// to do the various bits of work here, and for associated code samples (and code copying.)
//
// How to send an email in java:
//     http://www.tutorialspoint.com/java/java_sending_email.htm
//
// Hmmm, running my own SMTP server seems like needless hassle (and another service
// dependency I have to deploy and maintain.) I'll bet I can auth with google and use
// their mail servers to do the sending... Yup.
//     http://programmers.stackexchange.com/questions/229272/sending-e-mails-without-using-an-e-mail-server
//
// Well that didn't work, messaging errors, and it's an old tutorial... I wonder if
// google has changed their auth and wants SSL or something better now... Yup. Ok, new
// example from here:
//     http://www.mkyong.com/java/javamail-api-sending-email-via-gmail-smtp-example/

public class TaskConsumer {

    //region Code copied from http://programmers.stackexchange.com/questions/229272/sending-e-mails-without-using-an-e-mail-server
    private Session mailSession;

    public void setMailServerProperties()
    {
        Properties emailProperties = System.getProperties();
        emailProperties.put("mail.smtp.port", "465");
        emailProperties.put("mail.smtp.auth", "true");
        emailProperties.put("mail.smtp.starttls.enable", "true");
        mailSession = Session.getDefaultInstance(emailProperties, null);
    }

    private MimeMessage draftEmailMessage() throws AddressException, MessagingException
    {
        String[] toEmails = { "coding.challenge.2015@gmail.com" };
        String emailSubject = "Test email subject";
        String emailBody = "This is an email sent by http://www.coding.challenge.2015";
        MimeMessage emailMessage = new MimeMessage(mailSession);
        /**
         * Set the mail recipients
         * */
        for (int i = 0; i < toEmails.length; i++)
        {
            emailMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmails[i]));
        }
        emailMessage.setSubject(emailSubject);
        /**
         * If sending HTML mail
         * */
        emailMessage.setContent(emailBody, "text/html");
        /**
         * If sending only text mail
         * */
        //emailMessage.setText(emailBody);// for a text email
        return emailMessage;
    }

    public void sendEmail() throws AddressException, MessagingException
    {
        /**
         * Sender's credentials
         * */
        String fromUser = "coding.challenge.2015@gmail.com";
        String fromUserEmailPassword = "thisisasimplepassword";

        String emailHost = "smtp.gmail.com";
        Transport transport = mailSession.getTransport("smtp");
        transport.connect(emailHost, fromUser, fromUserEmailPassword);
        /**
         * Draft the message
         * */
        MimeMessage emailMessage = draftEmailMessage();
        /**
         * Send the mail
         * */
        transport.sendMessage(emailMessage, emailMessage.getAllRecipients());
        transport.close();
        System.out.println("Email sent successfully.");
    }
    //endregion

//
//    public static void SendEmail(
//            String recipient,
//            String subject,
//            String content)
//    {
//        // Sample code taken from http://www.tutorialspoint.com/java/java_sending_email.htm
//
//        // Get system properties
//        Properties properties = System.getProperties();
//
//        // Setup mail server
//        properties.setProperty("mail.smtp.host", "localhost");
//
//        // Get the default Session object.
//        Session session = Session.getDefaultInstance(properties);
//
//        try{
//            // Create a default MimeMessage object.
//            MimeMessage message = new MimeMessage(session);
//
//            // Set From: header field of the header.
//            message.setFrom(new InternetAddress("no-reply@WebsiteMonitor.com"));
//
//            // Set To: header field of the header.
//            message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
//
//            // Set Subject: header field
//            message.setSubject(subject);
//
//            // Now set the actual message
//            message.setText(content);
//
//            // Send message
//            Transport.send(message);
//            System.out.println("Sent message successfully....");
//        }catch (MessagingException mex) {
//            mex.printStackTrace();
//        }
//    }
}
