package client.presenters.implementations;

import client.ConnectionResult;
import client.models.interfaces.MessengerModel;
import client.presenters.interfaces.MessengerPresenter;
import client.services.interfaces.MessageBoxService;
import client.views.interfaces.ChildView;
import client.views.interfaces.MessangerView;

import java.io.IOException;
import java.net.DatagramSocket;

public class TestMessangerPresenter implements MessengerPresenter {
    private final MessangerView view;
    private final MessengerModel model;
    private final MessageBoxService messageBoxService;

    @Override
    public ChildView getView() {
        return view;
    }

    public TestMessangerPresenter(
            MessangerView view,
            MessengerModel model,
            MessageBoxService messageBoxService)
    {
        this.view = view;
        this.model = model;
        this.messageBoxService = messageBoxService;

        view.addMessaged((o, e) -> {
            try {
                model.send(view.getLastMessage());
            } catch (IOException ioException) {
                messageBoxService.showMessageDialog(ioException.getMessage());
            }
        });

        model.addReceived((o, e) -> {
            view.addMessage(model.getLastMessage());
        });
    }

    @Override
    public void start(DatagramSocket socket, ConnectionResult result) throws Exception {
        model.start(socket, result);
    }

    @Override
    public void stop() {
        model.stop();
    }
}
