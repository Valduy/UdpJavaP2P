package client.models.interfaces;

import client.ConnectionResult;
import events.Event;
import events.EventArgs;

import java.io.IOException;
import java.net.DatagramSocket;

public interface MessengerModel {
    String getLastMessage();
    void addReceived(Event<EventArgs> methodReference);
    void removeReceived(Event<EventArgs> methodReference);
    void start(DatagramSocket socket, ConnectionResult result) throws Exception;
    void stop();
    void send(String data) throws IOException;
}
