package com.company.server;

import com.company.network.EndPoint;
import com.company.network.MessageHelper;
import com.company.network.NetworkMessages;
import com.company.network.UserStatus;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class Matchmaker{
    private final LinkedList<EndPoint> waitingPlayers = new LinkedList<>();
    private final HashMap<EndPoint, Integer> playerToMatch = new HashMap<>();
    private final ArrayList<Match> matches = new ArrayList<>();
    private DatagramSocket socket;
    private int port;

    private Thread matchmakerThread;
    private boolean isRun;

    public boolean getIsRun() {
        return isRun;
    }

    public int getPort() {
        return port;
    }

    public void start(int port) throws SocketException {
        this.port = port;
        socket = new DatagramSocket(port);
        isRun = true;
        System.out.print("Matchmaker started!");
        matchmakerThread = new Thread(this::matchmakerLoop);
    }

    public void stop() throws InterruptedException {
        try {
            isRun = false;
            matchmakerThread.join();
        } finally {
            socket.close();
        }
    }

    private void matchmakerLoop(){
        while (isRun){
            matchmakerFrame();
        }
    }

    private void matchmakerFrame(){
        var receive = new byte[NetworkMessages.size];
        var packet = new DatagramPacket(receive, receive.length);

        try{
            socket.receive(packet);
            var messageType = MessageHelper.getMessageType(packet.getData());
            var userAddress = packet.getAddress();
            var userPort = packet.getPort();

            switch (messageType){
                case Hello:
                    onHello(userAddress, userPort);
                    break;
                case Bye:
                    onBye(userAddress, userPort);
                    break;
                case Info:
                    onInfo(userAddress, userPort);
                    break;
                case Initial:
                    onInitial(userAddress, userPort);
                    break;
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }

        tryCreateMatch();
        checkMatches();
    }

    private void onHello(InetAddress address, int port) throws IOException {
        //System.out.printf("Hello from: %s:%s", address.toString(), port);

        if (getEndPoint(address, port) != null){
            waitingPlayers.add(new EndPoint(address, port));
        }

        var message = MessageHelper.getMessage(NetworkMessages.Hello);
        var packet = new DatagramPacket(message, 0, message.length, address, port);
        socket.send(packet);
    }

    private void onBye(InetAddress address, int port){
        var endPoint = getEndPoint(address, port);

        if (endPoint != null){
            waitingPlayers.remove(endPoint);
        }
    }

    private void onInfo(InetAddress address, int port) throws IOException {
        UserStatus status = UserStatus.Absent;

        if (getEndPoint(address, port) != null){
            status = UserStatus.Wait;
        }

        if (playerToMatch.containsKey(new EndPoint(address, port))){
            status = UserStatus.Connected;
        }

        var message = MessageHelper.getMessage(NetworkMessages.Info, status.label.getBytes(StandardCharsets.US_ASCII));
        var packet = new DatagramPacket(message, 0, message.length, address, port);
        socket.send(packet);
    }

    private void onInitial(InetAddress address, int port) throws IOException {
        var match = playerToMatch.get(new EndPoint(address, port));

        if (match != null){
            var message = MessageHelper.getMessage(NetworkMessages.Initial, ByteBuffer.allocate(4).putInt(match).array());
            var packet = new DatagramPacket(message, 0, message.length, address, port);
            socket.send(packet);
        }
    }

    private EndPoint getEndPoint(InetAddress address, int port){
        var endPoint = new EndPoint(address, port);

        return waitingPlayers
                .stream()
                .filter(o -> o.equals(endPoint))
                .findFirst()
                .orElse(null);
    }

    private void tryCreateMatch(){
        //TODO: создать матч (который еще написать бы)
    }

    private void checkMatches(){

    }
}