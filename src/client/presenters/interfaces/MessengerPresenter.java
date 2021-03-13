package client.presenters.interfaces;

import client.ConnectionResult;

import java.net.DatagramSocket;

public interface MessengerPresenter extends ChildPresenter{
    void start(DatagramSocket socket, ConnectionResult result) throws Exception;
    void stop();
}
