package com.company.server.matchmakers;

import com.company.network.EndPoint;
import com.company.network.MessageHelper;
import com.company.network.NetworkMessages;
import com.company.network.UserStatus;
import com.company.server.matches.Match;
import com.company.server.matches.MatchException;
import events.EventArgs;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class Matchmaker implements Callable<Void> {
    private final LinkedList<EndPoint> queue = new LinkedList<>();
    private final HashMap<Match, Future<?>> matches = new HashMap<>();
    private final HashMap<EndPoint, Match> playerToMatch = new HashMap<>();
    private final int playersPerMatch;
    private final int port;

    private DatagramSocket socket;
    private boolean isRun;

    public boolean getIsRun() {
        return isRun;
    }

    public int getPort() {
        return port;
    }

    public Collection<EndPoint> getQueue(){
        return Collections.unmodifiableCollection(queue);
    }

    public Matchmaker(int playersPerMatch, int port){
        this.playersPerMatch = playersPerMatch;
        this.port = port;
    }

    @Override
    public Void call() throws MatchmakerException {
        tryCreateSocket();
        System.out.println("Матчмейкер запущен.\n");
        matchmakerLoop();
        return null;
    }

    public void cancel(){
        isRun = false;
        socket.close();
    }

    private void tryCreateSocket() throws MatchmakerException {
        try {
            socket = new DatagramSocket(port, InetAddress.getByName("0.0.0.0"));
        } catch (SocketException e) {
            throw new MatchmakerException("Не удалось открыть сокет.", e);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    private void matchmakerLoop() throws MatchmakerException {
        isRun = true;
        while (isRun){
            matchmakerFrame();
        }
    }

    private void matchmakerFrame() throws MatchmakerException {
        try {
            System.out.println("Ожидание запросов...");
            DatagramPacket packet = receivePacket();
            System.out.println("Сообщение получено!");
            processPacket(packet);
            tryCreateMatch();
            tryEndMatches();
        } catch (IOException e) {
            if (!socket.isClosed()){
                throw new MatchmakerException("Произошла ошибка во время попытки получить пакет.", e);
            }
        }
    }

    private DatagramPacket receivePacket() throws IOException {
        var receive = new byte[NetworkMessages.size];
        var packet = new DatagramPacket(receive, receive.length);
        socket.receive(packet);
        return packet;
    }

    private void processPacket(DatagramPacket packet) throws MatchmakerException {
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

    private void onHello(InetAddress address, int port) throws MatchmakerException {
        System.out.printf("Привет от: %s:%s\n", address.toString(), port);

        if (getEndPoint(address, port) == null){
            queue.add(new EndPoint(address, port));
        }

        var message = MessageHelper.getMessage(NetworkMessages.HLLO);
        var packet = new DatagramPacket(message, 0, message.length, address, port);

        try {
            socket.send(packet);
        } catch (IOException e) {
            throw new MatchmakerException(e);
        }
    }

    private void onBye(InetAddress address, int port) throws MatchmakerException {
        System.out.printf("Пока от: %s:%s\n", address.toString(), port);
        var endPoint = getEndPoint(address, port);

        if (endPoint != null){
            queue.remove(endPoint);
        }

        var message = MessageHelper.getMessage(NetworkMessages.GBYE);
        var packet = new DatagramPacket(message, 0, message.length, address, port);

        try {
            socket.send(packet);
        } catch (IOException e) {
            throw new MatchmakerException(e);
        }
    }

    private void onInfo(InetAddress address, int port) throws MatchmakerException {
        System.out.printf("Запрос на статус от: %s:%s\n", address.toString(), port);
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

        try {
            socket.send(packet);
        } catch (IOException e) {
            throw new MatchmakerException(e);
        }
    }

    private void onInitial(InetAddress address, int port) throws MatchmakerException {
        System.out.printf("Запрос на инициализацию от: %s:%s\n", address.toString(), port);
        var match = playerToMatch.get(new EndPoint(address, port));

        if (match != null){
            var data = ByteBuffer.allocate(4).putInt(match.getPort()).array();
            var message = MessageHelper.getMessage(NetworkMessages.INIT, data);
            var packet = new DatagramPacket(message, 0, message.length, address, port);

            try {
                socket.send(packet);
            } catch (IOException e) {
                throw new MatchmakerException(e);
            }
        }
    }

    private EndPoint getEndPoint(InetAddress address, int port){
        var endPoint = new EndPoint(address, port);

        return queue
                .stream()
                .filter(o -> o.equals(endPoint))
                .findFirst()
                .orElse(null);
    }

    private void tryCreateMatch(){
        try {
            if (queue.size() >= playersPerMatch){
                System.out.println("Создание матча...");
                var match = new Match(playersPerMatch, 30 * 1000);
                var players = new ArrayList<EndPoint>();

                for (int i = 0; i < playersPerMatch; i++){
                    players.add(queue.remove(0));
                }

                var executor = Executors.newSingleThreadExecutor();
                var future = executor.submit(match);
                matches.put(match, future);

                for (var player : players){
                    playerToMatch.put(player, match);
                }

                System.out.println("Матч создан!");
            }
        } catch (MatchException e) {
            System.out.print("Не удалось создать матч.");
        }
    }

    private void tryEndMatches(){
        var futures = matches.values().stream()
                .filter(Future::isDone)
                .collect(Collectors.toList());

        for (var future : futures){
            matches.values().removeIf(f -> f == future);

            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                System.out.println("Во время матча произошла ошибка...");
            }
        }
    }

    private void onMatchEnded(Object sender, EventArgs e){
        var match = (Match)sender;
        matches.remove(match);
        playerToMatch.values().removeIf(m -> m == match);
    }
}
