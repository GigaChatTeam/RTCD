import com.jsoniter.spi.JsonException;
import dbexecutors.ChannelsExecutor;
import exceptions.AccessDenied;
import exceptions.AlreadyCompleted;
import exceptions.NotFound;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.NoSuchElementException;

import static dbexecutors.SystemExecutor.logAuthentication;
import static dbexecutors.SystemExecutor.logExit;


class WSCore extends WebSocketServer {
    private final Clients clients = new Clients( );
    private final int port;

    public WSCore (int port) {
        super(new InetSocketAddress(port));
        this.port = port;
    }

    private boolean clientIDVerifier (@NotNull WebSocket webSocket, long id) {
        return id == clients.getID(webSocket) && id != -1;
    }

    @Override
    public void onOpen (@NotNull WebSocket webSocket, @NotNull ClientHandshake clientHandshake) {
        if (Starter.DEBUG >= 3) System.out.println(clientHandshake.getResourceDescriptor( ));

        String[] connectionParams = clientHandshake.getResourceDescriptor( ).split("/");

        try {
            ExpectedClient validateClient = Starter.authorizer.validateClient(Long.valueOf(connectionParams[1]), connectionParams[2]);
            if (validateClient != null) {
                clients.addClient(new ConnectedClient(webSocket, Long.parseLong(connectionParams[1]), Helper.SHA512(validateClient.key)));
                webSocket.send(new ResponsesPatterns.System.ConnectionParameters.ConnectionControl(true).serialize(connectionParams[2]));
                clients.changeClientConnectionStatus(webSocket, true);
                logAuthentication(Long.parseLong(connectionParams[1]), Helper.SHA512(validateClient.key), validateClient.agent);
            } else webSocket.close(4002, "InvalidAuthorizationData");
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
            webSocket.close(1007, "InsufficientData");
        } catch (Exception e) {
            e.printStackTrace( );
            webSocket.close(1011, "InsufficientData");
        }
    }

