package dbexecutors;

import dbexecutors.sql.ChannelsExecutor;
import dbexecutors.sql.PolledConnection;
import exceptions.AccessDenied;
import exceptions.AlreadyCompleted;
import exceptions.NotFound;
import exceptions.NotValid;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;

import static dbexecutors.sql.PoolController.getConnection;
import static dbexecutors.sql.PoolController.returnConnection;

public class Channels {
    public static long create (long owner, @NotNull String title) throws SQLException {
        PolledConnection connection = getConnection( );

        try {
            return ChannelsExecutor.create(connection.conn, owner, title);
        } catch (SQLException e) {
            connection.rollback( );
            throw e;
        } finally {
            connection.commit( );
            returnConnection(connection);
        }
    }

    public static class Users {
        public static class Presence {
            public static boolean isClientOnChannel (long client, long channel) throws SQLException {
                PolledConnection connection = getConnection( );

                try {
                    return ChannelsExecutor.Users.Presence.isClientOnChannel(connection.conn, client, channel);
                } catch (SQLException e) {
                    connection.rollback( );
                    throw e;
                } finally {
                    connection.commit( );
                    returnConnection(connection);
                }
            }

            public static long join (long user, @NotNull String channelInvitationURI) throws SQLException, AlreadyCompleted, NotFound {
                PolledConnection connection = getConnection( );

                try {
                    return ChannelsExecutor.Users.Presence.join(connection.conn, user, channelInvitationURI);
                } catch (SQLException e) {
                    connection.rollback( );
                    throw e;
                } finally {
                    connection.commit( );
                    returnConnection(connection);
                }
            }

            public static void leave (long id, long channel, @Nullable String reason) throws SQLException, IOException, NotFound {
                PolledConnection connection = getConnection( );

                try {
                    dbexecutors.es.ChannelsExecutor.Users.Presence.leave(
                            id, channel, reason,
                            dbexecutors.sql.ChannelsExecutor.Users.Presence.leave(connection.conn, id, channel));
                } catch (SQLException | IOException e) {
                    connection.rollback( );
                    throw e;
                } finally {
                    connection.commit( );
                    returnConnection(connection);
                }
            }
        }
    }

    public static class Invitations {
        public static @NotNull String create (long user, long channel, @Nullable Timestamp expiration, @Nullable Integer permittedUses) throws SQLException, AccessDenied {
            PolledConnection connection = getConnection( );

            try {
                return ChannelsExecutor.Invitations.create(connection.conn, user, channel, expiration, permittedUses);
            } catch (SQLException e) {
                connection.rollback( );
                throw e;
            } finally {
                connection.commit( );
                returnConnection(connection);
            }
        }

        public static void delete (long user, @NotNull String uri) throws SQLException, NotFound {
            PolledConnection connection = getConnection( );

            try {
                ChannelsExecutor.Invitations.delete(connection.conn, user, uri);
            } catch (SQLException e) {
                connection.rollback( );
                throw e;
            } finally {
                connection.commit( );
                returnConnection(connection);
            }
        }
    }

    public static class Messages {
        public static Timestamp postTextMessage (long author, long channel, @Nullable UUID alias, @NotNull String text, @Nullable Long[][] media, @Nullable Long[] files) throws SQLException {
            PolledConnection connection = getConnection( );

            try {
                return ChannelsExecutor.Messages.postTextMessage(connection.conn, author, channel, alias, text, media, files);
            } catch (SQLException e) {
                connection.rollback( );
                throw e;
            } finally {
                connection.commit( );
                returnConnection(connection);
            }
        }
    }

    public static class Settings {
        public static class External {
            public static void changeTitle (long user, long channel, @NotNull String newTitle) throws SQLException, NotFound, NotValid {
                PolledConnection connection = getConnection( );

                try {
                    ChannelsExecutor.Settings.External.changeTitle(connection.conn, user, channel, newTitle);
                } catch (SQLException e) {
                    connection.rollback( );
                    throw e;
                } finally {
                    connection.commit( );
                    returnConnection(connection);
                }
            }

            public static void changeDescription (long user, long channel, @NotNull String newDescription) throws SQLException, NotFound, NotValid {
                PolledConnection connection = getConnection( );

                try {
                    ChannelsExecutor.Settings.External.changeTitle(connection.conn, user, channel, newDescription);
                } catch (SQLException e) {
                    connection.rollback( );
                    throw e;
                } finally {
                    connection.commit( );
                    returnConnection(connection);
                }
            }
        }
    }
}
