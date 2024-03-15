import com.clickhouse.client.ClickHouseException;
import datathreads.Channel;
import org.java_websocket.WebSocket;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;

import static java.lang.System.currentTimeMillis;

class Clients {
    private final HashMap<WebSocket, ConnectedClient> clients = new HashMap<>( );

    public void sendAll (String message) {
        clients.keySet( ).parallelStream( )
                .forEach(client -> client.send(message));
    }

    public void sendCommandToChannel (long channel, String data) {
        clients.values( ).parallelStream( )
                .filter(c -> c.channels.getListenChannels( ).parallelStream( )
                        .anyMatch(v -> v.id( ) == channel))
                .forEach(c -> c.send(data));
    }

    public boolean isUserInChannel (long client, long channel) {
        return clients.values( ).parallelStream( )
                .filter(c -> c.id == client)
                .anyMatch(c -> c.channels.getListenChannels( ).parallelStream( )
                        .anyMatch(v -> v.id( ) == channel));
    }

    public boolean isUserInChannel (WebSocket webSocket, long channel) {
        return clients.values( ).parallelStream( )
                .filter(c -> c.socket == webSocket)
                .anyMatch(c -> c.channels.getListenChannels( ).parallelStream( )
                        .anyMatch(v -> v.id( ) == channel));
    }

    public boolean isUserCanPostToChannel (WebSocket webSocket, long channel) {
        return clients.values( ).parallelStream( )
                .filter(c -> c.socket == webSocket)
                .anyMatch(c -> c.channels.getListenChannels( ).parallelStream( )
                        .anyMatch(v -> v.id( ) == channel && v.validateRule((short) 0)));
    }

    public boolean isUserConnected (WebSocket webSocket) {
        return clients.containsKey(webSocket);
    }

    public synchronized void addClient (ConnectedClient client) {
        clients.put(client.socket, client);

        try {
            dbexecutors.Users.Login.login(
                    client.id,
                    Timestamp.from(Instant.ofEpochMilli(currentTimeMillis( ))),
                    true,
                    false,
                    client.agent,
                    client.socket.getRemoteSocketAddress( ));
        } catch (ClickHouseException e) {
            e.printStackTrace( );
        }
    }

    public ConnectedClient getClient (WebSocket socket) {
        return clients.get(socket);
    }

    public Boolean isClientConnected (long client) {
        return clients.values( ).stream( )
                .anyMatch(c -> c.id == client);
    }

    public void removeClient (WebSocket socket, Integer code, String reason, Boolean forced, Boolean error) {
        ConnectedClient client = clients.remove(socket);
        client.close(code, reason);

        try {
            dbexecutors.Users.Login.logout(
                    client.id,
                    Timestamp.from(Instant.ofEpochMilli(currentTimeMillis( ))),
                    !forced,
                    error,
                    client.agent,
                    client.socket.getRemoteSocketAddress( ));
        } catch (ClickHouseException e) {
            e.printStackTrace( );
        }
    }

    public void addListeningClientToChannel (WebSocket socket, Channel channelObj) {
        clients.values( ).parallelStream( )
                .filter(c -> c.socket == socket)
                .forEach(c -> c.channels.addListenChannel(channelObj));
    }

    public void addListeningClientToChannel (Long client, Channel channelObj) {
        clients.values( ).parallelStream( )
                .filter(c -> c.id == client)
                .forEach(c -> c.channels.addListenChannel(channelObj));
    }

    public void removeListeningClientFromChannel (WebSocket socket, long channel) {
        clients.values( ).parallelStream( )
                .filter(c -> c.socket == socket)
                .forEach(c -> c.channels.removeListenChannel(channel));
    }

    public void removeListeningClientFromChannel (long client, long channel) {
        clients.values( ).parallelStream( )
                .filter(c -> c.id == client)
                .forEach(c -> c.channels.removeListenChannel(channel));
    }

    public long getID (WebSocket webSocket) {
        return clients.get(webSocket).id;
    }

    public void changeClientConnectionStatus (WebSocket webSocket, boolean status) {
        final ConnectedClient client = clients.get(webSocket);

        client.status = status;

        if (status) {
            try {
                dbexecutors.Users.Login.login(
                        client.id,
                        Timestamp.from(Instant.ofEpochMilli(currentTimeMillis( ))),
                        true,
                        false,
                        client.agent,
                        client.socket.getRemoteSocketAddress( ));
            } catch (ClickHouseException e) {
                e.printStackTrace( );
            }
        }
    }

    public boolean getClientConnectionStatus (WebSocket webSocket) {
        return clients.get(webSocket).status;
    }

    public void closeAllClients (int code, String reason) throws SQLException {
        Timestamp timestamp = Timestamp.from(Instant.ofEpochMilli(currentTimeMillis( )));

        synchronized (clients) {
            clients.values( ).parallelStream( )
                    .forEach(client -> {
                        try {
                            client.close(code, reason);
                            dbexecutors.Users.Login.logout(
                                    client.id,
                                    timestamp,
                                    true,
                                    false,
                                    client.agent,
                                    client.socket.getRemoteSocketAddress( ));
                        } catch (ClickHouseException e) {
                            e.printStackTrace( );
                        }
                    });
        }
    }

    public HashMap<WebSocket, ConnectedClient> getClients ( ) {
        return clients;
    }
}
