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
    private Exception exception;
    private DatagramSocket client;
    private P2PConnectionMessage connectionMessage;
    private Future<?> connectionFuture;
    private ArrayList<EndPoints> potencials;
    private HashSet<EndPoint> requesters;
    private HashSet<EndPoint> confirmed;
    private ArrayList<EndPoint> result;
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

    public ArrayList<EndPoint> getClients() throws ConnectorException {
        if (result == null){
            throw new ConnectorException("Не удалось произвести соединение с клиентами.", exception);
        }

        return result;
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
        System.out.println("Холпанчер запущен");
        this.client = client;
        this.connectionMessage = connectionMessage;
        potencials = new ArrayList<>(connectionMessage.clients);
        requesters = new HashSet<>();
        confirmed = new HashSet<>();
        result = null;

        isRun = true;
        var executor = Executors.newSingleThreadExecutor();
        connectionFuture = executor.submit(() ->{
            try {
                connect();
            } catch (ConnectorException e) {
                exception = e;
                punched.invoke(this, new EventArgs());
                throw new RuntimeException(e);
            }
        });
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
        try {
            client.setSoTimeout(100);
        } catch (SocketException e) {
            throw new ConnectorException("Не удалось изменить таймаут сокета.", e);
        }

        while (isRun){
            try {
                Thread.sleep(1000);
                send();
                var buffer = new byte[512];
                var packet = new DatagramPacket(buffer, buffer.length);
                System.out.println("Жду сообщений...");
                client.receive(packet);
                failureCount = 0;
                processMessage(packet.getAddress(), packet.getPort(), packet.getData());

            } catch (IOException | InterruptedException e) {
                if (e instanceof SocketTimeoutException) {
                    System.out.print("Кажется, никто нам не пишет(((.");
                    failureCount++;
                } else{
                    throw new ConnectorException(e);
                }
            }

            if (failureCount >= allowedFailuresCount){
                System.out.println("Давно не получал сообщений. Видимо, соединиться невозможно...");
                var e = new TimeoutException();
                throw new ConnectorException("Не удается установить соединение с сервером", e);
            }
        }
    }

    private void send() throws ConnectorException {
        System.out.println("Отсылаю пакеты всем потенциальным клиентам...");

        for (var endPoints : potencials){
            sendMessage(endPoints.publicEndPoint, checkedMessage);
            sendMessage(endPoints.privateEndPoint, checkedMessage);
        }
        
        for (var endPoint : requesters){
            sendMessage(endPoint, confirmedMessage);
        }
    }

    private void processMessage(InetAddress address, int port, byte[] received) throws ConnectorException {
        System.out.println("Попытаюсь обработать полученное сообщение...");

        if (MessageHelper.getMessageType(received) == NetworkMessages.CONN){
            var data = MessageHelper.toString(received).trim();
            var state = HolePunching.valueOf(data);
            var endPoint = new EndPoint(address, port);

            switch (state){
                case CHCK:
                    System.out.printf("Проверка от %s:%s\n", endPoint.address, endPoint.port);
                    removeFromPotentials(endPoint);
                    requesters.add(endPoint);
                    break;
                case CONF:
                    System.out.printf("Подтверждение от %s:%s\n", endPoint.address, endPoint.port);
                    removeFromPotentials(endPoint);
                    requesters.remove(endPoint);
                    confirmed.add(endPoint);
                    break;
            }

            if (potencials.size() == 0 && requesters.size() == 0){
                System.out.print("Удалось соединиться со всеми клиентами!");
                result = new ArrayList<>(confirmed);
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
            //stopConnectionTask();
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
