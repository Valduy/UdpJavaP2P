package client.presenters.interfaces;

import connectors.ConnectorException;
import events.Event;
import events.EventArgs;

import java.net.DatagramSocket;

public interface MenuPresenter extends ChildPresenter {
    void addConnected(Event<EventArgs> methodReference);
    void removeConnected(Event<EventArgs> methodReference);
    DatagramSocket getSocket();
    int getMatchPort() throws ConnectorException;
}
