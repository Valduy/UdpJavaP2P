package com.company.server;

import com.company.network.EndPoint;
import com.company.network.MessageHelper;
import com.company.network.NetworkMessages;
import com.company.network.UserStatus;
import events.EventArgs;

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

/**
 * Класс, реализующий логику создания многопользовательских матчей.
 */
public class Matchmaker{
    private final LinkedList<EndPoint> waitingPlayers = new LinkedList<>();
    private final HashMap<EndPoint, Integer> playerToMatch = new HashMap<>();
    private final ArrayList<Match> matches = new ArrayList<>();
    private final int playersPerMatch;

    private DatagramSocket socket;
    private Thread matchmakerThread;
    private int port;
    private boolean isRun;

    public boolean getIsRun() {
        return isRun;
    }

    public int getPort() {
        return port;
    }

    /**
     * <p>Конструктор матчмейкера.</p>
     * @param playersPerMatch Количество игроков, учавствующих в одном матче.
     */
    public Matchmaker(int playersPerMatch){
        this.playersPerMatch = playersPerMatch;
    }

    /**
     * <p>Метод запускает работу матчмейкера.</p>
     * @param port Порт, на котором матчмейкер будет принимать сообщения.
     * @throws SocketException Исключение, возникающее при неудачной попытке открыть сокет.
     */
    public void start(int port) throws SocketException {
        this.port = port;
        socket = new DatagramSocket(port);
        isRun = true;
        System.out.print("Матчмейкер запущен.");
        matchmakerThread = new Thread(this::matchmakerLoop);
    }

    /**
     * <p>Метод останавливает работу матчмейкера.</p>
     * @throws InterruptedException Исключение, возникающее, если не удалось дождаться завершения потока матчмейкера.
     */
    public void stop() throws InterruptedException {
        try {
            isRun = false;
            matchmakerThread.join();
            System.out.print("Матчмейкер остановлен.");
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
            System.out.print("Waiting for request...");
            socket.receive(packet);
            System.out.print("Message received!");
            var messageType = MessageHelper.getMessageType(packet.getData());
            var userAddress = packet.getAddress();
            var userPort = packet.getPort();

            switch (messageType){
                case HLLO:
                    onHello(userAddress, userPort);
                    break;
                case GBYE:
                    onBye(userAddress, userPort);
                    break;
                case INFO:
                    onInfo(userAddress, userPort);
                    break;
                case INIT:
                    onInitial(userAddress, userPort);
                    break;
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }

        tryCreateMatch();
    }

    private void onHello(InetAddress address, int port) throws IOException {
        System.out.printf("Hello from: %s:%s", address.toString(), port);

        if (getEndPoint(address, port) != null){
            waitingPlayers.add(new EndPoint(address, port));
        }

        var message = MessageHelper.getMessage(NetworkMessages.HLLO);
        var packet = new DatagramPacket(message, 0, message.length, address, port);
        socket.send(packet);
    }

    private void onBye(InetAddress address, int port){
        System.out.printf("Bye from: %s:%s", address.toString(), port);
        var endPoint = getEndPoint(address, port);

        if (endPoint != null){
            waitingPlayers.remove(endPoint);
        }
    }

    private void onInfo(InetAddress address, int port) throws IOException {
        System.out.printf("Info request from: %s:%s", address.toString(), port);
        UserStatus status = UserStatus.ABSN;

        if (getEndPoint(address, port) != null){
            status = UserStatus.WAIT;
        }

        if (playerToMatch.containsKey(new EndPoint(address, port))){
            status = UserStatus.CONN;
        }

        var data = status.toString().getBytes(StandardCharsets.US_ASCII);
        var message = MessageHelper.getMessage(NetworkMessages.INFO, data);
        var packet = new DatagramPacket(message, 0, message.length, address, port);
        socket.send(packet);
    }

    private void onInitial(InetAddress address, int port) throws IOException {
        System.out.printf("Initial request from: %s:%s", address.toString(), port);
        var match = playerToMatch.get(new EndPoint(address, port));

        if (match != null){
            var message = MessageHelper.getMessage(NetworkMessages.INIT, ByteBuffer.allocate(4).putInt(match).array());
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
        if (waitingPlayers.size() >= playersPerMatch){
            System.out.print("Match creating...");
            var players = new ArrayList<EndPoint>();

            for (int i = 0; i < playersPerMatch; i++){
                players.add(waitingPlayers.remove(0));
            }

            var match = new Match(playersPerMatch);
            match.getEnded().subscribe(this::onMatchEnded);

            for (var player : players){
                playerToMatch.put(player, match.getPort());
            }

            try {
                match.start();
            } catch (SocketException e) {
                e.printStackTrace();
            }

            System.out.print("Match created!");
        }
    }

    private void onMatchEnded(Object sender, EventArgs e){
        var match = (Match)sender;
        playerToMatch.values().removeIf(v -> v == match.getPort());
        matches.remove(match);
    }
}