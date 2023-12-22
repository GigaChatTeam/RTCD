import DataThreads.Channel;
import org.java_websocket.WebSocket;

import java.sql.SQLException;
import java.util.HashSet;

import static dbexecutors.SystemExecutor.logExit;

public class ConnectedClient {
    public final long id;
    protected final WebSocket socket;
    protected final HashSet<Channel> channels = new HashSet<>( );
    protected final String key;

    public boolean status = false;

    public ConnectedClient (WebSocket sock, long id, String key) {
        this.socket = sock;
        this.id = id;
        this.key = key;
    }

    public void addListenChannel (long channel, boolean canPost) {
        channels.add(new Channel(
                channel, canPost
        ));
    }

    public void addListenChannel (long channel) {
        channels.add(new Channel(
                channel, false
        ));
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
        socket.close(code, reason);
        logExit(id, key);
    }
}
