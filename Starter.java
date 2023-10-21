import java.sql.Timestamp;

public class Starter {
    static byte DEBUG = 2;

    public static void main (String[] args) {
//        int port = 8080;
//        WSCore server = new WSCore(port);
//        server.start();

        ResponsesPatterns.Channels.Messages.Post.New test = new ResponsesPatterns.Channels.Messages.Post.New(1, 1, "test", "test", new Timestamp(System.currentTimeMillis()));

        System.out.println(test.serialize(Helper.SHA512("123")));
    }
}
