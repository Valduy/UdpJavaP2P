package com.company.states;

import Network.EndPoint;
import Network.EndPoints;
import Network.MessageHelper;
import Network.NetworkMessages;
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
                getContext().setHost(client);
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
