import java.util.Arrays;

public enum Commands {
    ADMIN_CHANNELS_CREATE(new String[]{"ADMIN", "CHANNELS", "CREATE"}, DataCommands.ChannelCreate.class),
    ADMIN_CHANNELS_DELETE(new String[]{"ADMIN", "CHANNELS", "DELETE"}, null),
    ADMIN_CHANNELS_USERS_APPEND(new String[]{"ADMIN", "CHANNELS", "USERS", "APPEND"}, null),
    ADMIN_CHANNELS_USERS_REMOVE(new String[]{"ADMIN", "CHANNELS", "USERS", "REMOVE"}, null),

    USER_CHANNELS_JOIN(new String[]{"ADMIN", "CHANNELS", "JOIN"}, DataCommands.ChannelUsersJoin.class),

    USER_CHANNELS_MESSAGES_POST_NEW(new String[]{"USER", "CHANNELS", "MESSAGES", "POST", "NEW"}, DataCommands.ChannelMessagesPostNew.class),
    USER_CHANNELS_MESSAGES_POST_FORWARD(new String[]{"USER", "CHANNELS", "MESSAGES", "POST", "FORWARD"}, null),
    USER_CHANNELS_MESSAGES_EDIT(new String[]{"USER", "CHANNELS", "MESSAGES", "EDIT"}, null),
    USER_CHANNELS_MESSAGES_DELETE(new String[]{"USER", "CHANNELS", "MESSAGES", "DELETE"}, null);

    private final String[] intents;
    final Class pattern;

    private Commands (String[] intents, Class pattern) {
        this.intents = intents;
        this.pattern = pattern;
    }

    public static Commands byIntents (String[] intents) {
        return Arrays.stream(Commands.values())
            .filter(v -> Arrays.equals(v.intents, intents))
            .findFirst()
            .orElse(null);
    }
}
