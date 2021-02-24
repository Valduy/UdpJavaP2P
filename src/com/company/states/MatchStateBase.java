package com.company.states;

import Network.EndPoint;
import com.company.Match;

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
}
