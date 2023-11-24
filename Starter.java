public class Starter {
    static byte DEBUG = 5;

    static {
        JsonIteratorExtra.UUIDSupport.registerModule();
        new JsonIteratorExtra.SQLTimestampSupport(Helper.Constants.timestamp).registerModule();
    }

    public static void main (String[] args) {
        int port = 8080;
        WSCore server = new WSCore(port);
        server.start();
    }
}
