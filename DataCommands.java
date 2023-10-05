public class DataCommands {
    static class ChannelCreate {
        long owner;
        String title;
    }
    static class ChannelUsersJoin {
        long user;
        long channel;
        String invitation;
    }
    static class ChannelMessagesPostNew {
        long author;
        long channel;
        String text;
    }
}
