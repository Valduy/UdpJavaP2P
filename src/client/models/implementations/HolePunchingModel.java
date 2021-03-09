package client.models.implementations;

import client.models.interfaces.LoadModel;
import com.company.network.P2PConnectionMessage;
import events.Event;
import events.EventArgs;

public class HolePunchingModel implements LoadModel {
    @Override
    public P2PConnectionMessage getConnectionMessage() {
        return null;
    }

    @Override
    public void addConnected(Event<EventArgs> methodReference) {

    }

    @Override
    public void removeConnected(Event<EventArgs> methodReference) {

    }

    @Override
    public void startConnection() {

    }
}
