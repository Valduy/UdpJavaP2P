package client.models.implementations;

import client.ConnectionResult;
import client.models.interfaces.MessangerModel;
import com.company.network.MessageHelper;
import com.company.network.NetworkMessages;
import com.company.network.Role;
import events.Event;
import events.EventArgs;
import events.EventHandler;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.Executors;

public class TestMessangerModel implements MessangerModel {
    private final EventHandler<EventArgs> received = new EventHandler<>();
    private DatagramSocket socket;
    private ConnectionResult result;
    private String lastMessage;

    @Override
    public String getLastMessage(){
        return lastMessage;
    }

    @Override
    public void addReceived(Event<EventArgs> methodReference) {
        received.subscribe(methodReference);
    }

    @Override
    public void removeReceived(Event<EventArgs> methodReference) {
        received.unSubscribe(methodReference);
    }

    @Override
    public void start(DatagramSocket socket, ConnectionResult result) throws IOException {
        this.socket = socket;
        this.result = result;

        if (result.role == Role.Client){
            startReceiveLoop();
        }
    }

    @Override
    public void stop()
    {
        //TODO
    }

    @Override
    public void send(String data) throws IOException {
        var message = MessageHelper.getMessage(NetworkMessages.INFO, data);

        for (var clinet : result.clients){
            var packet = new DatagramPacket(message, message.length, clinet.address, clinet.port);
            socket.send(packet);
        }
    }

    private void startReceiveLoop(){
        var executor = Executors.newSingleThreadExecutor();
        executor.submit(() ->{
            while (true){
                var buffer = new byte[512];
                var packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                lastMessage = MessageHelper.toString(packet.getData());
                received.invoke(this, new EventArgs());
            }
        });
    }
}
