package dbexecutors.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;

public class PolledConnection {
    public final Connection conn;
    protected final Date created;
    protected long issued;

    public PolledConnection (Connection conn) {
        this.conn = conn;
        this.created = new Date(System.currentTimeMillis());
        this.issued = System.currentTimeMillis();
    }

    public void issue () {
        issued = System.currentTimeMillis();
    }

    boolean clearing (long currentTime) throws SQLException {
        if ((issued - currentTime) >= 20000000) {
            conn.close();
            return true;
        }
        return false;
    }
}
