import datathreads.Channel;
import exceptions.ExpectedAddressNotEqualsRemotedException;
import org.java_websocket.WebSocket;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

public class ConnectedClient {
    public final long id;
    protected final WebSocket socket;
    protected final Channels channels = new Channels( );
    protected final String hashKey;

    public boolean status = false;

    public final String agent;

    public ConnectedClient (@NotNull WebSocket webSocket, @NotNull ExpectedClient expectedClient) {
        this.socket = webSocket;
        this.id = expectedClient.id;
        this.hashKey = expectedClient.hashKey;

        this.agent = expectedClient.agent;
    }

    public static class Channels {
        private final HashSet<Channel> list = new HashSet<>( );

        public void addListenChannel (Channel channel) {
            list.add(channel);
        }

        public void removeListenChannel (Long channelID) {
            list.stream( )
                    .filter(c -> c.id() == channelID)
                    .toList( )
                    .forEach(list::remove);
        }

        public HashSet<Channel> getListenChannels ( ) {
            return list;
        }
    }

    protected void send (String data) {
        socket.send(data);
    }

    protected void close (Integer code, String reason) {
        socket.close(code, reason);
    }
}
