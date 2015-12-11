import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class Mailer {

    private Session session;

    public Mailer() {
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
                        return new PasswordAuthentication("coding.challenge.2015@gmail.com","thisisasimplepassword");
                    }
                });
    }

    public void SendMail() {
        try {

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("coding.challenge.2015@gmail.com"));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse("coding.challenge.2015@gmail.com"));
            message.setSubject("Testing Subject");
            message.setText("Dear Mail Crawler," +
                    "\n\n No spam to my email, please!");

            Transport.send(message);

            System.out.println("Done");

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
}