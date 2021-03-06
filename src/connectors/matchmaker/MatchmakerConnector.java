package connectors.matchmaker;

import connectors.matchmaker.states.HelloState;
import connectors.matchmaker.states.MatchmakerConnectorState;
import events.Event;
import events.EventHandler;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.*;

public class MatchmakerConnector {
    private final EventHandler<ConnectionArgs> found = new EventHandler<>();

    private DatagramSocket client;
    private Future<Void> connectionFuture;
    private MatchmakerConnectorState state;

    private InetAddress serverAddress;
    private int serverPort;
    private int matchPort;
    private long time;
    private long endTime;
    private boolean isRun;

    public boolean getIsRun(){
        return isRun;
    }

    public InetAddress getServerAddress(){
        return serverAddress;
    }

    public int getServerPort(){
        return serverPort;
    }

    public int getMatchPort(){
        return matchPort;
    }

    public MatchmakerConnectorState getState(){
        return state;
    }

    public void setState(MatchmakerConnectorState state){
        this.state = state;
    }

    public void addFound(Event<ConnectionArgs> methodReference){
        found.subscribe(methodReference);
    }

    public void removeFound(Event<ConnectionArgs> methodReference){
        found.unSubscribe(methodReference);
    }

    public MatchmakerConnector(){

    }

    public void start(DatagramSocket client, InetAddress address, int port, long time)
            throws MatchmakerConnectorException
    {
        this.serverAddress = address;
        this.serverPort = port;
        this.client = client;
        this.time = time;
        state = new HelloState(this);

        try {
            var socketTimeout = 1000;
            client.setSoTimeout(socketTimeout);
        } catch (SocketException e) {
            throw new MatchmakerConnectorException("Не удалось установить таймаут сокета.", e);
        }

        isRun = true;
        var executor = Executors.newSingleThreadScheduledExecutor();
        endTime = System.currentTimeMillis() + time;
        connectionFuture = executor.schedule(() -> {
            connect();
            return null;
        }, 1, TimeUnit.SECONDS);
    }

    public void stop() throws MatchmakerConnectorException {
        isRun = false;

        try {
            client.setSoTimeout(0);
        } catch (SocketException e) {
            throw new MatchmakerConnectorException("Не удалось сбросить таймаут сокета.", e);
        } finally {
            stopConnectionTask();
        }
    }

    public void sendMessage(byte[] message) throws MatchmakerConnectorException {
        var packet = new DatagramPacket(message, message.length, serverAddress, serverPort);

        try {
            client.send(packet);
        } catch (IOException e) {
            throw new MatchmakerConnectorException("Не удалось отправить паке.", e);
        }
    }

    public void finishConnection(int matchPort) throws MatchmakerConnectorException {
        this.matchPort = matchPort;
        isRun = false;

        try {
            client.setSoTimeout(0);
        } catch (SocketException e) {
            throw new MatchmakerConnectorException("Не удалось сбросить таймаут сокета.", e);
        }

        found.invoke(this, new ConnectionArgs(matchPort));
        stopConnectionTask();
    }

    private void connect() throws MatchmakerConnectorException {
        state.send();

        try {
            var buffer = new byte[512];
            var packet = new DatagramPacket(buffer, buffer.length);
            client.receive(packet);
            state.processMessage(packet.getData());
        } catch (IOException e) {
            if (!(e.getCause() instanceof SocketTimeoutException)) {
                throw new MatchmakerConnectorException(e);
            }
        }

        if (System.currentTimeMillis() > endTime){
            var e = new TimeoutException();
            throw new MatchmakerConnectorException("Время на подключение истекло.", e);
        }
    }

    private void stopConnectionTask() throws MatchmakerConnectorException {
        try {
            connectionFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new MatchmakerConnectorException(e);
        }
    }
}
