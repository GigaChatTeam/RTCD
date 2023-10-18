import com.jsoniter.output.JsonStream;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;
import java.sql.SQLException;


class WSCore extends WebSocketServer {
    private final Clients clients = new Clients();
    private final int port;

    public WSCore (int port) {
        super(new InetSocketAddress(port));
        this.port = port;
    }

    private boolean clientIDVerifier (WebSocket webSocket, long id) {
        return id == clients.getID(webSocket) && id != -1;
    }

    @Override
    public void onOpen (WebSocket webSocket, @NotNull ClientHandshake clientHandshake) {
        System.out.println(clientHandshake.getResourceDescriptor()); // Debug

        Helper.ConnectionPath connectParams;

        try {
            connectParams = Helper.parseURI(clientHandshake.getResourceDescriptor());
        } catch (Helper.InvalidURIException e) {
            webSocket.close(406, "InsufficientData");
            return;
        }

        if (connectParams.params.get("id") != null && connectParams.params.get("token") != null) {
            if (PermissionOperator.validateToken(Integer.parseInt(connectParams.params.get("id")), connectParams.params.get("token"))) {
                clients.addClient(new Client(webSocket, Integer.parseInt(connectParams.params.get("id")), connectParams.params.get("token")));

                webSocket.send(STR. "CONNECTION%\{ JsonStream.serialize(SystemResponses.Confirmations.CONNECTION_READY) }" );

                clients.changeClientConnectionStatus(webSocket, true);
            } else webSocket.close(401, "InvalidAuthorizationData");
        } else webSocket.close(406, "InsufficientData");
    }

    @Override
    public void onMessage (WebSocket webSocket, String message) {
        if (!clients.getClientConnectionStatus(webSocket)) {
            webSocket.send(STR. "CONNECTION%MISS%\{ JsonStream.serialize(SystemResponses.Errors.NOT_AUTHORIZED) }" );
            return;
        }

        if (Starter.DEBUG >= 3) {
            System.out.println(message);
            clients.sendAll(message);
            return;
        }

        Helper.MessagePacket packet = Helper.parsePacket(message);
        Commands cmd = Commands.byIntents(packet.intention);

        try {
            packet.parseData(cmd.pattern);
        } catch (NullPointerException e) {
            if (Starter.DEBUG >= 1) System.out.println(e.getMessage());
            webSocket.send(STR. "SYSTEM%MISS%\{ JsonStream.serialize(SystemResponses.Errors.NOT_VALID_INTENTIONS) }" );
            return;
        }

        if (!clientIDVerifier(webSocket, ((CommandsPatterns.Channels.Messages.Post.New) packet.postData).author)) {
            webSocket.send(STR. "SYSTEM%\{ packet.hash }%\{ JsonStream.serialize(SystemResponses.Errors.NOT_VALID_ID) }" );
            return;
        }

        try {
            switch (cmd) {
                case ADMIN_CHANNELS_CREATE -> {
                    long channel_id = ChannelsExecutor.create(
                            ((CommandsPatterns.Channels.Create) packet.postData).owner,
                            ((CommandsPatterns.Channels.Create) packet.postData).title);

                    clients.addListeningClientToChannel(webSocket, channel_id);
                    clients.sendCommandToChannel(channel_id,
                            STR. "\{ String.join("-", packet.intention) }%\{ packet.hash }%\{ JsonStream.serialize(
                                    new ResponsesPatterns.Channels.Create((CommandsPatterns.Channels.Create) packet.postData, channel_id)) }" );
                }
                case ADMIN_CHANNELS_DELETE -> {

                }
                case USER_CHANNELS_MESSAGES_POST_NEW -> {
                    if (!clients.isUserInChannel(webSocket, ((CommandsPatterns.Channels.Messages.Post.New) packet.postData).channel)) {
                        webSocket.send(STR. "SYSTEM%\{ packet.hash }%\{ JsonStream.serialize(SystemResponses.Errors.PERMISSION_DENIED) }" );
                        return;
                    }

                    long message_id = ChannelsExecutor.Messages.postMessage(
                            ((CommandsPatterns.Channels.Messages.Post.New) packet.postData).author,
                            ((CommandsPatterns.Channels.Messages.Post.New) packet.postData).channel,
                            ((CommandsPatterns.Channels.Messages.Post.New) packet.postData).text);

                    clients.sendCommandToChannel(((CommandsPatterns.Channels.Messages.Post.New) packet.postData).channel,
                            STR. "\{ String.join("-", packet.intention) }%\{ packet.hash }%\{ JsonStream.serialize(
                                    new ResponsesPatterns.Channels.Messages.Post.New((CommandsPatterns.Channels.Messages.Post.New) packet.postData, message_id)) }" );
                }
                case SYSTEM_CHANNELS_LISTEN_ADD -> {
                    if (!ChannelsExecutor.Users.Permissions.isClientOnChannel(
                            ((CommandsPatterns.Systems.Listen.Add) packet.postData).client,
                            ((CommandsPatterns.Systems.Listen.Add) packet.postData).channel)
                    ) throw new AccessDenied();

                    clients.addListeningClientToChannel(
                            ((CommandsPatterns.Systems.Listen.Add) packet.postData).client,
                            ((CommandsPatterns.Systems.Listen.Add) packet.postData).channel);
                }
                default ->
                        webSocket.send(STR. "SYSTEM%\{ packet.hash }%\{ JsonStream.serialize(SystemResponses.Errors.MESSAGE_DAMAGED) }" );
            }
        } catch (SQLException e) {
            if (Starter.DEBUG >= 1) System.out.println(e.getMessage());
            webSocket.send(JsonStream.serialize(SystemResponses.Errors.SERVER_ERROR));
        } catch (AccessDenied e) {
            if (Starter.DEBUG >= 2) System.out.println(e.getMessage());
            webSocket.send(JsonStream.serialize(SystemResponses.Errors.PERMISSION_DENIED));
        }
    }

    @Override
    public void onClose (WebSocket webSocket, int status, String reason, boolean b) {
        clients.removeClient(webSocket);
    }

    @Override
    public void onError (WebSocket webSocket, Exception e) {
        clients.removeClient(webSocket);
    }

    @Override
    public void onStart () {
        System.out.printf("Server started on port %s\n", port);
    }
}