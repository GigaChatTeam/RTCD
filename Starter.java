public class Starter {
    static byte DEBUG = 5;
    static int port = 8080;

    static {
        JsonIteratorExtra.UUIDSupport.registerModule();
        new JsonIteratorExtra.SQLTimestampSupport(Helper.Constants.timestamp).registerModule();
    }

    public static void main (String[] args) {
        WSCore server = new WSCore(port);
        server.start();
    }
}
