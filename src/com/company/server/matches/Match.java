package com.company.server.matches;

import com.company.network.*;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.StringReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Callable;

public class Match implements Callable<Void> {
    private abstract class MatchStateBase {
        private final Match context;

        protected Match getContext(){
            return context;
        }

        public MatchStateBase(Match context){
            this.context = context;
        }

        public abstract void processMessage(InetAddress address, int port, byte[] received) throws MatchException;

        protected boolean isClient(EndPoint endPoint){
            return context.getClients().stream().anyMatch(c -> c.publicEndPoint.equals(endPoint));
        }

        protected boolean isHost(EndPoint endPoint){
            return context.getHost() != null && context.getHost().publicEndPoint.equals(endPoint);
        }
    }

    private class WaitClientState extends MatchStateBase {
        private final Gson gson = new Gson();

        public WaitClientState(Match context) {
            super(context);
        }

        @Override
        public void processMessage(InetAddress address, int port, byte[] received) throws MatchException {
            if (MessageHelper.getMessageType(received) == NetworkMessages.HLLO){
                var context = getContext();
                var publicEndPoint = new EndPoint(address, port);

                if (!isClient(publicEndPoint)){
                    var data = MessageHelper.toString(received);
                    var reader = new JsonReader(new StringReader(data));
                    reader.setLenient(true);
                    EndPoint localEndPoint = gson.fromJson(reader, EndPoint.class);
                    var endPoints = new EndPoints(publicEndPoint, localEndPoint);
                    context.addClient(endPoints);

                    if (context.getIsFull()){
                        context.setState(new ChooseHostState(getContext()));
                    }
                }

                context.sendMessage(address, port, MessageHelper.getMessage(NetworkMessages.HLLO));
            }
        }
    }

    private class ChooseHostState extends MatchStateBase {
        public ChooseHostState(Match context) {
            super(context);
        }

        @Override
        public void processMessage(InetAddress address, int port, byte[] received) throws MatchException {
            if (MessageHelper.getMessageType(received) == NetworkMessages.HLLO){
                var publicEndPoint = new EndPoint(address, port);
                var client = getClient(publicEndPoint);

                if (client != null){
                    var context = getContext();
                    context.setHost(client);
                    context.setState(new ConnectClientsState(context));
                    context.sendMessage(address, port, MessageHelper.getMessage(NetworkMessages.HLLO));
                }
            }
        }

        private EndPoints getClient(EndPoint endPoint){
            return getContext().getClients()
                    .stream()
                    .filter(c -> c.publicEndPoint.equals(endPoint))
                    .findFirst()
                    .orElse(null);
        }
    }

    public class ConnectClientsState extends MatchStateBase {
        private final byte[] hostMessage;
        private final byte[] clientMessage;

        public ConnectClientsState(Match context) {
            super(context);
            var gson = new Gson();

            var messageForHost = new P2PConnectionMessage();
            messageForHost.role = Role.Host;
            messageForHost.clients = new ArrayList<>(getContext().getClients());
            hostMessage = MessageHelper.getMessage(NetworkMessages.INIT, gson.toJson(messageForHost));

            var messageForClient = new P2PConnectionMessage();
            messageForClient.role = Role.Client;
            messageForClient.clients = new ArrayList<>();
            messageForClient.clients.add(getContext().getHost());
            clientMessage = MessageHelper.getMessage(NetworkMessages.INIT, gson.toJson(messageForClient));
        }

        @Override
        public void processMessage(InetAddress address, int port, byte[] received) throws MatchException {
            if (MessageHelper.getMessageType(received) == NetworkMessages.HLLO){
                var publicEndPoint = new EndPoint(address, port);
                var context = getContext();

                if (isClient(publicEndPoint)){
                    context.sendMessage(address, port, clientMessage);
                }
                else if (isHost(publicEndPoint)){
                    context.sendMessage(address, port, hostMessage);
                }
            }
        }
    }

    private final ArrayList<EndPoints> clients = new ArrayList<>();
    private EndPoints host;

    private final int playersCount;
    private final long time;

    private MatchStateBase state;
    private DatagramSocket socket;

    private long endTime;
    private boolean isRun;

    public Collection<EndPoints> getClients(){
        return Collections.unmodifiableCollection(clients);
    }

    public EndPoints getHost(){
        return host;
    }

    protected void setHost(EndPoints endPoints){
        clients.remove(endPoints);
        host = endPoints;
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
        return socket.getLocalPort();
    }

    public int getPlayersCount(){
        return playersCount;
    }

    protected void setState(MatchStateBase state){
        this.state = state;
    }

    public Match(int playersCount, long time) throws MatchException {
        this(playersCount, time, 0);
    }

    public Match(int playersCount, long time, int port) throws MatchException {
        this.playersCount = playersCount;
        this.time = time;
        tryCreateSocket(port);
    }

    @Override
    public Void call() throws MatchException {
        startMatch();
        return null;
    }

    public void cancel(){
        isRun = false;
        socket.close();
    }

    protected void sendMessage(InetAddress address, int port, byte[] message) throws MatchException {
        try {
            var packet = new DatagramPacket(message, message.length, address, port);
            socket.send(packet);
        } catch (IOException e) {
            if (!socket.isClosed()) {
                throw new MatchException("Не удалось отправить пакет.", e);
            }
        }
    }

    private void startMatch() throws MatchException {
        state = new WaitClientState(this);
        isRun = true;
        endTime = System.currentTimeMillis() + time;
        matchLoop();
    }

    protected void addClient(EndPoints endPoints){
        clients.add(endPoints);
    }

    private void tryCreateSocket(int port) throws MatchException {
        try {
            socket = new DatagramSocket(port);
        } catch (SocketException e) {
            throw new MatchException("Не удалось создать сокет.", e);
        }
    }

    private void matchLoop() throws MatchException {
        while (System.currentTimeMillis() < endTime && isRun){
            matchFrame();
        }

        isRun = false;
        socket.close();
    }

    private void matchFrame() throws MatchException {
        var receive = new byte[128];
        var packet = new DatagramPacket(receive, receive.length);

        try {
            socket.receive(packet);
            state.processMessage(packet.getAddress(), packet.getPort(), packet.getData());
        } catch (IOException e) {
            if (!socket.isClosed()){
                throw new MatchException(e);
            }
        }
    }
}
