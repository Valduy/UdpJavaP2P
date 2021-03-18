package client;

import events.Event;
import events.EventHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.Callable;

public class Receiver implements Callable<Void> {
    private final Socket socket;
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

    public Receiver(int port, int packetSize) throws IOException {
        this.packetSize = packetSize;
        socket = new Socket();
        var socketAddress = new InetSocketAddress(port);
        socket.setReceiveBufferSize(packetSize * 10);
        socket.bind(socketAddress);
    }

    @Override
    public Void call() throws Exception {
        isRun = true;
        receiveLoop();
        return null;
    }

    public void cancel() throws IOException {
        isRun = false;
        socket.close();
    }

    private void receiveLoop() throws IOException {
        while (isRun){
            receiveFrame();
        }
    }

    private void receiveFrame() throws IOException {
        try {
            var in = socket.getInputStream();
            var buffer = new byte[packetSize];

            while (in.read(buffer) != -1){
                received.invoke(this, new ReceiveEventArgs(buffer));
                buffer = new byte[packetSize];
            }
        } catch (IOException e){
            if (!socket.isClosed()){
                throw e;
            }
        }

    }
}
