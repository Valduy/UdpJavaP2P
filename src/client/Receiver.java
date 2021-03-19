package client;

import events.Event;
import events.EventHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.concurrent.Callable;

public class Receiver implements Callable<Void> {
    private final DatagramSocket socket;
    private final int packetSize;
    private boolean isRun;

    public boolean getIsRun(){
        return isRun;
    }

    private final EventHandler<ReceiveEventArgs> received = new EventHandler<>();

    public void addReceived(Event<ReceiveEventArgs> methodReference){
        received.subscribe(methodReference);
    }

    public void removeReceived(Event<ReceiveEventArgs> methodReference){
        received.unSubscribe(methodReference);
    }

    public Receiver(DatagramSocket socket, int packetSize) throws IOException {
        this.socket = socket;
        this.packetSize = packetSize;
    }

    @Override
    public Void call() throws Exception {
        isRun = true;
        receiveLoop();
        return null;
    }

    public void cancel() throws IOException {
        isRun = false;
    }

    private void receiveLoop() throws IOException {
        try {
            while (isRun){
                receiveFrame();
            }
        } catch (Exception e){
            throw e;
        }
    }

    private void receiveFrame() throws IOException {
        var buffer = new byte[packetSize];
        var packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet);
        received.invoke(this, new ReceiveEventArgs(packet.getData()));
    }
}
