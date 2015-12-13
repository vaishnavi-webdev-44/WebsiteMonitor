public class EntryPoint {
    public static void main(String[] args) throws Exception {
        Config config = new Config();
        config.QueueName = "TEST_QUEUE";
        config.RabbitHostName = "localhost";

        WebServer webServer = new WebServer(config);
        webServer.StartServer();
    }
}
