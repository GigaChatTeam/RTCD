package dbexecutors.es.patterns;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Timestamp;

public class ChannelsPatterns {
    public static class Users {
        public static class Presence {
            public static class Leave {
                public Long user;
                public Boolean bot;
                public Long channel;
                public String reason;
                public Timestamp timestamp;

                public Leave (@NotNull Long user, @NotNull Long channel, @NotNull Boolean bot, @Nullable String reason, @NotNull Timestamp timestamp) {
                    this.user = user;
                    this.channel = channel;

                    this.bot = bot;
                    this.reason = reason;
                    this.timestamp = timestamp;
                }
            }
        }
    }
}
