public class Starter {
    static int DEBUG = 1;

    public static void main (String[] args) {
        int port = 8080;
        WSCore server = new WSCore(port);
        server.start();
    }
}
