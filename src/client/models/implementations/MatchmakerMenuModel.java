package client.models.implementations;

import client.Settings;
import client.models.interfaces.MenuModel;
import connectors.ConnectorException;
import connectors.matchmakers.MatchmakerConnector;
import events.Event;
import events.EventArgs;

import javax.swing.*;
import java.net.DatagramSocket;

public class MatchmakerMenuModel implements MenuModel {
    private class MatchmakerConnectorTask extends SwingWorker<Void, Integer>{
        private final MatchmakerConnector connector = new MatchmakerConnector();
        private final DatagramSocket socket;

        public MatchmakerConnectorTask(DatagramSocket socket){
            this.socket = socket;
        }

        @Override
        protected Void doInBackground() throws Exception {
            connector.start(socket, Settings.serverAddress, Settings.serverPort);
            return null;
        }
    }

    private final MatchmakerConnector connector = new MatchmakerConnector();

    @Override
    public int getMatchPort() throws ConnectorException {
        return connector.getResult();
    }

    @Override
    public void addConnected(Event<EventArgs> methodReference) {
        connector.addConnected(methodReference);
    }

    @Override
    public void removeConnected(Event<EventArgs> methodReference) {
        connector.removeConnected(methodReference);
    }

    @Override
    public void startSearch(DatagramSocket client) throws ConnectorException {
        connector.start(client, Settings.serverAddress, Settings.serverPort);
    }

    @Override
    public void stopSearch() throws ConnectorException {
        connector.stop();
    }
}
