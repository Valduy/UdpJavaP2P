package com.company.states;

import Network.EndPoint;
import Network.MessageHelper;
import Network.NetworkMessages;
import com.company.Match;

import java.net.InetAddress;

public class WaitClientState extends MatchStateBase{
    public WaitClientState(Match context) {
        super(context);
    }

    @Override
    public void processMessage(InetAddress address, int port, byte[] received) {
        if (MessageHelper.getMessageType(received) == NetworkMessages.HELLO){
            var endPoint = new EndPoint(address, port);

            if (!isClient(endPoint)){
                // TODO: добавить конечную точку и порт
            }

            getContext().sendMessage(address, port, MessageHelper.getMessage(NetworkMessages.HELLO));
        }
    }
}
