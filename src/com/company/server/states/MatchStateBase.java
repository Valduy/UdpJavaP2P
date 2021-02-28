package com.company.server.states;

import com.company.network.EndPoint;
import com.company.server.Match;

import java.net.InetAddress;

public abstract class MatchStateBase {
    private final Match context;

    protected Match getContext(){
        return context;
    }

    public MatchStateBase(Match context){
        this.context = context;
    }

    public abstract void processMessage(InetAddress address, int port, byte[] received);

    protected boolean isClient(EndPoint endPoint){
        return context.getClients().stream().anyMatch(c -> c.publicEndPoint.equals(endPoint));
    }

    protected boolean isHost(EndPoint endPoint){
        return context.getHost() != null && context.getHost().publicEndPoint.equals(endPoint);
    }
}
