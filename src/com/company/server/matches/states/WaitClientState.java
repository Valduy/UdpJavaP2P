package com.company.server.matches.states;

import com.company.network.EndPoint;
import com.company.network.EndPoints;
import com.company.network.MessageHelper;
import com.company.network.NetworkMessages;
import com.company.server.matches.Match;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.StringReader;
import java.net.InetAddress;

public class WaitClientState extends MatchStateBase{
    private Gson gson = new Gson();

    public WaitClientState(Match context) {
        super(context);
    }

    @Override
    public void processMessage(InetAddress address, int port, byte[] received) {
        if (MessageHelper.getMessageType(received) == NetworkMessages.HLLO){
            var context = getContext();
            var publicEndPoint = new EndPoint(address, port);

            if (!isClient(publicEndPoint)){
                var message = MessageHelper.toString(received);
                var reader = new JsonReader(new StringReader(message));
                reader.setLenient(true);
                EndPoint localEndPoint = gson.fromJson(reader, EndPoint.class);
                var endPoints = new EndPoints(publicEndPoint, localEndPoint);
                context.addClient(endPoints);

                if (context.getIsFull()){
                    context.setState(new ChooseHostState(getContext()));
                }
            }

            context.sendMessage(address, port, MessageHelper.getMessage(NetworkMessages.HLLO));
        }
    }
}
