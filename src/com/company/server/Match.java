package com.company.server;

import com.company.network.EndPoints;
import com.company.server.states.MatchStateBase;
import com.company.server.states.WaitClientState;

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
    private EndPoints host;

    private final int playersCount;
    private final int port;
    private final int timeForConnection = 30 * 1000;

    private MatchStateBase state;
    private DatagramSocket socket;
    private Thread matchThread;

    private long endTime;

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

    public Match(int playersCount, int port){
        this.port = port;
        this.playersCount = playersCount;
    }

    public Match(int playersCount){
        this(playersCount, 0);
    }

    public void start() throws SocketException {
        socket = new DatagramSocket(port);
        state = new WaitClientState(this);
        isRun = true;
        endTime = System.currentTimeMillis() + timeForConnection;
        matchThread = new Thread(this::matchLoop);
    }

    public void stop() throws InterruptedException {
        try {
            isRun = false;
            matchThread.join();
        } finally {
            socket.close();
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

    private void matchLoop(){
        while (System.currentTimeMillis() < endTime){
            matchFrame();
        }

        isCompleted = true;
    }

    private void matchFrame(){
        var receive = new byte[128];
        var packet = new DatagramPacket(receive, receive.length);

        try {
            socket.receive(packet);
            state.processMessage(packet.getAddress(), packet.getPort(), packet.getData());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
