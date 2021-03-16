package client.models.implementations;

import client.Settings;
import client.models.interfaces.MenuModel;
import connectors.matchmakers.MatchmakerConnector;
import events.Event;
import events.EventArgs;
import events.EventHandler;

import javax.swing.*;
import java.net.DatagramSocket;
import java.util.concurrent.ExecutionException;

public class MatchmakerMenuModel implements MenuModel {
    private class MatchmakerConnectorTask extends SwingWorker<Integer, Void>{
        private final MatchmakerConnector connector;
        private final EventHandler<EventArgs> done = new EventHandler<>();

        public void addDone(Event<EventArgs> methodReference){
            done.subscribe(methodReference);
        }

        public void removeDone(Event<EventArgs> methodReference){
            done.unSubscribe(methodReference);
        }

        public MatchmakerConnectorTask(DatagramSocket socket){
            this.connector = new MatchmakerConnector(socket, Settings.serverAddress, Settings.serverPort);
        }

        @Override
        protected Integer doInBackground() throws Exception {
            return connector.call();
        }

        @Override
        protected void done() {
            done.invoke(this, new EventArgs());
        }
    }

    private MatchmakerConnectorTask connectorTask;

    @Override
    public int getMatchPort() throws ExecutionException, InterruptedException {
        return connectorTask.get();
    }

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
    public void startSearch(DatagramSocket client) {
        connectorTask = new MatchmakerConnectorTask(client);
        connectorTask.addDone(this::onDone);
        connectorTask.execute();
    }

    @Override
    public void stopSearch() {
        connectorTask.cancel(true);
        connectorTask.removeDone(this::onDone);
        connectorTask = null;
    }

    private void onDone(Object sender, EventArgs e){
        connectorTask.removeDone(this::onDone);
        connected.invoke(this, new EventArgs());
    }
}
