package client.presenters.implementations;

import client.ConnectionResult;
import client.Settings;
import client.models.implementations.HolePunchingModel;
import client.models.interfaces.LoadModel;
import client.presenters.interfaces.LoadPresenter;
import client.views.interfaces.ChildView;
import client.views.interfaces.LoadView;
import connectors.ConnectorException;
import events.Event;
import events.EventArgs;

import java.net.DatagramSocket;

public class HolePunchingPresenter implements LoadPresenter {
    private final LoadView loadView;
    private final LoadModel loadModel;

    @Override
    public ChildView getView() {
        return null;
    }

    @Override
    public ConnectionResult getConnectionMessage() throws ConnectorException {
        return loadModel.getConnectionMessage();
    }

    @Override
    public void addConnected(Event<EventArgs> methodReference) {
        loadModel.addConnected(methodReference);
    }

    @Override
    public void removeConnected(Event<EventArgs> methodReference) {
        loadModel.removeConnected(methodReference);
    }

    public HolePunchingPresenter(LoadView loadView, LoadModel loadModel){
        this.loadView = loadView;
        this.loadModel = loadModel;
    }

    @Override
    public void connect(DatagramSocket socket, int port) throws ConnectorException {
        loadModel.startConnection(socket, Settings.serverAddress, port);
    }
}
