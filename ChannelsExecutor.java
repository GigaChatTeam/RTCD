import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ChannelsExecutor extends DBOperator {
//    static boolean isClientInChannel () {
//        String sql = """
//            SELECT token
//            FROM public.tokens
//            WHERE
//                client = ?
//        """;
//        PreparedStatement stmt;
//
//        try {
//            stmt = conn.prepareStatement(sql);
//            stmt.setInt(1, id);
//
//            ResultSet rs = stmt.executeQuery();
//            rs.next();
//
//            String token = rs.getString(1);
//            return Helper.verifierBCrypt(user_token, token.getBytes());
//        } catch (SQLException e) {
//            return false;
//        }
//
//        return false;
//    }
}
