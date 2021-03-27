package client;

import client.models.implementations.HolePunchingModel;
import client.models.implementations.MatchmakerMenuModel;
import client.models.implementations.TestMessengerModel;
import client.presenters.implementations.*;
import client.services.implementations.SwingMessageBoxSerivice;
import client.views.implementations.*;

import javax.swing.*;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Main {
    private static final int fieldWidth = 600;
    private static final int fieldHeight = 450;

    public static void main(String[] args) {
        if (args.length > 0){
            try {
                Settings.serverAddress = InetAddress.getByName(args[0]);
                Settings.serverPort = Integer.parseInt(args[1]);
                initialize();
            } catch (UnknownHostException e) {
                System.out.printf("Неверный формат адреса: %s.\n", args[0]);
            }
            catch (NumberFormatException e) {
                System.out.printf("Некорректный порт: %s.\n", args[1]);
            }
        }
        else {
            initialize();
        }
    }

    private static void initialize(){
        SwingUtilities.invokeLater(() -> {
            var mainFrame = new MainFrame(fieldWidth + 30, fieldHeight + 30);
            var messageBoxService = new SwingMessageBoxSerivice();

            var menuView = new Menu();
            var menuModel = new MatchmakerMenuModel();
            var menuPresenter = new MatchmakerMenuPresenter(menuView, menuModel, messageBoxService);

            var loadView = new Loading();
            var loadModel = new HolePunchingModel();
            var loadPresenter = new HolePunchingPresenter(loadView, loadModel);

            var hostView = new PongView(fieldWidth, fieldHeight);
            var hostPresenter = new HostPongPresenter(messageBoxService, hostView, fieldWidth, fieldHeight);

            var clientView = new PongView(fieldWidth, fieldHeight);
            var clientPresenter = new ClientPongPresenter(messageBoxService, clientView, fieldWidth, fieldHeight);

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
