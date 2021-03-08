package com.company.server.matches;

import com.company.network.*;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import events.Event;
import events.EventArgs;
import events.EventHandler;

import java.io.IOException;
import java.io.StringReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Match {
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

    public class WaitClientState extends MatchStateBase {
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

    public class ChooseHostState extends MatchStateBase {
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

    public class ConnectClientsState extends MatchStateBase{
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
    private final EventHandler<EventArgs> ended = new EventHandler<>();
    private EndPoints host;

    private final int playersCount;
    private int port;

    private MatchStateBase state;
    private DatagramSocket socket;
    private Future<Void> matchFuture;

    private long endTime;
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

    protected void setState(MatchStateBase state){
        this.state = state;
    }

    public void addEnded(Event<EventArgs> methodReference){
        ended.subscribe(methodReference);
    }

    public void removeEnded(Event<EventArgs> methodReference){
        ended.unSubscribe(methodReference);
    }

    public Match(int playersCount){
        this.playersCount = playersCount;
    }

    public void start(long time) throws MatchException {
        start(0, time);
    }

    public void start(int port, long time) throws MatchException {
        try {
            socket = new DatagramSocket(port);
        } catch (SocketException e) {
            throw new MatchException("Не удалось создать сокет.", e);
        }

        this.port = socket.getLocalPort();
        state = new WaitClientState(this);
        isRun = true;
        var executor = Executors.newSingleThreadExecutor();
        endTime = System.currentTimeMillis() + time;
        matchFuture = executor.submit(() -> {
            matchLoop();
            return null;
        });
    }

    public void stop() throws MatchException {
        isRun = false;
        socket.close();

        try {
            matchFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new MatchException("Не удалось корректно завершить задачу.", e);
        }
    }

    protected void sendMessage(InetAddress address, int port, byte[] message) throws MatchException {
        try {
            var packet = new DatagramPacket(message, message.length, address, port);
            socket.send(packet);
        } catch (IOException e) {
            throw new MatchException("Не удалось отправить пакет.", e);
        }
    }

    protected void addClient(EndPoints endPoints){
        clients.add(endPoints);
    }

    private void matchLoop() throws MatchException {
        while (System.currentTimeMillis() < endTime && isRun){
            matchFrame();
        }

        isRun = false;
        socket.close();
        ended.invoke(this, new EventArgs());
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
