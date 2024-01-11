package dbexecutors.es;

import dbexecutors.es.patterns.ChannelsPatterns;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.sql.Timestamp;

public class ChannelsExecutor extends ESAdapter {
    public static class Users {
        public static class Presence {
            public static void leave (long channel, long user, @Nullable String reason, @NotNull Timestamp timestamp) throws IOException {
                esClient.index(i -> i
                    .index("channels")
                    .document(new ChannelsPatterns.Users.Presence.Leave(channel, user, false, reason, timestamp))
                );
            }
        }
    }
}
