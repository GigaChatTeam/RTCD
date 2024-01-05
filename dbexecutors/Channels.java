package dbexecutors;

import dbexecutors.sql.ChannelsExecutor;
import exceptions.AccessDenied;
import exceptions.AlreadyCompleted;
import exceptions.NotFound;
import exceptions.NotValid;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;

public class Channels {
    public static long create (long owner, @NotNull String title) throws SQLException {
        return ChannelsExecutor.create(owner, title);
    }

    public static class Users {
        public static class Presence {
            public static boolean isClientOnChannel (long client, long channel) throws SQLException {
                return ChannelsExecutor.Users.Presence.isClientOnChannel(client, channel);
            }

            public static long join (long user, @NotNull String channelInvitationURI) throws SQLException, AlreadyCompleted, NotFound {
                return ChannelsExecutor.Users.Presence.join(user, channelInvitationURI);
            }

            public static void leave (long id, long channel) throws SQLException, NotFound {
                ChannelsExecutor.Users.Presence.leave(id, channel);
            }
        }
    }

    public static class Invitations {
        public static @NotNull String create (long user, long channel, @Nullable Timestamp expiration, @Nullable Integer permittedUses) throws SQLException, AccessDenied {
            return ChannelsExecutor.Invitations.create(user, channel, expiration, permittedUses);
        }

        public static void delete (long user, @NotNull String uri) throws SQLException, NotFound {
            ChannelsExecutor.Invitations.delete(user, uri);
        }
    }

    public static class Messages {
        public static Timestamp postTextMessage (long author, long channel, @Nullable UUID alias, @NotNull String text, @Nullable Long[][] media, @Nullable Long[] files) throws SQLException {
            return ChannelsExecutor.Messages.postTextMessage(author, channel, alias, text, media, files);
        }
    }

    public static class Settings {
        public static class External {
            public static void changeTitle (long user, long channel, @NotNull String newTitle) throws SQLException, NotFound, NotValid {
                ChannelsExecutor.Settings.External.changeTitle(user, channel, newTitle);
            }

            public static void changeDescription (long user, long channel, @NotNull String newDescription) throws SQLException, NotFound, NotValid {
                ChannelsExecutor.Settings.External.changeTitle(user, channel, newDescription);
            }
        }
    }
}
