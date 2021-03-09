package client.presenters.implementations;

import client.presenters.interfaces.FieldPresenter;
import client.presenters.interfaces.LoadPresenter;
import client.presenters.interfaces.MenuPresenter;
import client.services.interfaces.MessageBoxService;
import client.views.interfaces.MainView;
import connectors.ConnectorException;
import events.EventArgs;

public class MainPresenter {
    private final MainView view;
    private final MenuPresenter menuPresenter;
    private final LoadPresenter loadPresenter;
    private final FieldPresenter fieldPresenter;
    private final MessageBoxService messageBoxService;

    public MainPresenter(
            MainView view,
            MenuPresenter menuPresenter,
            LoadPresenter loadPresenter,
            FieldPresenter fieldPresenter,
            MessageBoxService messageBoxService)
    {
        this.view = view;
        this.menuPresenter = menuPresenter;
        this.loadPresenter = loadPresenter;
        this.fieldPresenter = fieldPresenter;
        this.messageBoxService = messageBoxService;
        view.setComponent(menuPresenter.getView().toComponent());

        menuPresenter.addConnected(this::onMatchCreated);
        loadPresenter.addConnected(this::onConnected);
    }

    private void onMatchCreated(Object sender, EventArgs e){
        try {
            loadPresenter.connect(menuPresenter.getSocket(), menuPresenter.getMatchPort());
            view.setComponent(loadPresenter.getView().toComponent());
        } catch (ConnectorException connectorException) {
            view.setComponent(menuPresenter.getView().toComponent());
            messageBoxService.showMessageDialog(connectorException.getMessage());
        }
    }

    private void onConnected(Object sender, EventArgs e){

    }
}
