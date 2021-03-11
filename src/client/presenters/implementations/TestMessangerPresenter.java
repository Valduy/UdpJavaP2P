package client.presenters.implementations;

import client.ConnectionResult;
import client.models.interfaces.MessangerModel;
import client.presenters.interfaces.MessangerPresenter;
import client.services.interfaces.MessageBoxService;
import client.views.interfaces.ChildView;
import client.views.interfaces.MessangerView;

import java.io.IOException;
import java.net.DatagramSocket;

public class TestMessangerPresenter implements MessangerPresenter {
    private final MessangerView view;
    private final MessangerModel model;
    private final MessageBoxService messageBoxService;

    @Override
    public ChildView getView() {
        return view;
    }

    public TestMessangerPresenter(
            MessangerView view,
            MessangerModel model,
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
    public void start(DatagramSocket socket, ConnectionResult result) throws IOException {
        model.start(socket, result);
    }

    @Override
    public void stop() {
        model.stop();
    }
}
