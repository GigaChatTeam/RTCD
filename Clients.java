import org.java_websocket.WebSocket;

import java.util.HashMap;

class Clients {
    private final HashMap<WebSocket, Client> clients = new HashMap<>();

    public void sendAll(String message) {
        clients.keySet().parallelStream()
                .forEach(client -> client.send(message));
    }

    public void sendCommandToChannel (long channel, String data) {
        clients.values().parallelStream()
                .filter(c -> c.getChannels().contains(channel))
                .forEach(c -> c.send(data));
    }

    public Boolean userIsConnected(long channel) {
        return null;
    }

    public void addClient (Client client) {
        clients.put(client.socket, client);
    }
    public Boolean isClientConnected (long client) {
        return clients.values().stream()
            .anyMatch(c -> c.id == client);
    }
    public void removeClient (WebSocket socket) {
        clients.remove(socket);
    }

    public void joinClientToChannel (WebSocket socket, long channel) {
        clients.values().parallelStream()
                .filter(c -> c.socket == socket)
                .forEach(c -> c.addListen(channel));
    }
    public void LeaveClientFromChannel (WebSocket socket, long channel) {
        clients.values().parallelStream()
                .filter(c -> c.socket == socket)
                .forEach(c -> c.removeListen(channel));
    }

    public long getID (WebSocket webSocket) {
        for (Client client : clients.values()) if (client.socket == webSocket) return client.id;
        return -1;
    }

    public void changeStatus (WebSocket webSocket, boolean status) {
        clients.get(webSocket).status = status;
    }
    public boolean getStatus (WebSocket webSocket) {
        return clients.get(webSocket).status;
    }
}
