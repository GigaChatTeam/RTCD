public class Commands {
    private static class Message {
        public String type;
        public int id = 0;
        public int channel;
        public int author;
    }
    public static class MessagePost extends Message {
        public String type = "MESSAGE-POST";
        public String text;
    }
    public static class MessageDelete extends Message {
        public String type = "MESSAGE-DELETE";
    }

    private static class ChannelControl {
        public int id = 0;
        public String title;
    }
}
