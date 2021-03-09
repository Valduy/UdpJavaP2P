package client.presenters.implementations;

import client.models.interfaces.MenuModel;
import client.presenters.interfaces.MenuPresenter;
import client.services.interfaces.MessageBoxService;
import client.views.interfaces.ChildView;
import client.views.interfaces.MenuView;
import connectors.ConnectorException;
import events.Event;
import events.EventArgs;
import events.EventHandler;

import java.net.DatagramSocket;
import java.net.SocketException;

public class MatchmakerMenuPresenter implements MenuPresenter {
    private final MenuView view;
    private final MenuModel model;
    private final MessageBoxService messageBoxService;

    private DatagramSocket socket;

    @Override
    public ChildView getView() {
        return view;
    }

    @Override
    public DatagramSocket getSocket() {
        return socket;
    }

    public int getMatchPort() throws ConnectorException {
        return model.getMatchPort();
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

    public MatchmakerMenuPresenter(MenuView view, MenuModel model, MessageBoxService messageBoxService){
        this.view = view;
        this.model = model;
        this.messageBoxService = messageBoxService;

        view.addSearchClicked(this::onSearchClicked);
        view.addStopClicked(this::onStopClicked);
        model.addConnected(this::onConnected);
    }

    private void onSearchClicked(Object sender, EventArgs e){
        try {
            socket = new DatagramSocket();
            model.startSearch(socket);
            view.setIsInSearch(true);
        } catch (SocketException | ConnectorException ex) {
            messageBoxService.showMessageDialog(ex.getMessage());
        }
    }

    private void onStopClicked(Object sender, EventArgs e){
        try {
            model.stopSearch();
            view.setIsInSearch(false);
        } catch (ConnectorException ex) {
            messageBoxService.showMessageDialog(ex.getMessage());
        }
    }

    private void onConnected(Object sender, EventArgs e){
        view.setIsInSearch(false);
        connected.invoke(this, e);
    }
}
