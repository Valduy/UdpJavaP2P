package connectors;

import com.company.network.*;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.*;

public class HolePuncher implements Callable<ArrayList<EndPoint>> {
    private DatagramSocket client;
    private P2PConnectionMessage connectionMessage;
    private ArrayList<EndPoints> potentials;
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

    public HolePuncher(DatagramSocket client, P2PConnectionMessage connectionMessage){
        var checkedData = HolePunching.CHCK.toString().getBytes(StandardCharsets.US_ASCII);
        checkedMessage = MessageHelper.getMessage(NetworkMessages.CONN, checkedData);

        var approvedData = HolePunching.CONF.toString().getBytes(StandardCharsets.US_ASCII);
        confirmedMessage = MessageHelper.getMessage(NetworkMessages.CONN, approvedData);

        this.client = client;
        this.connectionMessage = connectionMessage;
        allowedFailuresCount = 10;
    }

    @Override
    public ArrayList<EndPoint> call() throws Exception {
        System.out.println("Холпанчер запущен");
        potentials = new ArrayList<>(connectionMessage.clients);
        requesters = new HashSet<>();
        confirmed = new HashSet<>();
        result = null;
        setSocketTimeout();
        isRun = true;
        connectionLoop();
        resetSocketTimeout();
        isRun = false;
        failureCount = 0;
        return result;
    }

    public void cancel() throws ConnectorException {
        isRun = false;
        resetSocketTimeout();
    }

    private void connectionLoop() throws ConnectorException {
        while (isRun){
            connectionFrame();
        }
    }

    private void connectionFrame() throws ConnectorException {
        try {
            Thread.sleep(1000);
            send();
            System.out.println("Жду сообщений...");
            var buffer = new byte[512];
            var packet = new DatagramPacket(buffer, buffer.length);
            client.receive(packet);
            failureCount = 0;
            processMessage(packet.getAddress(), packet.getPort(), packet.getData());

        } catch (IOException e) {
            if (e instanceof SocketTimeoutException) {
                System.out.println("Кажется, никто нам не пишет(((.");
                failureCount++;
            } else{
                throw new ConnectorException("Произошла ошибка при попытке принять пакет.", e);
            }
        } catch (InterruptedException e) {
            throw new ConnectorException(e);
        }

        if (failureCount >= allowedFailuresCount){
            System.out.println("Давно не получал сообщений. Видимо, соединиться невозможно...");
            var e = new TimeoutException();
            throw new ConnectorException("Не удается установить соединение с сервером", e);
        }
    }

    private DatagramPacket receivePacket() throws ConnectorException {
        try {
            var buffer = new byte[512];
            var packet = new DatagramPacket(buffer, buffer.length);
            client.receive(packet);
            return packet;
        } catch (IOException e) {
            throw new ConnectorException("Произошла ошибка при попытке принять пакет.", e);
        }
    }

    private void send() throws ConnectorException {
        System.out.println("Отсылаю пакеты всем потенциальным клиентам...");

        for (var endPoints : potentials){
            if (!endPoints.publicEndPoint.address.isLoopbackAddress()){
                sendMessage(endPoints.publicEndPoint, checkedMessage);
            }
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

            if (potentials.size() == 0 && requesters.size() == 0){
                System.out.println("Удалось соединиться со всеми клиентами!");
                result = new ArrayList<>(confirmed);
                cancel();
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

    private void removeFromPotentials(EndPoint endPoint){
        var endPoints = potentials.stream()
                .filter(p -> p.publicEndPoint.equals(endPoint) || p.privateEndPoint.equals(endPoint))
                .findFirst()
                .orElse(null);

        if (endPoint != null) {
            potentials.remove(endPoints);
        }
    }

    private void setSocketTimeout() throws ConnectorException {
        try {
            client.setSoTimeout(100);
        } catch (SocketException e) {
            throw new ConnectorException("Не удалось установить таймаут сокета.", e);
        }
    }

    private void resetSocketTimeout() throws ConnectorException {
        try {
            client.setSoTimeout(0);
        } catch (SocketException e) {
            throw new ConnectorException("Не удалось сбросить таймаут сокета.", e);
        }
    }
}
