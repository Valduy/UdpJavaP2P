package com.company.states;

import com.company.Match;

import java.net.InetAddress;

public class ConnectClientsState extends MatchStateBase{
    public ConnectClientsState(Match context) {
        super(context);
    }

    @Override
    public void processMessage(InetAddress address, int port, byte[] received) {

    }
}
