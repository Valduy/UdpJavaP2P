package client;

import client.models.implementations.HolePunchingModel;
import client.models.implementations.MatchmakerMenuModel;
import client.models.implementations.TestMessengerModel;
import client.presenters.implementations.HolePunchingPresenter;
import client.presenters.implementations.MainPresenter;
import client.presenters.implementations.MatchmakerMenuPresenter;
import client.presenters.implementations.TestMessangerPresenter;
import client.services.implementations.SwingMessageBoxSerivice;
import client.views.implementations.Loading;
import client.views.implementations.MainFrame;
import client.views.implementations.Menu;
import client.views.implementations.Messenger;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        initialize();
    }

    private static void initialize(){
        SwingUtilities.invokeLater(() -> {
            var messageBoxSerivce = new SwingMessageBoxSerivice();

            var menuView = new Menu();
            var menuModel = new MatchmakerMenuModel();
            var menuPresenter = new MatchmakerMenuPresenter(menuView, menuModel, messageBoxSerivce);

            var loadView = new Loading();
            var loadModel = new HolePunchingModel();
            var loadPresenter = new HolePunchingPresenter(loadView, loadModel);

            var messangerView = new Messenger();
            var messangerModel = new TestMessengerModel();
            var messangerPresenter = new TestMessangerPresenter(messangerView, messangerModel, messageBoxSerivce);

            var mainFrame = new MainFrame();
            var mainPresenter = new MainPresenter(
                    mainFrame,
                    menuPresenter,
                    loadPresenter,
                    messangerPresenter,
                    messageBoxSerivce);
        });
    }
}
