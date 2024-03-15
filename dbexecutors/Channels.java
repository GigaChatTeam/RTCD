package dbexecutors;

import co.elastic.clients.util.Pair;
import com.clickhouse.client.ClickHouseClient;
import com.clickhouse.client.ClickHouseException;
import com.clickhouse.client.ClickHouseProtocol;
import dbexecutors.sql.ChannelsExecutor;
import exceptions.AccessDenied;
import exceptions.AlreadyCompleted;
import exceptions.NotFound;
import exceptions.NotValid;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.UUID;

import static dbexecutors.sql.PoolController.getConnection;

public class Channels {
    static ClickHouseClient client = ClickHouseClient.newInstance(ClickHouseProtocol.HTTP);

    public static long create (long owner, @NotNull String title, boolean isPublic) throws SQLException, ClickHouseException {
        Connection connection = getConnection( );

        try {
            Long channelID = ChannelsExecutor.create(connection, owner, title, isPublic);

            Pair<Long, Timestamp> metaForNotify = dbexecutors.sql.ChannelsExecutor.Messages.getNextMessageId(connection, channelID);
            Pair<Long, Timestamp> metaForOwnerJoin = dbexecutors.sql.ChannelsExecutor.Messages.getNextMessageId(connection, channelID);

            ClickHouseClient client = ClickHouseClient.newInstance(ClickHouseProtocol.HTTP);

            dbexecutors.ch.ChannelsExecutor.Messages.postNewTextMessage(
                    client,
                    channelID,
                    metaForNotify.key( ),
                    metaForNotify.value( ),
                    0L,
                    MetaTextData.CHANNELS_SYSTEM_CREATE.data,
                    new Long[0],
                    new Long[0][0],
                    dbexecutors.patterns.Channels.Messages.ForwardBy.NO_FORWARD);
            dbexecutors.ch.ChannelsExecutor.Messages.postNewTextMessage(
                    client,
                    channelID,
                    metaForOwnerJoin.key( ),
                    metaForOwnerJoin.value( ),
                    owner,
                    MetaTextData.CHANNELS_USERS_JOIN.data,
                    new Long[0],
                    new Long[0][0],
                    dbexecutors.patterns.Channels.Messages.ForwardBy.NO_FORWARD);

            return channelID;
        } catch (SQLException e) {
            connection.rollback( );
            throw e;
        } finally {
            connection.commit( );
            connection.close( );
        }
    }

    public static class Users {
        public static class Presence {
            public static @NotNull @Unmodifiable HashMap<Short, Boolean> getUserFromChannel (long client, long channel) throws SQLException, AccessDenied, InterruptedException {
                Connection connection = getConnection( );

                try {
                    return ChannelsExecutor.Users.Presence.getUserFromChannel(connection, client, channel);
                } catch (SQLException e) {
                    connection.rollback( );
                    throw e;
                } finally {
                    connection.commit( );
                    connection.close( );
                }
            }

            public static long join (long user, @NotNull String channelInvitationURI) throws SQLException, AlreadyCompleted, NotFound {
                Connection connection = getConnection( );

                try {
                    return ChannelsExecutor.Users.Presence.join(connection, user, channelInvitationURI);
                } catch (SQLException e) {
                    connection.rollback( );
                    throw e;
                } finally {
                    connection.commit( );
                    connection.close( );
                }
            }

            public static void leave (long id, long channel, @Nullable String reason) throws SQLException, IOException, NotFound {
                Connection connection = getConnection( );

                try {
                    dbexecutors.es.ChannelsExecutor.Users.Presence.leave(
                            id, channel, reason,
                            dbexecutors.sql.ChannelsExecutor.Users.Presence.leave(connection, id, channel));
                } catch (SQLException | IOException e) {
                    connection.rollback( );
                    throw e;
                } finally {
                    connection.commit( );
                    connection.close( );
                }
            }
        }
    }

    public static class Invitations {
        public static @NotNull String create (long user, long channel, @Nullable Timestamp expiration, @Nullable Integer permittedUses) throws SQLException, AccessDenied {
            Connection connection = getConnection( );

            try {
                return ChannelsExecutor.Invitations.create(connection, user, channel, expiration, permittedUses);
            } catch (SQLException e) {
                connection.rollback( );
                throw e;
            } finally {
                connection.commit( );
                connection.close( );
            }
        }

        public static void delete (long user, @NotNull String uri) throws SQLException, NotFound {
            Connection connection = getConnection( );

            try {
                ChannelsExecutor.Invitations.delete(connection, user, uri);
            } catch (SQLException e) {
                connection.rollback( );
                throw e;
            } finally {
                connection.commit( );
                connection.close( );
            }
        }
    }

    public static class Messages {
        public static @NotNull Pair<Long, Timestamp> postTextMessage (long author, long channel, @Nullable UUID alias, @NotNull String text, @Nullable Long @NotNull [] @NotNull [] media, @Nullable Long[] files) throws SQLException, ClickHouseException {
            Connection connection = getConnection( );

            Pair<Long, Timestamp> messageMeta;
            dbexecutors.patterns.Channels.Messages.Message messageData;

            try {
                messageMeta = dbexecutors.sql.ChannelsExecutor.Messages.getNextMessageId(connection, channel);
            } catch (SQLException e) {
                connection.rollback( );
                throw e;
            } finally {
                connection.commit( );
                connection.close( );
            }

//            messageData = new dbexecutors.patterns.Channels.Messages.Message(
//                    channel,
//                    messageMeta.key( ),
//                    messageMeta.value( ),
//                    author,
//                    text,
//                    files,
//                    media,
//                    dbexecutors.patterns.Channels.Messages.ForwardBy.NO_FORWARD);

            dbexecutors.ch.ChannelsExecutor.Messages.postNewTextMessage(
                    ClickHouseClient.newInstance(ClickHouseProtocol.HTTP),
                    channel,
                    messageMeta.key( ),
                    messageMeta.value( ),
                    author,
                    text,
                    files,
                    media,
                    dbexecutors.patterns.Channels.Messages.ForwardBy.NO_FORWARD);

            return messageMeta;
        }

        public static class Settings {
            public static class External {
                public static void changeTitle (long user, long channel, @NotNull String newTitle) throws SQLException, NotFound, NotValid {
                    Connection connection = getConnection( );

                    try {
                        ChannelsExecutor.Settings.External.changeTitle(connection, user, channel, newTitle);
                    } catch (SQLException e) {
                        connection.rollback( );
                        throw e;
                    } finally {
                        connection.commit( );
                        connection.close( );
                    }
                }

                public static void changeDescription (long user, long channel, @NotNull String newDescription) throws SQLException, NotFound, NotValid {
                    Connection connection = getConnection( );

                    try {
                        ChannelsExecutor.Settings.External.changeTitle(connection, user, channel, newDescription);
                    } catch (SQLException e) {
                        connection.rollback( );
                        throw e;
                    } finally {
                        connection.commit( );
                        connection.close( );
                    }
                }
            }
        }
    }

    enum MetaTextData {
        CHANNELS_SYSTEM_CREATE("@channels/system/create"),
        CHANNELS_USERS_JOIN("@channels/users/join");
        final String data;

        MetaTextData (String data) {
            this.data = data;
        }
    }
}
