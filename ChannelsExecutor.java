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
    static long create (long owner, String title) throws SQLException {
        String sql = """
                    SELECT channels.create(?, ?)
                """;
        PreparedStatement stmt;

        stmt = conn.prepareStatement(sql);
        stmt.setLong(1, owner);
        stmt.setString(2, title);

        ResultSet rs = stmt.executeQuery();

        rs.next();

        return rs.getLong(1);
    }

    static class Users {
        static class Permissions {
            static boolean isClientOnChannel (long client, long channel) throws SQLException {
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

                stmt = conn.prepareStatement(sql);

                stmt.setLong(1, client);
                stmt.setLong(2, channel);

                ResultSet rs = stmt.executeQuery();

                rs.next();

                return rs.getBoolean(1);
            }
        }

        static void join (long user, long channel, String uri) throws SQLException, AccessDenied {
            String sql = """
                        SELECT channels.join_user(?, ?, ?)
                    """;
            PreparedStatement stmt;

            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, user);
            stmt.setLong(2, channel);
            stmt.setString(3, uri);

            ResultSet rs = stmt.executeQuery();

            rs.next();

            if (!rs.getBoolean(1)) throw new AccessDenied();
        }

        static void leave (long user, long channel) throws SQLException, AccessDenied {
            String sql = """
                        SELECT channels.leave_user(?, ?)
                    """;
            PreparedStatement stmt;

            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, user);
            stmt.setLong(2, channel);

            ResultSet rs = stmt.executeQuery();

            rs.next();

            if (!rs.getBoolean(1)) throw new AccessDenied();
        }
    }

    static class Invitations {
        static @NotNull String create (long user, long channel) throws SQLException, AccessDenied {
            String sql = """
                        SELECT channels.create_invitation(?, ?)
                    """;
            PreparedStatement stmt;

            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, user);
            stmt.setLong(2, channel);

            ResultSet rs = stmt.executeQuery();

            rs.next();

            String uri = rs.getString(1);

            if (uri != null) return uri;
            else throw new AccessDenied();
        }

        static void delete (long user, String uri) throws SQLException, AccessDenied {

        }
    }

    static class Messages {
        static Timestamp postMessage (long author, long channel, @NotNull String text) throws SQLException {
            String sql = """
                        SELECT channels.post_message_new(?, ?, ?)
                    """;
            PreparedStatement stmt;

            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, author);
            stmt.setLong(2, channel);
            stmt.setString(3, text);

            ResultSet rs = stmt.executeQuery();

            rs.next();

            return rs.getTimestamp(1);
        }

        static void editMessage (CommandsPatterns.Channels.Messages.@NotNull Edit command) throws SQLException {
            String sql = """
                        INSERT INTO channels.messages_data (channel, original, edited, data)
                        VALUES
                            (%s, %s, %s, %s)
                    """;
            PreparedStatement stmt;

            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, command.channel);
            stmt.setTimestamp(2, command.posted);
            stmt.setTimestamp(3, Timestamp.from(Instant.ofEpochSecond(System.currentTimeMillis())));
            stmt.setString(4, command.text);

            stmt.executeQuery();
        }

        static void editMessage (long channel, Timestamp posted, String text) throws SQLException {
            String sql = """
                        INSERT INTO channels.messages_data (channel, original, edited, data)
                        VALUES
                            (%s, %s, %s, %s)
                    """;
            PreparedStatement stmt;

            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, channel);
            stmt.setTimestamp(2, posted);
            stmt.setTimestamp(3, Timestamp.from(Instant.ofEpochSecond(System.currentTimeMillis())));
            stmt.setString(4, text);

            stmt.executeQuery();
        }
    }

    static class Settings {
        static class External {
            static void changeTitle (long channel, @NotNull String newTitle) throws SQLException, NotFound.Channel, NotValid.Data {
                if (newTitle.length() > 2 && newTitle.length() < 33) throw new NotValid.Data();

                String sql = """
                            UPDATE channels."index"
                            SET
                                title = %s
                            WHERE
                                id = %s
                        """;
                PreparedStatement stmt;

                stmt = conn.prepareStatement(sql);
                stmt.setString(1, newTitle);
                stmt.setLong(2, channel);

                ResultSet rs = stmt.executeQuery();

                rs.next();

                if (rs.getByte(1) == 0) throw new NotFound.Channel();
            }

            static void changeDescription (long channel, @NotNull String newDescription) throws SQLException, NotFound.Channel, NotValid.Data {
                if (newDescription.length() < 257) throw new NotValid.Data();

                String sql = """
                        UPDATE channels."index"
                        SET
                            description = %s
                        WHERE
                            id = %s
                        """;
                PreparedStatement stmt;

                stmt = conn.prepareStatement(sql);
                stmt.setString(1, newDescription);
                stmt.setLong(2, channel);

                ResultSet rs = stmt.executeQuery();

                rs.next();

                if (rs.getByte(1) == 0) throw new NotFound.Channel();
            }
        }
    }
}
