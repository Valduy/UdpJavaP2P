package com.company.states;

import network.EndPoint;
import network.EndPoints;
import network.MessageHelper;
import network.NetworkMessages;
import com.company.Match;

import java.net.InetAddress;

public class ChooseHostState extends MatchStateBase {
    public ChooseHostState(Match context) {
        super(context);
    }

    @Override
    public void processMessage(InetAddress address, int port, byte[] received) {
        if (MessageHelper.getMessageType(received) == NetworkMessages.HELLO){
            var publicEndPoint = new EndPoint(address, port);
            var client = getClient(publicEndPoint);

            if (client != null){
                var context = getContext();
                context.setHost(client);
                context.setState(new ConnectClientsState(context));
                context.sendMessage(address, port, MessageHelper.getMessage(NetworkMessages.HELLO));
            }
        }
    }

    private EndPoints getClient(EndPoint endPoint){
        return getContext().getClients()
                .stream()
                .filter(c -> c.publicEndPoint.equals(endPoint))
                .findFirst()
                .orElse(null);
    }
}
