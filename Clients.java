import org.java_websocket.WebSocket;

import java.util.HashMap;

class Clients {
    private final HashMap<WebSocket, Client> clients = new HashMap<>();

    public void sendCommandToChannel (int channel, String data) {
        clients.values().parallelStream()
                .filter(c -> c.getChannels().contains(channel))
                .forEach(c -> c.send(data));
    }

    public Boolean userIsConnected(int channel) {
        return null;
    }

    public void addClient (Client client) {
        clients.put(client.socket, client);
    }
    public Boolean isClientConnected (int client) {
        return clients.values().stream()
            .anyMatch(c -> c.id == client);
    }
    public void removeClient (WebSocket socket) {
        clients.remove(socket);
    }

    public void joinClientToChannel (WebSocket socket, int channel) {
        clients.values().parallelStream()
                .filter(c -> c.socket == socket)
                .forEach(c -> c.addListen(channel));
    }
    public void LeaveClientFromChannel (WebSocket socket, int channel) {
        clients.values().parallelStream()
                .filter(c -> c.socket == socket)
                .forEach(c -> c.removeListen(channel));
    }
}
