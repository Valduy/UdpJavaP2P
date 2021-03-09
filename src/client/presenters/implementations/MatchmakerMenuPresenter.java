package client.presenters.implementations;

import client.models.interfaces.MenuModel;
import client.presenters.interfaces.MenuPresenter;
import client.services.interfaces.MessageBoxService;
import client.views.interfaces.ChildView;
import client.views.interfaces.MenuView;
import connectors.ConnectorException;
import events.Event;
import events.EventArgs;

import java.net.DatagramSocket;
import java.net.SocketException;

public class MatchmakerMenuPresenter implements MenuPresenter {
    private final MenuView view;
    private final MenuModel model;
    private final MessageBoxService messageBoxService;

    private DatagramSocket socket;

    @Override
    public DatagramSocket getSocket() {
        return socket;
    }

    public int getMatchPort() throws ConnectorException {
        return model.getMatchPort();
    }

    @Override
    public void addConnected(Event<EventArgs> methodReference) {
        model.addConnected(methodReference);
    }

    @Override
    public void removeConnected(Event<EventArgs> methodReference) {
        model.removeConnected(methodReference);
    }

    public MatchmakerMenuPresenter(MenuView view, MenuModel model, MessageBoxService messageBoxService){
        this.view = view;
        this.model = model;
        this.messageBoxService = messageBoxService;

        view.addSearchClicked(this::onSearchClicked);
        view.addStopClicked(this::onStopClicked);
    }

    @Override
    public ChildView getView() {
        return view;
    }

    private void onSearchClicked(Object sender, EventArgs e){
        try {
            socket = new DatagramSocket();
            model.startSearch(socket);
        } catch (SocketException | ConnectorException ex) {
            messageBoxService.showMessageDialog(ex.getMessage());
        }
    }

    private void onStopClicked(Object sender, EventArgs e){
        try {
            model.stopSearch();
        } catch (ConnectorException ex) {
            messageBoxService.showMessageDialog(ex.getMessage());
        }
    }
}
