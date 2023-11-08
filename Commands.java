import java.util.Arrays;

public enum Commands {
    ADMIN_CHANNELS_CREATE(new String[]{"ADMIN", "CHANNELS", "CREATE"}, CommandsPatterns.Channels.Create.class),
    ADMIN_CHANNELS_DELETE(new String[]{"ADMIN", "CHANNELS", "DELETE"}, CommandsPatterns.Channels.Delete.class),
    ADMIN_CHANNELS_USERS_ADD(new String[]{"ADMIN", "CHANNELS", "USERS", "ADD"}, CommandsPatterns.Channels.Users.Add.class),
    ADMIN_CHANNELS_USERS_REMOVE(new String[]{"ADMIN", "CHANNELS", "USERS", "REMOVE"}, CommandsPatterns.Channels.Users.Remove.class),

    ADMIN_CHANNELS_SETTINGS_EXTERNAL_CHANGE_TITLE(new String[]{"ADMIN", "CHANNELS", "SETTINGS", "EXTERNAL", "CHANGE", "TITLE"}, CommandsPatterns.Channels.Settings.External.Change.Title.class),
    ADMIN_CHANNELS_SETTINGS_EXTERNAL_CHANGE_DESCRIPTION(new String[]{"ADMIN", "CHANNELS", "SETTINGS", "EXTERNAL", "CHANGE", "DESCRIPTION"}, CommandsPatterns.Channels.Settings.External.Change.Description.class),

    USER_CHANNELS_JOIN(new String[]{"USER", "CHANNELS", "JOIN"}, CommandsPatterns.Channels.Users.Join.class),
    USER_CHANNELS_LEAVE(new String[]{"USER", "CHANNELS", "LEAVE"}, CommandsPatterns.Channels.Users.Leave.class),

    USER_CHANNELS_MESSAGES_POST_NEW(new String[]{"USER", "CHANNELS", "MESSAGES", "POST", "NEW"}, CommandsPatterns.Channels.Messages.Post.New.class),
    USER_CHANNELS_MESSAGES_POST_FORWARD_MESSAGE(new String[]{"USER", "CHANNELS", "MESSAGES", "POST", "FORWARD", "MESSAGE"}, CommandsPatterns.Channels.Messages.Post.ForwardMessage.class),
    USER_CHANNELS_MESSAGES_POST_FORWARD_POST(new String[]{"USER", "CHANNELS", "MESSAGES", "POST", "FORWARD", "POST"}, CommandsPatterns.Channels.Messages.Post.ForwardPost.class),
    USER_CHANNELS_MESSAGES_EDIT(new String[]{"USER", "CHANNELS", "MESSAGES", "EDIT"}, CommandsPatterns.Channels.Messages.Edit.class),
    USER_CHANNELS_MESSAGES_DELETE(new String[]{"USER", "CHANNELS", "MESSAGES", "DELETE"}, CommandsPatterns.Channels.Messages.Delete.class),

    USER_CHANNELS_MESSAGES_REACTIONS_ADD(new String[]{"USER", "CHANNELS", "MESSAGES", "REACTIONS", "ADD"}, CommandsPatterns.Channels.Reactions.Add.class),
    USER_CHANNELS_MESSAGES_REACTIONS_REMOVE(new String[]{"USER", "CHANNELS", "MESSAGES", "REACTIONS", "REMOVE"}, CommandsPatterns.Channels.Reactions.Remove.class),

    SYSTEM_CHANNELS_LISTEN_ADD(new String[]{"SYSTEM", "CHANNELS", "LISTEN", "ADD"}, CommandsPatterns.Systems.Listen.Channel.Add.class),
    SYSTEM_CHANNELS_LISTEN_REMOVE(new String[]{"SYSTEM", "CHANNELS", "LISTEN", "REMOVE"}, CommandsPatterns.Systems.Listen.Channel.Remove.class),

    SYSTEM_TTOKENS_CHANNELS_LOAD_MESSAGES_HISTORY(new String[]{"SYSTEM", "TTOKENS", "CHANNELS", "LOAD", "MESSAGES", "HISTORY"}, CommandsPatterns.Systems.TTokens.Channels.Load.MessagesHistory.class);

    final String[] intents;
    final Class<?> pattern;

    Commands (String[] intents, Class<?> pattern) {
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
