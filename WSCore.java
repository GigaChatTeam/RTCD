import com.jsoniter.output.JsonStream;
import exceptions.AccessDenied;
import exceptions.NotFound;
import exceptions.NotValid;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Arrays;


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
        if (Starter.DEBUG >= 3) System.out.println(clientHandshake.getResourceDescriptor()); // Debug

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
            webSocket.send(SystemResponses.Errors.Systems.NOT_AUTHORIZED());
            return;
        }

        if (Starter.DEBUG >= 3) System.out.println(message);

        Helper.MessagePacket packet = Helper.parsePacket(message);
        Commands cmd = Commands.byIntents(packet.intention);

        try {
            packet.parseData(cmd.pattern);
        } catch (NullPointerException e) {
            if (Starter.DEBUG >= 1) System.out.println(e.getMessage());
            webSocket.send(STR. "SYSTEM%MISS%\{ JsonStream.serialize(SystemResponses.Errors.Systems.NOT_VALID_INTENTIONS()) }" );
            return;
        }

        if (Starter.DEBUG >= 3) {
            System.out.println(Arrays.toString(packet.intention));
            System.out.println(packet.hash);
            System.out.println(JsonStream.serialize(JsonStream.serialize(packet.postData)));
        }

        try {
            switch (cmd) {
                case USER_CHANNELS_MESSAGES_POST_NEW -> {
                    if (!clients.isUserInChannel(webSocket, ((CommandsPatterns.Channels.Messages.Post.New) packet.postData).channel)) {
                        webSocket.send(SystemResponses.Errors.Users.PERMISSION_DENIED(packet.hash));
                        return;
                    }

                    Timestamp posted = ChannelsExecutor.Messages.postMessage(
                            ((CommandsPatterns.Channels.Messages.Post.New) packet.postData).author,
                            ((CommandsPatterns.Channels.Messages.Post.New) packet.postData).channel,
                            ((CommandsPatterns.Channels.Messages.Post.New) packet.postData).text);

                    clients.sendCommandToChannel(((CommandsPatterns.Channels.Messages.Post.New) packet.postData).channel,
                            new ResponsesPatterns.Channels.Messages.Post.New((CommandsPatterns.Channels.Messages.Post.New) packet.postData, posted).serialize(packet.hash));
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
                case SYSTEM_CHANNELS_LISTEN_REMOVE -> clients.removeListeningClientFromChannel(
                        ((CommandsPatterns.Systems.Listen.Add) packet.postData).client,
                        ((CommandsPatterns.Systems.Listen.Add) packet.postData).channel);
                case USER_CHANNELS_MESSAGES_EDIT -> {
                    if (!clients.isUserInChannel(webSocket, ((CommandsPatterns.Channels.Messages.Edit) packet.postData).channel)) {
                        webSocket.send(SystemResponses.Errors.Users.PERMISSION_DENIED(packet.hash));
                        return;
                    }

                    ChannelsExecutor.Messages.editMessage((CommandsPatterns.Channels.Messages.Edit) packet.postData);

                    clients.sendCommandToChannel(((CommandsPatterns.Channels.Messages.Edit) packet.postData).channel,
                            new ResponsesPatterns.Channels.Messages.Edit((CommandsPatterns.Channels.Messages.Edit) packet.postData).serialize(packet.hash));
                }
                case ADMIN_CHANNELS_SETTINGS_EXTERNAL_CHANGE_TITLE -> {
                    ChannelsExecutor.Settings.External.changeTitle(
                            ((CommandsPatterns.Channels.Settings.External.Change.Title) packet.postData).client,
                            ((CommandsPatterns.Channels.Settings.External.Change.Title) packet.postData).channel,
                            ((CommandsPatterns.Channels.Settings.External.Change.Title) packet.postData).newTitle);

                    clients.sendCommandToChannel(((CommandsPatterns.Channels.Settings.External.Change.Title) packet.postData).channel,
                            new ResponsesPatterns.Channels.Settings.External.Change.Title((CommandsPatterns.Channels.Settings.External.Change.Title) packet.postData).serialize(packet.hash));
                }
                case ADMIN_CHANNELS_SETTINGS_EXTERNAL_CHANGE_DESCRIPTION -> {
                    ChannelsExecutor.Settings.External.changeTitle(
                            ((CommandsPatterns.Channels.Settings.External.Change.Description) packet.postData).client,
                            ((CommandsPatterns.Channels.Settings.External.Change.Description) packet.postData).channel,
                            ((CommandsPatterns.Channels.Settings.External.Change.Description) packet.postData).newDescription);

                    clients.sendCommandToChannel(((CommandsPatterns.Channels.Settings.External.Change.Description) packet.postData).channel,
                            new ResponsesPatterns.Channels.Settings.External.Change.Description((CommandsPatterns.Channels.Settings.External.Change.Description) packet.postData).serialize(packet.hash));
                }
                default -> throw new ParseException("SERVER ERROR", 1);
            }
        } catch (SQLException e) {
            if (Starter.DEBUG >= 1) System.out.println(e.getMessage());
            webSocket.send(SystemResponses.Errors.Users.SERVER_ERROR(packet.hash));
        } catch (AccessDenied e) {
            if (Starter.DEBUG >= 2) System.out.println(e.getMessage());
            webSocket.send(SystemResponses.Errors.Users.PERMISSION_DENIED(packet.hash));
        } catch (ParseException e) {
            if (Starter.DEBUG >= 2) System.out.println(e.getMessage());
            webSocket.send(SystemResponses.Errors.Users.MESSAGE_DAMAGED(packet.hash));
        } catch (NotFound.Channel e) {
            if (Starter.DEBUG >= 2) System.out.println(e.getMessage());
            webSocket.send(SystemResponses.Errors.Users.NOT_FOUND(packet.hash));
        } catch (NotValid.Data e) {
            if (Starter.DEBUG >= 2) System.out.println(e.getMessage());
            webSocket.send(SystemResponses.Errors.Users.NOT_VALID_DATA(packet.hash));
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