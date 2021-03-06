package com.company.server.matches;

import com.company.network.EndPoints;
import com.company.server.matches.states.MatchStateBase;
import com.company.server.matches.states.WaitClientState;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Match {
    private final ArrayList<EndPoints> clients = new ArrayList<>();
    private final EventHandler<EventArgs> ended = new EventHandler<>();
    private EndPoints host;

    private final int playersCount;
    private int port;

    private MatchStateBase state;
    private DatagramSocket socket;
    private Future<Void> matchFuture;

    private long endTime;
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

    public void start(long time) throws MatchException {
        start(0, time);
    }

    public void start(int port, long time) throws MatchException {
        try {
            socket = new DatagramSocket(port);
        } catch (SocketException e) {
            throw new MatchException("Не удалось создать сокет.", e);
        }

        this.port = socket.getLocalPort();
        state = new WaitClientState(this);
        isRun = true;
        var executor = Executors.newSingleThreadExecutor();
        endTime = System.currentTimeMillis() + time;
        matchFuture = executor.submit(() -> {
            matchLoop();
            return null;
        });
    }

    public void stop() throws MatchException {
        isRun = false;

        try {
            matchFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new MatchException(e);
        }
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

    private void matchLoop() throws MatchException {
        while (System.currentTimeMillis() < endTime && isRun){
            matchFrame();
        }

        isRun = false;
        socket.close();
        ended.invoke(this, new EventArgs());
    }

    private void matchFrame() throws MatchException {
        var receive = new byte[128];
        var packet = new DatagramPacket(receive, receive.length);

        try {
            socket.receive(packet);
            state.processMessage(packet.getAddress(), packet.getPort(), packet.getData());
        } catch (IOException e) {
            throw new MatchException(e);
        }
    }
}
