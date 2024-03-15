package dbexecutors.sql;

import co.elastic.clients.util.Pair;
import exceptions.AccessDenied;
import exceptions.AlreadyCompleted;
import exceptions.NotFound;
import exceptions.NotValid;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.postgresql.util.PSQLException;

import java.sql.*;
import java.util.HashMap;

import static java.lang.String.format;

public class ChannelsExecutor {
    public static long create (@NotNull Connection conn, long owner, @NotNull String title, boolean isPublic) throws SQLException {
        String sql = """
                    SELECT "channels"."create"(?, ?, ?)
                """;
        PreparedStatement stmt;

        stmt = conn.prepareStatement(sql);
        stmt.setLong(1, owner);
        stmt.setString(2, title);
        stmt.setBoolean(3, isPublic);

        ResultSet rs = stmt.executeQuery( );

        rs.next( );

        return rs.getLong(1);
    }

    public static class Users {
        public static class Presence {
            public static @NotNull @Unmodifiable HashMap<Short, Boolean> getUserFromChannel (@NotNull Connection conn, long user, long channel) throws SQLException, AccessDenied {
                String sqlCheckThePresence = """
                        SELECT EXISTS (
                            SELECT *
                            FROM channels.users
                            WHERE
                                client = ? AND
                                channel = ?
                        )
                        """;
                String sqlGetRightsAndPermissions = """
                        SELECT * FROM channels.select_permissions(?, ?) AS (
                            "permission" SMALLINT,
                            "value" BOOLEAN
                        )
                        """;

                PreparedStatement stmt;
                ResultSet rs;

                stmt = conn.prepareStatement(sqlCheckThePresence);
                stmt.setLong(1, user);
                stmt.setLong(2, channel);

                rs = stmt.executeQuery( );
                rs.next( );

                if (!rs.getBoolean(1)) throw new AccessDenied( );

                stmt = conn.prepareStatement(sqlGetRightsAndPermissions);
                stmt.setLong(1, channel);
                stmt.setLong(2, user);

                rs = stmt.executeQuery( );

                HashMap<Short, Boolean> rights = new HashMap<>( );

                while (rs.next( )) {
                    rights.put(rs.getShort(1), rs.getBoolean(2));
                }

                return rights;
            }

            public static long join (@NotNull Connection conn, long user, @NotNull String channelInvitationURI) throws SQLException, AlreadyCompleted, NotFound {
                String sql = """
                            SELECT channels.join_user(?, ?)
                        """;
                PreparedStatement stmt;

                stmt = conn.prepareStatement(sql);
                stmt.setLong(1, user);
                stmt.setString(2, channelInvitationURI);

                ResultSet rs;
                try {
                    rs = stmt.executeQuery( );
                } catch (PSQLException e) {
                    if (e.getServerErrorMessage( ) == null) throw e;

                    switch (e.getServerErrorMessage( ).getConstraint( )) {
                        case "users_pkey" -> throw new AlreadyCompleted( );
                        case "invitations_check" -> throw new NotFound( );
                        case null, default -> throw e;
                    }
                }

                rs.next( );

                long channel = rs.getLong(1);

                if (channel == 0) throw new NotFound( );
                return channel;
            }

            public static @NotNull Timestamp leave (@NotNull Connection conn, long id, long channel) throws SQLException, NotFound {
                String sql = """
                            SELECT channels.leave_user(?, ?)
                        """;
                PreparedStatement stmt;

                stmt = conn.prepareStatement(sql);
                stmt.setLong(1, id);
                stmt.setLong(2, channel);

                ResultSet rs = stmt.executeQuery( );

                rs.next( );

                Timestamp timestamp = rs.getTimestamp(1);

                if (timestamp == null) {
                    throw new NotFound( );
                } else {
                    return timestamp;
                }
            }
        }
    }

    public static class Invitations {
        public static @NotNull String create (@NotNull Connection conn, long user, long channel, @Nullable Timestamp expiration, @Nullable Integer permittedUses) throws SQLException, AccessDenied {
            String sql = """
                        SELECT channels.create_invitation(?, ?, ?, ?)
                    """;
            PreparedStatement stmt;

            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, user);
            stmt.setLong(2, channel);
            stmt.setTimestamp(3, expiration);
            if (permittedUses == null) {
                stmt.setNull(4, Types.INTEGER);
            } else {
                stmt.setInt(4, permittedUses);
            }

            ResultSet rs = stmt.executeQuery( );

            rs.next( );

            String uri = rs.getString(1);

            if (uri != null) return uri;
            else throw new AccessDenied( );
        }

        public static void delete (@NotNull Connection conn, long user, @NotNull String uri) throws SQLException, NotFound {
            String sql = """
                        SELECT channels.delete_invitation(?, ?)
                    """;
            PreparedStatement stmt;

            stmt = conn.prepareStatement(sql);
            stmt.setString(1, uri);
            stmt.setLong(2, user);

            ResultSet rs = stmt.executeQuery( );

            rs.next( );

            if (!rs.getBoolean(1)) throw new NotFound( );
        }
    }

    public static class Messages {
        public static @NotNull Pair<Long, Timestamp> getNextMessageId (@NotNull Connection conn, @NotNull Long channel) throws SQLException {
            String sql = format("""
                        SELECT
                            nextval('channels.channel_%s_messages_ids_sequence'),
                            TIMEZONE('UTC', now())
                    """, channel);

            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery( );

            rs.next( );

            return new Pair<>(rs.getLong(1), rs.getTimestamp(2));
        }
    }

    public static class Settings {
        public static class External {
            public static void changeTitle (@NotNull Connection conn, long user, long channel, @NotNull String newTitle) throws SQLException, NotFound, NotValid {
                if (newTitle.length( ) > 2 && newTitle.length( ) < 33) throw new NotValid( );

                String sql = """
                            SELECT channels.change_title(%s, %s, %s)
                        """;

                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setLong(1, user);
                stmt.setLong(2, channel);
                stmt.setString(3, newTitle);

                ResultSet rs = stmt.executeQuery( );

                rs.next( );

                if (rs.getBoolean(1)) throw new NotFound( );
            }

            public static void changeDescription (@NotNull Connection conn, long user, long channel, @NotNull String newDescription) throws SQLException, NotFound, NotValid {
                if (newDescription.length( ) < 257) throw new NotValid( );

                String sql = """
                            SELECT channels.change_description(%s, %s, %s)
                        """;

                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setLong(1, user);
                stmt.setLong(2, channel);
                stmt.setString(3, newDescription);

                ResultSet rs = stmt.executeQuery( );

                rs.next( );

                if (rs.getBoolean(1)) throw new NotFound( );
            }
        }
    }
}
