package com.company.server;

import com.company.network.EndPoints;
import com.company.server.states.MatchStateBase;
import com.company.server.states.WaitClientState;
import events.Event;
import events.EventArgs;
import events.EventHandler;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class Match {
    private final ArrayList<EndPoints> clients = new ArrayList<>();
    private final EventHandler<EventArgs> ended = new EventHandler<>();
    private EndPoints host;

    private final int playersCount;
    private int port;
    private final int timeForConnection = 30 * 1000;

    private MatchStateBase state;
    private DatagramSocket socket;
    private Thread matchThread;

    private boolean isCompleted;
    private boolean isRun;

    public Collection<EndPoints> getClients(){
        return Collections.unmodifiableCollection(clients);
    }

    public EndPoints getHost(){
        return host;
    }

    public void setHost(EndPoints endPoints){
        clients.remove(endPoints);
        host = endPoints;
    }

    public void setState(MatchStateBase state){
        this.state = state;
    }

    public boolean getIsCompleted(){
        return isCompleted;
    }

    public boolean getIsRun(){
        return isRun;
    }

    public boolean getIsFull(){
        return host == null
                ? clients.size() == playersCount
                : clients.size() == playersCount - 1;
    }

    public int getPort(){
        return port;
    }

    public int getPlayersCount(){
        return playersCount;
    }

    public void addEnded(Event<EventArgs> methodReference){
        ended.subscribe(methodReference);
    }

    public void removeEnded(Event<EventArgs> methodReference){
        ended.unSubscribe(methodReference);
    }

    public Match(int playersCount){
        this.playersCount = playersCount;
    }

    public void start() throws SocketException {
        start(0);
    }

    public void start(int port) throws SocketException {
        socket = new DatagramSocket(port);
        this.port = socket.getLocalPort();
        state = new WaitClientState(this);
        isRun = true;
        isCompleted = false;
        matchThread = new Thread(this::matchLoop);
        matchThread.start();
    }

    public void stop() throws InterruptedException {
        isRun = false;
        socket.close();
        matchThread.join();
    }

    public void sendMessage(InetAddress address, int port, byte[] message) {
        try {
            var packet = new DatagramPacket(message, message.length, address, port);
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addClient(EndPoints endPoints){
        clients.add(endPoints);
    }

    private void matchLoop(){
        var endTime = System.currentTimeMillis() + timeForConnection;

        while (System.currentTimeMillis() < endTime && isRun){
            matchFrame();
        }

        isRun = false;
        isCompleted = true;
        socket.close();
        ended.invoke(this, new EventArgs());
    }

    private void matchFrame(){
        var receive = new byte[128];
        var packet = new DatagramPacket(receive, receive.length);

        try {
            socket.receive(packet);
            state.processMessage(packet.getAddress(), packet.getPort(), packet.getData());
        } catch (IOException e) {
            if (!socket.isClosed()) {
                e.printStackTrace();
            }
        }
    }
}
