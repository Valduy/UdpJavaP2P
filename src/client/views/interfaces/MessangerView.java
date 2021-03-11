package client.views.interfaces;

import client.ConnectionResult;
import events.Event;
import events.EventArgs;

import java.net.DatagramSocket;

public interface MessangerView extends ChildView{
    String getLastMessage();
    void addMessaged(Event<EventArgs> methodReference);
    void removeMessaged(Event<EventArgs> methodReference);
    void addMessage(String message);
}
