package com.company.states;

import com.company.Match;
import com.google.gson.Gson;
import network.*;

import java.net.InetAddress;
import java.util.ArrayList;

public class ConnectClientsState extends MatchStateBase{
    private final byte[] hostMessage;
    private final byte[] clientMessage;
    private final Gson gson = new Gson();

    public ConnectClientsState(Match context) {
        super(context);

        var messageForHost = new P2PConnectionMessage();
        messageForHost.role = Role.Host;
        messageForHost.clients = new ArrayList<>(getContext().getClients());
        hostMessage = MessageHelper.getMessage(NetworkMessages.Initial, gson.toJson(messageForHost));

        var messageForClient = new P2PConnectionMessage();
        messageForClient.role = Role.Client;
        messageForClient.clients = new ArrayList<>();
        messageForClient.clients.add(getContext().getHost());
        clientMessage = MessageHelper.getMessage(NetworkMessages.Initial, gson.toJson(messageForClient));
    }

    @Override
    public void processMessage(InetAddress address, int port, byte[] received) {
        if (MessageHelper.getMessageType(received) == NetworkMessages.Hello){
            var publicEndPoint = new EndPoint(address, port);
            var context = getContext();

            if (isClient(publicEndPoint)){
                context.sendMessage(address, port, clientMessage);
            }
            else if (isHost(publicEndPoint)){
                context.sendMessage(address, port, hostMessage);
            }
        }
    }
}
