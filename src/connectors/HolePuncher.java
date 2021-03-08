package connectors;

import com.company.network.*;
import events.Event;
import events.EventArgs;
import events.EventHandler;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.*;

public class HolePuncher {
    private DatagramSocket client;
    private P2PConnectionMessage connectionMessage;
    private ScheduledFuture<?> connectionFuture;
    private ArrayList<EndPoints> potencials;
    private HashSet<EndPoint> requesters;
    private HashSet<EndPoint> confirmed;
    private final byte[] checkedMessage;
    private final byte[] confirmedMessage;
    private boolean isRun;
    private int failureCount;
    private int allowedFailuresCount;

    public boolean getIsRun(){
        return isRun;
    }

    public int getAllowedFailuresCount(){
        return allowedFailuresCount;
    }

    public void setAllowedFailuresCount(int allowedFailuresCount){
        this.allowedFailuresCount = allowedFailuresCount;
    }

    public ArrayList<EndPoint> getClients(){
        return new ArrayList<>(confirmed);
    }

    private final EventHandler<EventArgs> punched = new EventHandler<>();

    public void addPunched(Event<EventArgs> methodReference){
        punched.subscribe(methodReference);
    }

    public void removePunched(Event<EventArgs> methodReference){
        punched.unSubscribe(methodReference);
    }

    public HolePuncher(){
        var checkedData = HolePunching.CHCK.toString().getBytes(StandardCharsets.US_ASCII);
        checkedMessage = MessageHelper.getMessage(NetworkMessages.CONN, checkedData);

        var approvedData = HolePunching.CONF.toString().getBytes(StandardCharsets.US_ASCII);
        confirmedMessage = MessageHelper.getMessage(NetworkMessages.CONN, approvedData);

        allowedFailuresCount = 10;
    }

    public void start(DatagramSocket client, P2PConnectionMessage connectionMessage){
        this.client = client;
        this.connectionMessage = connectionMessage;
        potencials = new ArrayList<>(connectionMessage.clients);
        requesters = new HashSet<>();
        confirmed = new HashSet<>();

        isRun = true;
        var executor = Executors.newSingleThreadScheduledExecutor();
        connectionFuture = executor.scheduleAtFixedRate(() ->{
            try {
                connect();
            } catch (ConnectorException e) {
                throw new RuntimeException(e);
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    public void stop() throws ConnectorException {
        isRun = false;

        try {
            stopConnectionTask();
            client.setSoTimeout(0);
        } catch (SocketException e) {
            throw new ConnectorException("Не удалось сбросить таймаут сокета.", e);
        }
    }

    private void connect() throws ConnectorException {
        send();

        try {
            var buffer = new byte[512];
            var packet = new DatagramPacket(buffer, buffer.length);
            client.receive(packet);
            failureCount = 0;
            processMessage(packet.getAddress(), packet.getPort(), packet.getData());
        } catch (IOException e) {
            if (e.getCause() instanceof SocketTimeoutException) {
                failureCount++;
            } else{
                throw new ConnectorException(e);
            }
        }

        if (failureCount >= allowedFailuresCount){
            var e = new TimeoutException();
            throw new ConnectorException("Не удается установить соединение с сервером", e);
        }
    }

    private void send() throws ConnectorException {
        for (var endPoints : potencials){
            sendMessage(endPoints.publicEndPoint, checkedMessage);
            sendMessage(endPoints.privateEndPoint, checkedMessage);
        }
        
        for (var endPoint : requesters){
            sendMessage(endPoint, confirmedMessage);
        }
    }

    private void processMessage(InetAddress address, int port, byte[] received) throws ConnectorException {
        if (MessageHelper.getMessageType(received) == NetworkMessages.CONN){
            var data = MessageHelper.toString(received).trim();
            var state = HolePunching.valueOf(data);
            var endPoint = new EndPoint(address, port);

            switch (state){
                case CHCK:
                    removeFromPotentials(endPoint);
                    requesters.add(endPoint);
                    break;
                case CONF:
                    requesters.remove(endPoint);
                    confirmed.add(endPoint);
                    break;
            }

            if (potencials.size() == 0 && requesters.size() == 0){
                finishConnection();
            }
        }
    }

    private void sendMessage(EndPoint endPoint, byte[] message) throws ConnectorException {
        try {
            var packet = new DatagramPacket(message, message.length, endPoint.address, endPoint.port);
            client.send(packet);
        } catch (IOException e) {
            throw new ConnectorException("Не удалось отправить пакет", e);
        }
    }

    private void finishConnection() throws ConnectorException {
        isRun = false;

        try {
            stopConnectionTask();
            client.setSoTimeout(0);
        } catch (SocketException e) {
            throw new ConnectorException("Не удалось сбросить таймаут сокета.", e);
        }

        punched.invoke(this, new EventArgs());
    }

    private void stopConnectionTask() {
        connectionFuture.cancel(true);
    }

    private void removeFromPotentials(EndPoint endPoint){
        var endPoints = potencials.stream()
                .filter(p -> p.publicEndPoint.equals(endPoint) || p.privateEndPoint.equals(endPoint))
                .findFirst()
                .orElse(null);

        if (endPoint != null) {
            potencials.remove(endPoints);
        }
    }
}
