package client.presenters.interfaces;

import com.company.network.EndPoint;
import events.Event;
import events.EventArgs;

import java.io.IOException;
import java.net.DatagramSocket;
import java.util.Collection;

public interface PongPresenter extends ChildPresenter{
    void addEnded(Event<EventArgs> methodReference);
    void removeEnded(Event<EventArgs> methodReference);

    void start(DatagramSocket socket, Collection<EndPoint> clients) throws IOException;
}
