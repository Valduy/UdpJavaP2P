package client.presenters.implementations;

import client.presenters.interfaces.LoadPresenter;
import client.presenters.interfaces.MenuPresenter;
import client.presenters.interfaces.MessengerPresenter;
import client.presenters.interfaces.PongPresenter;
import client.services.interfaces.MessageBoxService;
import client.views.interfaces.MainView;
import events.EventArgs;

public class MainPresenter {
    private final MainView view;
    private final MenuPresenter menuPresenter;
    private final LoadPresenter loadPresenter;
    private final PongPresenter hostPresenter;
    private final PongPresenter clientPresenter;
    private final MessageBoxService messageBoxService;

    public MainPresenter(
            MainView view,
            MenuPresenter menuPresenter,
            LoadPresenter loadPresenter,
            PongPresenter hostPresenter,
            PongPresenter clientPresenter,
            MessageBoxService messageBoxService)
    {
        this.view = view;
        this.menuPresenter = menuPresenter;
        this.loadPresenter = loadPresenter;
        this.hostPresenter = hostPresenter;
        this.clientPresenter = clientPresenter;
        this.messageBoxService = messageBoxService;
        view.setComponent(menuPresenter.getView().toComponent());

        menuPresenter.addConnected(this::onMatchCreated);
        loadPresenter.addConnected(this::onConnected);
        hostPresenter.addEnded(this::onEnded);
        clientPresenter.addEnded(this::onEnded);
    }

    private void onMatchCreated(Object sender, EventArgs e){
        try {
            loadPresenter.connect(menuPresenter.getSocket(), menuPresenter.getMatchPort());
            view.setComponent(loadPresenter.getView().toComponent());
        } catch (Exception ex) {
            view.setComponent(menuPresenter.getView().toComponent());
            messageBoxService.showMessageDialog(ex.getMessage());
        }
    }

    private void onConnected(Object sender, EventArgs e){
        try {
            var message = loadPresenter.getConnectionMessage();

            switch (message.role){
                case Host:
                    hostPresenter.start(menuPresenter.getSocket(), message.clients);
                    view.setComponent(hostPresenter.getView().toComponent());
                    break;
                case Client:
                    clientPresenter.start(menuPresenter.getSocket(), message.clients);
                    view.setComponent(clientPresenter.getView().toComponent());
                    break;
            }
        } catch (Exception ex) {
            messageBoxService.showMessageDialog(ex.getMessage());
            view.setComponent(menuPresenter.getView().toComponent());
        }
    }

    private void onEnded(Object sender, EventArgs e){
        view.setComponent(menuPresenter.getView().toComponent());
    }
}
