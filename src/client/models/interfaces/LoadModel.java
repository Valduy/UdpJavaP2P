package client.models.interfaces;

import client.ConnectionResult;
import connectors.ConnectorException;
import events.Event;
import events.EventArgs;

import java.net.DatagramSocket;
import java.net.InetAddress;

public interface LoadModel{
    ConnectionResult getConnectionMessage() throws Exception;
    void addConnected(Event<EventArgs> methodReference);
    void removeConnected(Event<EventArgs> methodReference);
    void startConnection(DatagramSocket socket, InetAddress address, int port) throws Exception;
}
