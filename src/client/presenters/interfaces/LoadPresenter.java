package client.presenters.interfaces;

import client.ConnectionResult;
import connectors.ConnectorException;
import events.Event;
import events.EventArgs;

import java.net.DatagramSocket;

public interface LoadPresenter extends ChildPresenter {
    ConnectionResult getConnectionMessage() throws ConnectorException;
    void addConnected(Event<EventArgs> methodReference);
    void removeConnected(Event<EventArgs> methodReference);
    void connect(DatagramSocket socket, int port) throws ConnectorException;
}
