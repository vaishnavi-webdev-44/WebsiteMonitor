package WebsiteMonitor;

public interface MailerInterface {
    void SendMail(String email, String subject, String content);
}
