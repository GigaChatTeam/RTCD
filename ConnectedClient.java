import DataThreads.Channel;
import dbexecutors.sql.PolledConnection;
import dbexecutors.sql.PoolController;
import org.java_websocket.WebSocket;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.HashSet;

import static dbexecutors.sql.PoolController.returnConnection;
import static dbexecutors.sql.SystemExecutor.logAuthentication;
import static dbexecutors.sql.SystemExecutor.logExit;

public class ConnectedClient {
    public final long id;
    protected final WebSocket socket;
    protected final HashSet<Channel> channels = new HashSet<>( );
    protected final String key;

    public boolean status = false;

    public ConnectedClient (@NotNull WebSocket webSocket, @NotNull ExpectedClient expectedClient) throws SQLException {
        PolledConnection connection = PoolController.getConnection( );

        try {
            logAuthentication(
                    connection.conn,
                    expectedClient.id,
                    expectedClient.key,
                    expectedClient.agent);
        } finally {
            returnConnection(connection);
        }

        this.socket = webSocket;
        this.id = expectedClient.id;
        this.key = expectedClient.key;
    }

    public void addListenChannel (Channel channelObj) {
        channels.add(channelObj);
    }

    public void removeListenChannel (long channel) {
        channels.remove(
                channels.stream( )
                        .filter(c -> c.id == channel)
                        .findFirst( )
                        .orElse(null)
        );
    }

    protected void send (String data) {
        socket.send(data);
    }

    protected void close (int code, String reason) throws SQLException {
        PolledConnection dbConnection = PoolController.getConnection();

        socket.close(code, reason);
        try {
            logExit(dbConnection.conn, id, key);
        } finally {
            returnConnection(dbConnection);
        }
    }

    protected void close (@NotNull PolledConnection dbConnection, int code, String reason) throws SQLException {
        socket.close(code, reason);

        logExit(dbConnection.conn, id, key);
    }
}