    @Override
    public void onMessage (@NotNull WebSocket webSocket, @NotNull String message) {
        if (!clients.getClientConnectionStatus(webSocket)) {
            webSocket.send(SystemResponses.Errors.Systems.NOT_AUTHORIZED( ));
            return;
        }

        if (Starter.DEBUG >= 3) System.out.println(message);

        Helper.MessagePacket packet;
        try {
            packet = new Helper.MessagePacket(message);
        } catch (ParseException e) {
            if (Starter.DEBUG >= 1) e.printStackTrace( );
            webSocket.send(new ResponsesPatterns.System.ClientErrors.DataErrors.NotValidData( ).serialize("MISS"));
            return;
        }

        Commands cmd;

        try {
            cmd = Commands.byIntents(packet.intention);
            packet.parseData(cmd.pattern);
        } catch (NoSuchElementException | UnsupportedOperationException e) {
            if (Starter.DEBUG >= 1) e.printStackTrace( );
            webSocket.send(new ResponsesPatterns.System.ServerErrors.OutdatedServer( ).serialize(packet.hash));
            return;
        } catch (JsonException e) {
            if (Starter.DEBUG >= 1) e.printStackTrace( );
            webSocket.send(new ResponsesPatterns.System.ClientErrors.DataErrors.NotValidData( ).serialize(packet.hash));
            return;
        }

        try {
            switch (cmd) {
                case CHANNELS_USERS_MESSAGES_POST_NEW -> {
                    if (!clients.isUserInChannel(
                            webSocket,
                            ((CommandsPatterns.Channels.User.Messages.Post.New) packet.postData).channel)
                        ||
                        !clients.isUserCanPostToChannel(
                                webSocket,
                                ((CommandsPatterns.Channels.User.Messages.Post.New) packet.postData).channel))
                        throw new AccessDenied( );


                    clients.sendCommandToChannel(
                            ((CommandsPatterns.Channels.User.Messages.Post.New) packet.postData).channel,
                            ((CommandsPatterns.Channels.User.Messages.Post.New) packet.postData).text);

                    switch (((CommandsPatterns.Channels.User.Messages.Post.New) packet.postData).type) {
                        case "TEXT" -> {
                            if (!clients.isUserCanPostToChannel(webSocket, ((CommandsPatterns.Channels.User.Messages.Post.New) packet.postData).channel))
                                throw new AccessDenied( );

                            clients.sendCommandToChannel(
                                    ((CommandsPatterns.Channels.User.Messages.Post.New) packet.postData).channel,
                                    new ResponsesPatterns.Channels.User.Messages.Post.New(
                                            ((CommandsPatterns.Channels.User.Messages.Post.New) packet.postData).channel,
                                            clients.getID(webSocket),
                                            ((CommandsPatterns.Channels.User.Messages.Post.New) packet.postData).text,
                                            ChannelsExecutor.Messages.postTextMessage(
                                                    clients.getID(webSocket),
                                                    ((CommandsPatterns.Channels.User.Messages.Post.New) packet.postData).channel,
                                                    ((CommandsPatterns.Channels.User.Messages.Post.New) packet.postData).alias,
                                                    ((CommandsPatterns.Channels.User.Messages.Post.New) packet.postData).text,
                                                    null, ((CommandsPatterns.Channels.User.Messages.Post.New) packet.postData).files),
                                            null,
                                            ((CommandsPatterns.Channels.User.Messages.Post.New) packet.postData).files).serialize(packet.hash));
                        }
                        case null, default -> throw new ParseException("", 1);
                    }
                }
                case CHANNELS_SYSTEM_LISTENING_ADD -> {
                    if (!ChannelsExecutor.Users.Presence.isClientOnChannel(
                            clients.getID(webSocket),
                            ((CommandsPatterns.Channels.System.Notification.Listening.Add) packet.postData).channel))
                        throw new AccessDenied( );

                    clients.addListeningClientToChannel(
                            clients.getID(webSocket),
                            ((CommandsPatterns.Channels.System.Notification.Listening.Add) packet.postData).channel,
                            true);

                    webSocket.send(
                            new ResponsesPatterns.Channels.System.Notification.AddListening(
                                    ((CommandsPatterns.Channels.System.Notification.Listening.Add) packet.postData).channel).serialize(packet.hash));
                }
                case CHANNELS_SYSTEM_LISTENING_REMOVE -> {
                    clients.removeListeningClientFromChannel(
                            clients.getID(webSocket),
                            ((CommandsPatterns.Channels.System.Notification.Listening.Remove) packet.postData).channel);

                    webSocket.send(
                            new ResponsesPatterns.Channels.System.Notification.RemoveListening(
                                    ((CommandsPatterns.Channels.System.Notification.Listening.Add) packet.postData).channel).serialize(packet.hash));
                }
                case CHANNELS_SYSTEM_CREATE ->
                        webSocket.send(
                                new ResponsesPatterns.Channels.System.Control.Create(
                                        ChannelsExecutor.create(
                                                clients.getID(webSocket),
                                                ((CommandsPatterns.Channels.System.Control.Create) packet.postData).title),
                                        ((CommandsPatterns.Channels.System.Control.Create) packet.postData).title).serialize(packet.hash));
                case CHANNELS_USERS_INVITATIONS_CREATE -> webSocket.send(
                        new ResponsesPatterns.Channels.Invitations.Create(
                                clients.getID(webSocket),
                                ChannelsExecutor.Invitations.create(
                                        clients.getID(webSocket),
                                        ((CommandsPatterns.Channels.System.Invitations.Create) packet.postData).channel,
                                        ((CommandsPatterns.Channels.System.Invitations.Create) packet.postData).expiration,
                                        ((CommandsPatterns.Channels.System.Invitations.Create) packet.postData).permittedUses)).serialize(packet.hash));
                case CHANNELS_USERS_INVITATIONS_DELETE -> {
                    ChannelsExecutor.Invitations.delete(
                            clients.getID(webSocket),
                            ((CommandsPatterns.Channels.System.Invitations.Delete) packet.postData).uri);

                    webSocket.send(
                            new ResponsesPatterns.Channels.Invitations.Delete(
                                    ((CommandsPatterns.Channels.System.Invitations.Delete) packet.postData).uri).serialize(packet.hash));
                }
                case CHANNELS_USERS_JOIN -> webSocket.send(
                        new ResponsesPatterns.Channels.User.Presence.Join(
                                ChannelsExecutor.Users.Presence.join(
                                        clients.getID(webSocket),
                                        ((CommandsPatterns.Channels.User.Presence.Join) packet.postData).invitation)).serialize(packet.hash));
                case CHANNELS_USERS_LEAVE -> {
                    ChannelsExecutor.Users.Presence.leave(
                            clients.getID(webSocket),
                            ((CommandsPatterns.Channels.User.Presence.Leave) packet.postData).channel);

                    webSocket.send(new ResponsesPatterns.Channels.User.Presence.Leave().serialize(packet.hash));
                }
                default -> throw new ParseException("OUTDATED SERVER", 1);
            }
        } catch (SQLException e) {
            if (Starter.DEBUG >= 1) e.printStackTrace( );
            webSocket.send(new ResponsesPatterns.System.ServerErrors.InternalError( ).serialize(packet.hash));
        } catch (ParseException e) {
            if (Starter.DEBUG >= 2) e.printStackTrace( );
            webSocket.send(new ResponsesPatterns.System.ClientErrors.DataErrors.NotValidData( ).serialize(packet.hash));
        } catch (AccessDenied e) {
            if (Starter.DEBUG >= 2) e.printStackTrace( );
            webSocket.send(new ResponsesPatterns.System.ClientErrors.AccessErrors.AccessDenied( ).serialize(packet.hash));
        } catch (AlreadyCompleted e) {
            if (Starter.DEBUG >= 2) e.printStackTrace( );
            webSocket.send(new ResponsesPatterns.System.ClientErrors.AccessErrors.AlreadyCompleted( ).serialize(packet.hash));
        } catch (NotFound e) {
            if (Starter.DEBUG >= 2) e.printStackTrace( );
            webSocket.send(new ResponsesPatterns.System.ClientErrors.AccessErrors.NotFound( ).serialize(packet.hash));
        } catch (Throwable e) {
            System.out.println("----- Not handling exception -----");
            e.printStackTrace( );
        }
    }

    @Override
    public void onClose (@NotNull WebSocket webSocket, int status, @Nullable String reason, boolean b) {
        ConnectedClient client = clients.getClient(webSocket);
        try {
            if (client != null) {
                logExit(client.id, client.key);
            }
        } catch (SQLException e) {
            e.printStackTrace( );
        } finally {
            clients.removeClient(webSocket);
        }
    }

    @Override
    public void onError (@NotNull WebSocket webSocket, @NotNull Exception e) {
        e.printStackTrace( );
        ConnectedClient client = clients.getClient(webSocket);
        try {
            if (client != null) {
                logExit(client.id, client.key);
            }
        } catch (SQLException _e) {
            _e.printStackTrace( );
        } finally {
            clients.removeClient(webSocket);
        }
    }

    @Override
    public void onStart () {
        System.out.println(STR."WS server started on port \{port}");
    }
}