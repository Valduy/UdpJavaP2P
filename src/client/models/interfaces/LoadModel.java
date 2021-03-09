package client.models.interfaces;

import com.company.network.P2PConnectionMessage;
import connectors.ConnectorException;
import events.Event;
import events.EventArgs;

public interface LoadModel{
    P2PConnectionMessage getConnectionMessage();
    void addConnected(Event<EventArgs> methodReference);
    void removeConnected(Event<EventArgs> methodReference);
    void startConnection();
}
