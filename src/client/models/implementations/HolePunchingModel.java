package client.models.implementations;

import client.ConnectionResult;
import client.Settings;
import client.models.interfaces.LoadModel;
import com.company.network.EndPoint;
import com.company.network.P2PConnectionMessage;
import connectors.ConnectorException;
import connectors.HolePuncher;
import connectors.matches.MatchConnector;
import events.Event;
import events.EventArgs;
import events.EventHandler;

import javax.swing.*;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class HolePunchingModel implements LoadModel {
    private class ConnectionTask extends SwingWorker<ConnectionResult, Void>{
        private final DatagramSocket socket;
        private final InetAddress address;
        private final int port;
        private final EventHandler<EventArgs> done = new EventHandler<>();

        public void addDone(Event<EventArgs> methodReference){
            done.subscribe(methodReference);
        }

        public void removeDone(Event<EventArgs> methodReference){
            done.unSubscribe(methodReference);
        }

        private ConnectionTask(DatagramSocket socket, InetAddress address, int port) throws Exception {
            this.socket = socket;
            this.address = address;
            this.port = port;
        }

        @Override
        protected ConnectionResult doInBackground() throws Exception {
            var connector = new MatchConnector(socket, address, port);
            var message = connector.call();
            var puncher = new HolePuncher(socket, message);
            var clients = puncher.call();
            var result = new ConnectionResult();
            result.role = message.role;
            result.clients = clients;
            return result;
        }

        @Override
        protected void done() {
            done.invoke(this, new EventArgs());
        }
    }

    private ConnectionTask connectionTask;

    private final EventHandler<EventArgs> connected = new EventHandler<>();

    @Override
    public void addConnected(Event<EventArgs> methodReference) {
        connected.subscribe(methodReference);
    }

    @Override
    public void removeConnected(Event<EventArgs> methodReference) {
        connected.unSubscribe(methodReference);
    }

    @Override
    public ConnectionResult getConnectionMessage() throws Exception {
        return connectionTask.get();
    }

    @Override
    public void startConnection(DatagramSocket socket, InetAddress address, int port) throws Exception {
        connectionTask = new ConnectionTask(socket, address, port);
        connectionTask.addDone(this::onDone);
        connectionTask.execute();
    }

    private void onDone(Object sender, EventArgs e){
        connectionTask.removeDone(this::onDone);
        connected.invoke(this, new EventArgs());
    }
}
