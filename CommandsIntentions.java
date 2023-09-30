public class CommandsIntentions {
    static final Command ADMIN_CHANNELS_CREATE = new Command(new String[]{"ADMIN", "CHANNELS", "CREATE"}, DataCommands.ChannelCreate.class);
    static Command ADMIN_CHANNELS_USERS_JOIN = new Command(new String[]{"ADMIN", "CHANNELS", "USERS", "JOIN"}, DataCommands.ChannelUsersJoin.class);
    static Command ADMIN_CHANNELS_USERS_APPEND = null; // TODO
    static Command USER_CHANNELS_MESSAGES_POST_NEW = new Command(new String[]{"USER", "CHANNELS", "MESSAGES", "POST", "NEW"}, DataCommands.ChannelMessagesPostNew.class);
}
