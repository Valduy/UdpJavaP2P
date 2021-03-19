package client;

import client.models.implementations.HolePunchingModel;
import client.models.implementations.MatchmakerMenuModel;
import client.models.implementations.TestMessengerModel;
import client.presenters.implementations.*;
import client.services.implementations.SwingMessageBoxSerivice;
import client.views.implementations.*;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        initialize();
    }

    private static void initialize(){
        SwingUtilities.invokeLater(() -> {
            var messageBoxService = new SwingMessageBoxSerivice();

            var menuView = new Menu();
            var menuModel = new MatchmakerMenuModel();
            var menuPresenter = new MatchmakerMenuPresenter(menuView, menuModel, messageBoxService);

            var loadView = new Loading();
            var loadModel = new HolePunchingModel();
            var loadPresenter = new HolePunchingPresenter(loadView, loadModel);

            var hostView = new PongView();
            var hostPresenter = new HostPongPresenter(hostView, 600, 450);

            var clientView = new PongView();
            var clientPresenter = new ClientPongPresenter(clientView, 600, 450);

            var mainFrame = new MainFrame();
            var mainPresenter = new MainPresenter(
                    mainFrame,
                    menuPresenter,
                    loadPresenter,
                    hostPresenter,
                    clientPresenter,
                    messageBoxService);
        });
    }
}
