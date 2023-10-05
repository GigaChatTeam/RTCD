import java.util.Arrays;

public enum Commands {
    ADMIN_CHANNELS_CREATE(new String[]{"ADMIN", "CHANNELS", "CREATE"}, DataCommands.ChannelCreate.class),
    ADMIN_CHANNELS_USERS_JOIN(new String[]{"ADMIN", "CHANNELS", "USERS", "JOIN"}, DataCommands.ChannelUsersJoin.class),
    ADMIN_CHANNELS_USERS_APPEND(new String[0], null),
    USER_CHANNELS_MESSAGES_POST_NEW(new String[]{"USER", "CHANNELS", "MESSAGES", "POST", "NEW"}, DataCommands.ChannelMessagesPostNew.class);

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
