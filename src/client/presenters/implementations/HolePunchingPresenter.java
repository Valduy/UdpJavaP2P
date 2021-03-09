package client.presenters.implementations;

import client.presenters.interfaces.LoadPresenter;
import client.views.interfaces.ChildView;
import com.company.network.P2PConnectionMessage;
import events.Event;
import events.EventArgs;

import java.net.DatagramSocket;

public class HolePunchingPresenter implements LoadPresenter {
    @Override
    public ChildView getView() {
        return null;
    }

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
    public void connect(DatagramSocket socket, int port) {

    }
}
