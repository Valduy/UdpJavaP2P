package com.company.states;

import network.EndPoint;
import network.EndPoints;
import network.MessageHelper;
import network.NetworkMessages;
import com.company.Match;
import com.google.gson.Gson;

import java.net.InetAddress;

public class WaitClientState extends MatchStateBase{
    private Gson gson = new Gson();

    public WaitClientState(Match context) {
        super(context);
    }

    @Override
    public void processMessage(InetAddress address, int port, byte[] received) {
        if (MessageHelper.getMessageType(received) == NetworkMessages.Hello){
            var context = getContext();
            var publicEndPoint = new EndPoint(address, port);

            if (!isClient(publicEndPoint)){
                var message = MessageHelper.toString(received);
                var localEndPoint = gson.fromJson(message, EndPoint.class);
                var endPoints = new EndPoints(publicEndPoint, localEndPoint);
                context.addClient(endPoints);

                if (context.getIsFull()){
                    context.setState(new ChooseHostState(getContext()));
                }
            }

            context.sendMessage(address, port, MessageHelper.getMessage(NetworkMessages.Hello));
        }
    }
}
