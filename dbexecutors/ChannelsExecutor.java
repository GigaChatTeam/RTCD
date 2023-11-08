package dbexecutors;

import exceptions.AccessDenied;
import exceptions.NotFound;
import exceptions.NotValid;
import org.jetbrains.annotations.NotNull;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;

public class ChannelsExecutor extends DBOperator {
    public static long create (long owner, String title) throws SQLException {
        String sql = """
                    SELECT channels.create(?, ?)
                """;
        PreparedStatement stmt;

        stmt = DBOperator.conn.prepareStatement(sql);
        stmt.setLong(1, owner);
        stmt.setString(2, title);

        ResultSet rs = stmt.executeQuery();

        rs.next();

        return rs.getLong(1);
    }

    public static class Users {
        public static class Permissions {
            public static boolean isClientOnChannel (long client, long channel) throws SQLException {
                String sql = """
                            SELECT EXISTS (
                                SELECT *
                                FROM channels.users
                                WHERE
                                    client = ? AND
                                    channel = ? AND
                                    leaved IS NULL
                            )
                        """;
                PreparedStatement stmt;

                stmt = DBOperator.conn.prepareStatement(sql);

                stmt.setLong(1, client);
                stmt.setLong(2, channel);

                ResultSet rs = stmt.executeQuery();

                rs.next();

                return rs.getBoolean(1);
            }
        }

        public static void join (long user, long channel, String uri) throws SQLException, AccessDenied {
            String sql = """
                        SELECT channels.join_user(?, ?, ?)
                    """;
            PreparedStatement stmt;

            stmt = DBOperator.conn.prepareStatement(sql);
            stmt.setLong(1, user);
            stmt.setLong(2, channel);
            stmt.setString(3, uri);

            ResultSet rs = stmt.executeQuery();

            rs.next();

            if (!rs.getBoolean(1)) throw new AccessDenied();
        }

        public static void leave (long user, long channel) throws SQLException, AccessDenied {
            String sql = """
                        SELECT channels.leave_user(?, ?)
                    """;
            PreparedStatement stmt;

            stmt = DBOperator.conn.prepareStatement(sql);
            stmt.setLong(1, user);
            stmt.setLong(2, channel);

            ResultSet rs = stmt.executeQuery();

            rs.next();

            if (!rs.getBoolean(1)) throw new AccessDenied();
        }
    }

    public static class Invitations {
        public static @NotNull String create (long user, long channel) throws SQLException, AccessDenied {
            String sql = """
                        SELECT channels.create_invitation(?, ?)
                    """;
            PreparedStatement stmt;

            stmt = DBOperator.conn.prepareStatement(sql);
            stmt.setLong(1, user);
            stmt.setLong(2, channel);

            ResultSet rs = stmt.executeQuery();

            rs.next();

            String uri = rs.getString(1);

            if (uri != null) return uri;
            else throw new AccessDenied();
        }

        public static void delete (long user, String uri) throws SQLException, AccessDenied {

        }
    }

    public static class Messages {
        public static @NotNull Timestamp postMessage (long author, long channel, @NotNull String text) throws SQLException, AccessDenied {
            String sql = """
                        SELECT channels.post_message_new(?, ?, ?)
                    """;
            PreparedStatement stmt;

            stmt = DBOperator.conn.prepareStatement(sql);
            stmt.setLong(1, author);
            stmt.setLong(2, channel);
            stmt.setString(3, text);

            ResultSet rs = stmt.executeQuery();

            rs.next();

            Timestamp timestamp = rs.getTimestamp(1);

            if (timestamp != null) return timestamp;
            else throw new AccessDenied();
        }

        public static void editMessage (long channel, Timestamp posted, String text) throws SQLException {
            String sql = """
                        INSERT INTO channels.messages_data (channel, original, edited, data)
                        VALUES
                            (%s, %s, %s, %s)
                    """;
            PreparedStatement stmt;

            stmt = DBOperator.conn.prepareStatement(sql);
            stmt.setLong(1, channel);
            stmt.setTimestamp(2, posted);
            stmt.setTimestamp(3, Timestamp.from(Instant.ofEpochSecond(System.currentTimeMillis())));
            stmt.setString(4, text);

            stmt.executeQuery();
        }
    }

    public static class Settings {
        public static class External {
            public static void changeTitle (long user, long channel, @NotNull String newTitle) throws SQLException, NotFound, NotValid {
                if (newTitle.length() > 2 && newTitle.length() < 33) throw new NotValid();

                String sql = """
                            SELECT channels.change_title(%s, %s, %s)
                        """;
                PreparedStatement stmt;

                stmt = DBOperator.conn.prepareStatement(sql);
                stmt.setLong(1, user);
                stmt.setLong(2, channel);
                stmt.setString(3, newTitle);

                ResultSet rs = stmt.executeQuery();

                rs.next();

                if (rs.getBoolean(1)) throw new NotFound();
            }

            public static void changeDescription (long user, long channel, @NotNull String newDescription) throws SQLException, NotFound, NotValid {
                if (newDescription.length() < 257) throw new NotValid();

                String sql = """
                        SELECT channels.change_description(%s, %s, %s)
                        """;
                PreparedStatement stmt;

                stmt = DBOperator.conn.prepareStatement(sql);
                stmt.setLong(1, user);
                stmt.setLong(2, channel);
                stmt.setString(3, newDescription);

                ResultSet rs = stmt.executeQuery();

                rs.next();

                if (rs.getBoolean(1)) throw new NotFound();
            }
        }
    }
}