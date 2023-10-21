import java.sql.Timestamp;

public class Starter {
    static byte DEBUG = 2;

    public static void main (String[] args) {
        int port = 8080;
        WSCore server = new WSCore(port);
        server.start();
    }
}
