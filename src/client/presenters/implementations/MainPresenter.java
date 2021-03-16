package client.presenters.implementations;

import client.presenters.interfaces.LoadPresenter;
import client.presenters.interfaces.MenuPresenter;
import client.presenters.interfaces.MessengerPresenter;
import client.services.interfaces.MessageBoxService;
import client.views.interfaces.MainView;
import events.EventArgs;

public class MainPresenter {
    private final MainView view;
    private final MenuPresenter menuPresenter;
    private final LoadPresenter loadPresenter;
    //private final FieldPresenter fieldPresenter;
    private final MessengerPresenter messengerPresenter;
    private final MessageBoxService messageBoxService;

    public MainPresenter(
            MainView view,
            MenuPresenter menuPresenter,
            LoadPresenter loadPresenter,
            //FieldPresenter fieldPresenter,
            MessengerPresenter messangerPresenter,
            MessageBoxService messageBoxService)
    {
        this.view = view;
        this.menuPresenter = menuPresenter;
        this.loadPresenter = loadPresenter;
        //this.fieldPresenter = fieldPresenter;
        this.messengerPresenter = messangerPresenter;
        this.messageBoxService = messageBoxService;
        view.setComponent(menuPresenter.getView().toComponent());

        menuPresenter.addConnected(this::onMatchCreated);
        loadPresenter.addConnected(this::onConnected);
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
            messengerPresenter.start(menuPresenter.getSocket(), loadPresenter.getConnectionMessage());
            view.setComponent(messengerPresenter.getView().toComponent());
        } catch (Exception ex) {
            messageBoxService.showMessageDialog(ex.getMessage());
        }
    }
}
