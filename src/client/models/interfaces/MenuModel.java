package client.models.interfaces;

import connectors.ConnectorException;
import events.Event;
import events.EventArgs;

import java.net.DatagramSocket;

public interface MenuModel {
    int getMatchPort() throws ConnectorException;
    void addConnected(Event<EventArgs> methodReference);
    void removeConnected(Event<EventArgs> methodReference);
    void startSearch(DatagramSocket client) throws ConnectorException;
    void stopSearch() throws ConnectorException;
}
