package connectors;

import events.Event;
import events.EventArgs;

import java.net.DatagramSocket;
import java.net.InetAddress;

public interface Connector<TResult> {
    TResult getResult() throws ConnectorException;
    void addConnected(Event<EventArgs> methodReference);
    void removeConnected(Event<EventArgs> methodReference);
    void start(DatagramSocket client, InetAddress address, int port) throws ConnectorException;
    void stop() throws ConnectorException;
}
