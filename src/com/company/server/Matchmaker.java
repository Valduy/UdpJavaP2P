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
import java.util.*;

/**
 * Класс, реализующий логику создания многопользовательских матчей.
 */
public class Matchmaker{
    // TODO: вести учет времени, в течение которого клиент не отправлял сообщений
    private final LinkedList<EndPoint> queue = new LinkedList<>();
    private final ArrayList<Match> matches = new ArrayList<>();
    private final HashMap<EndPoint, Match> playerToMatch = new HashMap<>();
    private final int playersPerMatch;

    private DatagramSocket socket;
    private Thread matchmakerThread;
    private int port;
    private boolean isRun;

    /**
     * <p>Возвращает статус, работает матчмейкер или нет.</p>
     * @return true, если матчмейкер работает.
     */
    public boolean getIsRun() {
        return isRun;
    }

    /**
     * <p>Возвращает UDP порт, на котором работает матчмейкер.</p>
     * @return порт, на котором работает матчмейкер.
     */
    public int getPort() {
        return port;
    }

    /**
     * Возвращает очередь клиентов, ожидающих матч.
     * @return список клиентов в очереди.
     */
    public Collection<EndPoint> getQueue(){
        return Collections.unmodifiableCollection(queue);
    }

    /**
     * <p>Конструктор матчмейкера.</p>
     * @param playersPerMatch Количество игроков, учавствующих в одном матче.
     */
    public Matchmaker(int playersPerMatch){
        this.playersPerMatch = playersPerMatch;
    }

    /**
     * <p>Метод запускает работу матчмейкера на случайном порту.</p>
     * @throws SocketException Исключение, возникающее при неудачной попытке открыть сокет.
     */
    public void start() throws SocketException {
        socket = new DatagramSocket();
        port = socket.getLocalPort();
        startLoop();
    }

    /**
     * <p>Метод запускает работу матчмейкера.</p>
     * @param port Порт, на котором матчмейкер будет принимать сообщения.
     * @throws SocketException Исключение, возникающее при неудачной попытке открыть сокет.
     */
    public void start(int port) throws SocketException {
        this.port = port;
        socket = new DatagramSocket(port);
        startLoop();
    }

    /**
     * <p>Метод запускает цикл матчмейкера.</p>
     */
    private void startLoop(){
        isRun = true;
        matchmakerThread = new Thread(this::matchmakerLoop);
        matchmakerThread.start();
        System.out.println("Матчмейкер запущен.\n");
    }

    /**
     * <p>Метод останавливает работу матчмейкера.</p>
     * @throws InterruptedException Исключение, возникающее, если не удалось дождаться завершения потока матчмейкера.
     */
    public void stop() throws InterruptedException {
        isRun = false;
        socket.close();
        matchmakerThread.join();

        for (var march : matches){
            march.stop();
        }

        System.out.println("Матчмейкер остановлен.");
    }

    /**
     * Метод реализует цикл матчмейкера.
     */
    private void matchmakerLoop(){
        while (isRun){
            matchmakerFrame();
        }
    }

    private void matchmakerFrame(){
        var receive = new byte[NetworkMessages.size];
        var packet = new DatagramPacket(receive, receive.length);

        try{
            System.out.println("Ожидание запросов...");
            socket.receive(packet);
            System.out.println("Сообщение получено!");
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
            if (!socket.isClosed()) {
                e.printStackTrace();
            }
        }

        tryCreateMatch();
    }

    private void onHello(InetAddress address, int port) throws IOException {
        System.out.printf("Привет от: %s:%s\n", address.toString(), port);

        if (getEndPoint(address, port) == null){
            queue.add(new EndPoint(address, port));
        }

        var message = MessageHelper.getMessage(NetworkMessages.HLLO);
        var packet = new DatagramPacket(message, 0, message.length, address, port);
        socket.send(packet);
    }

    private void onBye(InetAddress address, int port) throws IOException {
        System.out.printf("Пока от: %s:%s\n", address.toString(), port);
        var endPoint = getEndPoint(address, port);

        if (endPoint != null){
            queue.remove(endPoint);
        }

        var message = MessageHelper.getMessage(NetworkMessages.GBYE);
        var packet = new DatagramPacket(message, 0, message.length, address, port);
        socket.send(packet);
    }

    private void onInfo(InetAddress address, int port) throws IOException {
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
        socket.send(packet);
    }

    private void onInitial(InetAddress address, int port) throws IOException {
        System.out.printf("Запрос на инициализацию от: %s:%s\n", address.toString(), port);
        var match = playerToMatch.get(new EndPoint(address, port));

        if (match != null){
            var data = ByteBuffer.allocate(4).putInt(match.getPort()).array();
            var message = MessageHelper.getMessage(NetworkMessages.INIT, data);
            var packet = new DatagramPacket(message, 0, message.length, address, port);
            socket.send(packet);
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
        if (queue.size() >= playersPerMatch){
            System.out.println("Создание матча...");
            var players = new ArrayList<EndPoint>();

            for (int i = 0; i < playersPerMatch; i++){
                players.add(queue.remove(0));
            }

            var match = new Match(playersPerMatch);
            match.addEnded(this::onMatchEnded);
            matches.add(match);

            try {
                match.start();

                for (var player : players){
                    playerToMatch.put(player, match);
                }
            } catch (SocketException e) {
                e.printStackTrace();
            }

            System.out.println("Матч создан!");
        }
    }

    private void onMatchEnded(Object sender, EventArgs e){
        var match = (Match)sender;
        matches.remove(match);
        playerToMatch.values().removeIf(m -> m == match);
        match.removeEnded(this::onMatchEnded);
    }
}