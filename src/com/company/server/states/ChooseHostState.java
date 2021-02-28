package com.company.server.states;

import com.company.network.EndPoint;
import com.company.network.EndPoints;
import com.company.network.MessageHelper;
import com.company.network.NetworkMessages;
import com.company.server.Match;

import java.net.InetAddress;

public class ChooseHostState extends MatchStateBase {
    public ChooseHostState(Match context) {
        super(context);
    }

    @Override
    public void processMessage(InetAddress address, int port, byte[] received) {
        if (MessageHelper.getMessageType(received) == NetworkMessages.Hello){
            var publicEndPoint = new EndPoint(address, port);
            var client = getClient(publicEndPoint);

            if (client != null){
                var context = getContext();
                context.setHost(client);
                context.setState(new ConnectClientsState(context));
                context.sendMessage(address, port, MessageHelper.getMessage(NetworkMessages.Hello));
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
