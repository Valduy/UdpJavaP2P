package client.presenters.interfaces;

import com.company.network.P2PConnectionMessage;
import events.Event;
import events.EventArgs;

import java.net.DatagramSocket;

public interface LoadPresenter extends ChildPresenter {
    P2PConnectionMessage getConnectionMessage();
    void addConnected(Event<EventArgs> methodReference);
    void removeConnected(Event<EventArgs> methodReference);
    void connect(DatagramSocket socket, int port);
}
