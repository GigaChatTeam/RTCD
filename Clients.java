import org.java_websocket.WebSocket;

import java.util.HashMap;

class Clients {
    private final HashMap<WebSocket, ConnectedClient> clients = new HashMap<>( );

    public void sendAll (String message) {
        clients.keySet( ).parallelStream( )
                .forEach(client -> client.send(message));
    }

    public void sendCommandToChannel (long channel, String data) {
        clients.values( ).parallelStream( )
                .filter(c -> c.channels.parallelStream( )
                        .anyMatch(v -> v.id == channel))
                .forEach(c -> c.send(data));
    }

    public boolean isUserInChannel (long client, long channel) {
        return clients.values( ).parallelStream( )
                .filter(c -> c.id == client)
                .anyMatch(c -> c.channels.parallelStream( )
                        .anyMatch(v -> v.id == channel));
    }

    public boolean isUserInChannel (WebSocket webSocket, long channel) {
        return clients.values( ).parallelStream( )
                .filter(c -> c.socket == webSocket)
                .anyMatch(c -> c.channels.parallelStream( )
                        .anyMatch(v -> v.id == channel));
    }

    public boolean isUserCanPostToChannel (WebSocket webSocket, long channel) {
        return clients.values( ).parallelStream( )
                .filter(c -> c.socket == webSocket)
                .anyMatch(c -> c.channels.parallelStream( )
                        .anyMatch(v -> v.id == channel && v.canPost));
    }

    public boolean isUserConnected (WebSocket webSocket) {
        return clients.containsKey(webSocket);
    }

    public void addClient (ConnectedClient client) {
        clients.put(client.socket, client);
    }

    public ConnectedClient getClient (WebSocket socket) {
        return clients.get(socket);
    }

    public Boolean isClientConnected (long client) {
        return clients.values( ).stream( )
                .anyMatch(c -> c.id == client);
    }

    public void removeClient (WebSocket socket) {
        clients.remove(socket);
    }

    public void addListeningClientToChannel (WebSocket socket, long channel, boolean canPost) {
        clients.values( ).parallelStream( )
                .filter(c -> c.socket == socket)
                .forEach(c -> c.addListenChannel(channel, canPost));
    }

    public void addListeningClientToChannel (WebSocket socket, long channel) {
        clients.values( ).parallelStream( )
                .filter(c -> c.socket == socket)
                .forEach(c -> c.addListenChannel(channel));
    }

    public void addListeningClientToChannel (long client, long channel, boolean canPost) {
        clients.values( ).parallelStream( )
                .filter(c -> c.id == client)
                .forEach(c -> c.addListenChannel(channel, canPost));
    }

    public void addListeningClientToChannel (long client, long channel) {
        clients.values( ).parallelStream( )
                .filter(c -> c.id == client)
                .forEach(c -> c.addListenChannel(channel));
    }

    public void removeListeningClientFromChannel (WebSocket socket, long channel) {
        clients.values( ).parallelStream( )
                .filter(c -> c.socket == socket)
                .forEach(c -> c.removeListenChannel(channel));
    }

    public void removeListeningClientFromChannel (long client, long channel) {
        clients.values( ).parallelStream( )
                .filter(c -> c.id == client)
                .forEach(c -> c.removeListenChannel(channel));
    }

    public long getID (WebSocket webSocket) {
        return clients.values( ).parallelStream( )
                .filter(client -> client.socket == webSocket)
                .map(client -> client.id)
                .findFirst( )
                .orElse(-1L);
    }

    public void changeClientConnectionStatus (WebSocket webSocket, boolean status) {
        clients.get(webSocket).status = status;
    }

    public boolean getClientConnectionStatus (WebSocket webSocket) {
        return clients.get(webSocket).status;
    }

    public HashMap<WebSocket, ConnectedClient> getClients () {
        return clients;
    }
}
