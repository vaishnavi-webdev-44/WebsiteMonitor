package WebsiteMonitor;

import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

// Resources used to learn how to send emails in java
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

public class Mailer implements MailerInterface {

    private Session session;

    public Mailer(final String mailerEmail, final String mailerPassword) {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");

        // I don't like throwing exceptions within constructors, but I'm not going to
        // get too fussy about it right now... probably an old C++ habit...
        session = Session.getDefaultInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(mailerEmail, mailerPassword);
                    }
                });
    }

    public void SendMail(String email, String subject, String content) {
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(email));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(email));
            message.setSubject(subject);
            message.setText(content);

            Transport.send(message);
        } catch (MessagingException e) {
            // The level of information we have in this error is pretty vague.
            // Some reading tells me that at best we can verify that the mail
            // was delivered to google's server, but nothing about if the
            // mail was actually delivered or the email address exists.
            throw new RuntimeException(e);
        }
    }
}