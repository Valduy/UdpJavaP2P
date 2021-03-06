package com.company.server.matches.states;

import com.company.server.matches.Match;
import com.company.server.matches.MatchException;
import com.google.gson.Gson;
import com.company.network.*;

import java.net.InetAddress;
import java.util.ArrayList;

public class ConnectClientsState extends MatchStateBase{
    private final byte[] hostMessage;
    private final byte[] clientMessage;

    public ConnectClientsState(Match context) {
        super(context);
        var gson = new Gson();

        var messageForHost = new P2PConnectionMessage();
        messageForHost.role = Role.Host;
        messageForHost.clients = new ArrayList<>(getContext().getClients());
        hostMessage = MessageHelper.getMessage(NetworkMessages.INIT, gson.toJson(messageForHost));

        var messageForClient = new P2PConnectionMessage();
        messageForClient.role = Role.Client;
        messageForClient.clients = new ArrayList<>();
        messageForClient.clients.add(getContext().getHost());
        clientMessage = MessageHelper.getMessage(NetworkMessages.INIT, gson.toJson(messageForClient));
    }

    @Override
    public void processMessage(InetAddress address, int port, byte[] received) throws MatchException {
        if (MessageHelper.getMessageType(received) == NetworkMessages.HLLO){
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
