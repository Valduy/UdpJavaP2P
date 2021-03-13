package client.models.implementations;

import client.ConnectionResult;
import client.models.interfaces.LoadModel;
import connectors.ConnectorException;
import connectors.HolePuncher;
import connectors.matches.MatchConnector;
import events.Event;
import events.EventArgs;
import events.EventHandler;

import java.net.DatagramSocket;
import java.net.InetAddress;

public class HolePunchingModel implements LoadModel {
    private final EventHandler<EventArgs> connected = new EventHandler<>();
    //private final MatchConnector matchConnector = new MatchConnector();
    private final HolePuncher holePuncher = new HolePuncher();
    private DatagramSocket socket;

    @Override
    public ConnectionResult getConnectionMessage() throws ConnectorException {
        var result = new ConnectionResult();
        //result.role = matchConnector.getResult().role;
        result.clients = holePuncher.getClients();
        return result;
    }

    @Override
    public void addConnected(Event<EventArgs> methodReference) {
        connected.subscribe(methodReference);
    }

    @Override
    public void removeConnected(Event<EventArgs> methodReference) {
        connected.unSubscribe(methodReference);
    }

    public HolePunchingModel(){
        //matchConnector.addConnected(this::onConnectedToMatch);
        holePuncher.addPunched(this::onHolePunched);
    }

    @Override
    public void startConnection(DatagramSocket socket, InetAddress address, int port) throws ConnectorException {
        this.socket = socket;
        //matchConnector.start(socket, address, port);
    }

    private void onConnectedToMatch(Object sender, EventArgs e){
//        try {
//            holePuncher.start(socket, matchConnector.getResult());
//        } catch (ConnectorException connectorException) {
//            connected.invoke(this, new EventArgs());
//        }
    }

    private void onHolePunched(Object sender, EventArgs e){
        connected.invoke(this, new EventArgs());
    }
}
