package client;

import client.models.implementations.HolePunchingModel;
import client.models.implementations.MatchmakerMenuModel;
import client.models.implementations.TestMessangerModel;
import client.presenters.implementations.HolePunchingPresenter;
import client.presenters.implementations.MainPresenter;
import client.presenters.implementations.MatchmakerMenuPresenter;
import client.presenters.implementations.TestMessangerPresenter;
import client.services.implementations.SwingMessageBoxSerivice;
import client.services.interfaces.MessageBoxService;
import client.views.implementations.Loading;
import client.views.implementations.MainFrame;
import client.views.implementations.Menu;
import client.views.implementations.Messanger;
import client.views.interfaces.ChildView;

public class Main {
    public static void main(String[] args) {
        //new MainFrame();
        initialize();
    }

    private static void initialize(){
        var messageBoxSerivce = new SwingMessageBoxSerivice();

        var menuView = new Menu();
        var menuModel = new MatchmakerMenuModel();
        var menuPresenter = new MatchmakerMenuPresenter(menuView, menuModel, messageBoxSerivce);

        var loadView = new Loading();
        var loadModel = new HolePunchingModel();
        var loadPresenter = new HolePunchingPresenter(loadView, loadModel);

        var messangerView = new Messanger();
        var messangerModel = new TestMessangerModel();
        var messangerPresenter = new TestMessangerPresenter(messangerView, messangerModel, messageBoxSerivce);

        var mainFrame = new MainFrame();
        var mainPresenter = new MainPresenter(
                mainFrame,
                menuPresenter,
                loadPresenter,
                messangerPresenter,
                messageBoxSerivce);
    }
}
