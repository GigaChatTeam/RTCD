import co.elastic.clients.util.Pair;
import com.clickhouse.client.ClickHouseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import dbexecutors.Channels;
import exceptions.AccessDenied;
import exceptions.AlreadyCompleted;
import exceptions.ExpectedAddressNotEqualsRemotedException;
import exceptions.NotFound;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Objects;


class WSCore extends WebSocketServer {
    private final Clients clients = new Clients( );
    private final int port;

    public WSCore (int port) {
        super(new InetSocketAddress(port));
        this.port = port;
    }

    @Override
    public void onOpen (@NotNull WebSocket webSocket, @NotNull ClientHandshake clientHandshake) {
        if (Starter.DEBUG >= 3)
            System.out.println(clientHandshake.getResourceDescriptor( ));

        String[] connectionParams = clientHandshake.getResourceDescriptor( ).split("/");

        try {
            ExpectedClient validateClient = Starter.authorizer.validateClient(Long.valueOf(connectionParams[1]), connectionParams[2]);

            if (validateClient != null) {
                if (Starter.DEBUG > 2)
                    System.out.println(STR."Client \{validateClient.id} try to connected");

                if (!Objects.equals(validateClient.ipAddress.getHostAddress( ), webSocket.getRemoteSocketAddress( ).getAddress( ).getHostAddress( )))
                    throw new ExpectedAddressNotEqualsRemotedException( );

                clients.addClient(new ConnectedClient(webSocket, validateClient));
                webSocket.send(SystemResponses.Confirmations.CONNECTION_READY(Helper.SHA512(connectionParams[2])));
                clients.changeClientConnectionStatus(webSocket, true);

                if (Starter.DEBUG > 2)
                    System.out.println(STR."Client \{validateClient.id} connected");
            } else webSocket.close(4002, "InvalidAuthorizationData");
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException | ExpectedAddressNotEqualsRemotedException e) {
            webSocket.close(1007, "InsufficientData");
        } catch (Exception e) {
            e.printStackTrace( );
            webSocket.close(1011, "InternalServerError");
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
            webSocket.send(SystemResponses.Errors.Systems.MESSAGE_DAMAGED( ));
            return;
        }

        Commands cmd;

        try {
            cmd = Commands.byIntents(packet.intention);
            assert cmd != null;
            packet.parseData(cmd.pattern);
        } catch (JsonProcessingException | AssertionError e) {
            if (Starter.DEBUG >= 1) e.printStackTrace( );
            webSocket.send(SystemResponses.Errors.Systems.NOT_VALID_INTENTIONS( ));
            return;
        }

        try {
            switch (cmd) {
                case CHANNELS_USERS_MESSAGES_POST_NEW -> {
                    switch (((CommandsPatterns.Channels.User.Messages.Post.New) packet.postData).type) {
                        case "TEXT" -> {
                            if (!clients.isUserCanPostToChannel(
                                    webSocket,
                                    ((CommandsPatterns.Channels.User.Messages.Post.New) packet.postData).channel))
                                throw new AccessDenied( );

                            Pair<Long, Timestamp> meta = Channels.Messages.postTextMessage(
                                    clients.getID(webSocket),
                                    ((CommandsPatterns.Channels.User.Messages.Post.New) packet.postData).channel,
                                    ((CommandsPatterns.Channels.User.Messages.Post.New) packet.postData).alias,
                                    ((CommandsPatterns.Channels.User.Messages.Post.New) packet.postData).text,
                                    ((CommandsPatterns.Channels.User.Messages.Post.New) packet.postData).media,
                                    ((CommandsPatterns.Channels.User.Messages.Post.New) packet.postData).files);

                            clients.sendCommandToChannel(
                                    ((CommandsPatterns.Channels.User.Messages.Post.New) packet.postData).channel,
                                    new ResponsesPatterns.Channels.User.Messages.Post.New(
                                            ((CommandsPatterns.Channels.User.Messages.Post.New) packet.postData).channel,
                                            clients.getID(webSocket),
                                            "TEXT",
                                            ((CommandsPatterns.Channels.User.Messages.Post.New) packet.postData).text,
                                            meta.value( ),
                                            null,
                                            ((CommandsPatterns.Channels.User.Messages.Post.New) packet.postData).files).serialize(packet.hash));
                        }
                        case null, default -> throw new ParseException("", 1);
                    }
                }
                case CHANNELS_SYSTEM_LISTENING_ADD -> {
                    clients.addListeningClientToChannel(
                            webSocket,
                            new datathreads.Channel(
                                    ((CommandsPatterns.Channels.System.Notification.Listening.Add) packet.postData).channel,
                                    Channels.Users.Presence.getUserFromChannel(
                                            clients.getID(webSocket),
                                            ((CommandsPatterns.Channels.System.Notification.Listening.Add) packet.postData).channel)
                            ));

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
                case CHANNELS_SYSTEM_CREATE -> webSocket.send(
                        new ResponsesPatterns.Channels.System.Control.Create(
                                Channels.create(
                                        clients.getID(webSocket),
                                        ((CommandsPatterns.Channels.System.Control.Create) packet.postData).title,
                                        ((CommandsPatterns.Channels.System.Control.Create) packet.postData).isPublic),
                                ((CommandsPatterns.Channels.System.Control.Create) packet.postData).title).serialize(packet.hash));
                case CHANNELS_USERS_INVITATIONS_CREATE -> webSocket.send(
                        new ResponsesPatterns.Channels.Invitations.Create(
                                clients.getID(webSocket),
                                Channels.Invitations.create(
                                        clients.getID(webSocket),
                                        ((CommandsPatterns.Channels.System.Invitations.Create) packet.postData).channel,
                                        ((CommandsPatterns.Channels.System.Invitations.Create) packet.postData).expiration,
                                        ((CommandsPatterns.Channels.System.Invitations.Create) packet.postData).permittedUses)).serialize(packet.hash));
                case CHANNELS_USERS_INVITATIONS_DELETE -> {
                    Channels.Invitations.delete(
                            clients.getID(webSocket),
                            ((CommandsPatterns.Channels.System.Invitations.Delete) packet.postData).uri);

                    webSocket.send(
                            new ResponsesPatterns.Channels.Invitations.Delete(
                                    ((CommandsPatterns.Channels.System.Invitations.Delete) packet.postData).uri).serialize(packet.hash));
                }
                case CHANNELS_USERS_JOIN -> webSocket.send(
                        new ResponsesPatterns.Channels.User.Presence.Join(
                                Channels.Users.Presence.join(
                                        clients.getID(webSocket),
                                        ((CommandsPatterns.Channels.User.Presence.Join) packet.postData).invitation)).serialize(packet.hash));
                case CHANNELS_USERS_LEAVE -> {
                    Channels.Users.Presence.leave(
                            clients.getID(webSocket),
                            ((CommandsPatterns.Channels.User.Presence.Leave) packet.postData).channel,
                            "self");

                    webSocket.send(new ResponsesPatterns.Channels.User.Presence.Leave( ).serialize(packet.hash));
                }
                default -> throw new ParseException("OUTDATED SERVER", 1);
            }
        } catch (SQLException | IOException | ClickHouseException e) {
            if (Starter.DEBUG >= 1) e.printStackTrace( );
            webSocket.send(SystemResponses.Errors.Systems.SERVER_ERROR(packet.hash));
        } catch (ParseException e) {
            if (Starter.DEBUG >= 2) e.printStackTrace( );
            webSocket.send(SystemResponses.Errors.Users.NOT_VALID_DATA(packet.hash));
        } catch (AccessDenied e) {
            if (Starter.DEBUG >= 2) e.printStackTrace( );
            webSocket.send(SystemResponses.Errors.Users.ACCESS_DENIED(packet.hash));
        } catch (AlreadyCompleted e) {
            if (Starter.DEBUG >= 2) e.printStackTrace( );
            webSocket.send(SystemResponses.Errors.Users.ALREADY_COMPLETED(packet.hash));
        } catch (NotFound e) {
            if (Starter.DEBUG >= 2) e.printStackTrace( );
            webSocket.send(SystemResponses.Errors.Users.NOT_FOUND(packet.hash));
        } catch (Throwable e) {
            System.out.println("----- Not handling exception -----");
            e.printStackTrace( );
        }
    }

    @Override
    public void onClose (WebSocket webSocket, int status, String reason, boolean remote) {
        if (Arrays.asList(4002, 1007, 1011).contains(status)) // TODO: clean up
            return;

        if (Starter.DEBUG > 2)
            System.out.println(STR."Client \{clients.getClient(webSocket).id} disconnected, by reason \{Helper.firstNonNull(reason, "not specified")}, \{remote ? "remote" : "local"}");

        clients.removeClient(webSocket, 1001, "", remote, false);
    }

    @Override
    public void onError (WebSocket webSocket, Exception e) {
        if (Starter.DEBUG > 2) e.printStackTrace( );
        if (Starter.DEBUG > 1)
            System.out.println(STR."Client \{clients.getClient(webSocket).id} disconnected with error");

        clients.removeClient(webSocket, 1001, "CloseOnError", false, true);
    }

    @Override
    public void onStart ( ) {
        System.out.println(STR."WS server started on port \{port}");
    }

    @Override
    public void stop ( ) throws InterruptedException {
        try {
            clients.closeAllClients(1001, "ServerShutdown");
        } catch (SQLException e) {
            e.printStackTrace( );
        }

        super.stop( );
    }
}