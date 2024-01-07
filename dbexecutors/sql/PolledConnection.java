package dbexecutors.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;

public class PolledConnection {
    public final Connection conn;
    protected final Date created;
    protected long issued;

    public PolledConnection (Connection conn) throws SQLException {
        this.conn = conn;
        this.conn.setAutoCommit(false);
        this.created = new Date(System.currentTimeMillis());
        this.issued = System.currentTimeMillis();
    }

    public void issue () {
        issued = System.currentTimeMillis();
    }

    boolean clearing (long currentTime) throws SQLException {
        if ((currentTime - issued) >= 10000 || conn.isClosed()) {
            conn.close();
            return true;
        }
        return false;
    }

    public void commit () throws SQLException {
        conn.commit();
    }

    public void rollback () throws SQLException {
        conn.rollback();
    }

    @Override
    public int hashCode () {
        return conn.hashCode( );
    }
}
