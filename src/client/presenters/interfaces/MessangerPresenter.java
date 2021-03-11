package client.presenters.interfaces;

import client.ConnectionResult;

import java.io.IOException;
import java.net.DatagramSocket;

public interface MessangerPresenter extends ChildPresenter{
    void start(DatagramSocket socket, ConnectionResult result) throws IOException;
    void stop();
}
