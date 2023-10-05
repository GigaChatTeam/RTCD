public class DataCommands {
    static class ChannelCreate implements MessagePostData {
        long owner;
        String title;
    }
    static class ChannelUsersJoin implements MessagePostData {
        long user;
        long channel;
        String invitation;
    }
    static class ChannelMessagesPostNew implements MessagePostData {
        long author;
        long channel;
        String text;
    }
}
