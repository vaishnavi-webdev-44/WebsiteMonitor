package WebsiteMonitor;

// An object containing the fields required for our tasks. The web server
// will queue up the first iteration of this, while the consumer will
// consume, update, and re-queue them.
public class Task {
    public String ListenerEmail;
    public String WebsiteeUrl;
    public int LastContentHash;
}
