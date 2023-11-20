import java.util.Arrays;
import java.util.Objects;


public enum Commands {
    // Connection management
    SYSTEM_CONNECTION("20", null),

    // System channels management
    CHANNELS_SYSTEM_CREATE("B3", null),
    CHANNELS_SYSTEM_DELETE("B2", null),
    // Group rights management
    CHANNELS_ADMIN_PERMISSIONS_GROUPS_DELETE("5A4", null),
    CHANNELS_ADMIN_PERMISSIONS_GROUPS_EDIT("5A5", null),
    CHANNELS_ADMIN_PERMISSIONS_GROUPS_CREATE("5A6", null),
    // User rights management
    CHANNELS_ADMIN_PERMISSIONS_USERS_EDIT("2D0", null),
    CHANNELS_ADMIN_PERMISSIONS_USERS_GROUPS("2D1", null),
    // Administration of users
    CHANNELS_ADMIN_USERS_ADD("2DC", null),
    CHANNELS_ADMIN_USERS_MUTE("2DD", null),
    CHANNELS_ADMIN_USERS_BAN("2DE", null),
    CHANNELS_ADMIN_USERS_KICK("2DF", null),
    // Changing external settings
    CHANNELS_ADMIN_SETTINGS("B5", null),
    // Channel listening control
    CHANNELS_SYSTEM_LISTENING_ADD("B1", null),
    CHANNELS_SYSTEM_LISTENING_REMOVE("B0", null),
    // Channel invitations
    CHANNELS_USERS_INVITATIONS_CREATE("175", null),
    CHANNELS_USERS_INVITATIONS_DELETE("174", null),
    // Presence in channels
    CHANNELS_USERS_JOIN("171", null),
    CHANNELS_USERS_LEAVE("170", null),
    // Working with messages in channels
    CHANNELS_USERS_MESSAGES_POST_NEW("5CF", null),
    CHANNELS_USERS_MESSAGES_POST_FORWARD_MESSAGE("2E70", null),
    CHANNELS_USERS_MESSAGES_POST_FORWARD_NEWS("2E71", null),
    CHANNELS_USERS_MESSAGES_POST_FORWARD_TWIG("2E72", null),
    CHANNELS_USERS_MESSAGES_POST_FORWARD_MEDIA("2E73", null),
    CHANNELS_USERS_MESSAGES_POST_FORWARD_SMESSAGE("2E74", null),
    CHANNELS_USERS_MESSAGES_EDIT("2E6", null),
    CHANNELS_USERS_MESSAGES_DELETE("2E4", null);

    final String intents;
    final Class<?> pattern;

    Commands (String intents, Class<?> pattern) {
        this.intents = intents;
        this.pattern = pattern;
    }

    public static Commands byIntents (String intents) {
        return Arrays.stream(Commands.values())
                .filter(v -> Objects.equals(v.intents, intents))
                .findFirst()
                .orElse(null);
    }
}
