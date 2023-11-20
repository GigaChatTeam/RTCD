import java.util.Arrays;
import java.util.Objects;


public enum Commands {
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
    CHANNELS_ADMIN_SETTINGS("B5", null);

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
