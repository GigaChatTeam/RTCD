package dbexecutors.sql;

import org.jetbrains.annotations.NotNull;

import java.sql.Connection;

public class PermissionOperator {
    public static boolean validateToken (@NotNull Connection conn, long id, @NotNull String secret, @NotNull String key) {
        return true;

//        String sql = """
//                    SELECT
//                        secret,
//                        key
//                    FROM users.tokens
//                    WHERE
//                        client = ?
//                """;
//        PreparedStatement stmt;
//
//        try {
//            stmt = conn.prepareStatement(sql);
//            stmt.setLong(1, id);
//
//            ResultSet rs = stmt.executeQuery( );
//
//            while (rs.next( )) {
//                if (
//                        verifierBCrypt(secret, rs.getString(1).getBytes( )) &&
//                        verifierBCrypt(key, rs.getString(2).getBytes( ))
//                ) return true;
//            }
//            return false;
//        } catch (SQLException e) {
//            e.printStackTrace( );
//            return false;
//        }
    }
}
