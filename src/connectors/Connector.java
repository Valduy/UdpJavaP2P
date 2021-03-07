package connectors;

import events.Event;
import events.EventArgs;

import java.net.DatagramSocket;
import java.net.InetAddress;

public interface Connector<TResult> {
    void addFound(Event<EventArgs> methodReference);
    void removeFound(Event<EventArgs> methodReference);
    void start(DatagramSocket client, InetAddress address, int port) throws ConnectorException;
    void stop() throws ConnectorException;
    TResult getResult();
}
