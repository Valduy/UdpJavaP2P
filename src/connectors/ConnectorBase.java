package connectors;

import events.Event;
import events.EventArgs;
import events.EventHandler;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.*;

public abstract class ConnectorBase<TResult> implements Connector<TResult> {
    protected abstract class ConnectorStateBase<Connector extends ConnectorBase>{
        private final Connector context;

        public Connector getContext(){
            return context;
        }

        public ConnectorStateBase(Connector context){
            this.context = context;
        }

        public abstract void send() throws ConnectorException;
        public abstract void processMessage(byte[] received) throws ConnectorException;

        protected void changeState(ConnectorStateBase<Connector> state){
            setState(state);
        }

        protected void send(byte[] message) throws ConnectorException {
            sendMessage(message);
        }

        protected void finish() throws ConnectorException {
            finishConnection();
        }
    }

    private DatagramSocket client;
    private Future<Void> connectionFuture;
    private ConnectorStateBase<?> state;
    private InetAddress serverAddress;
    private int serverPort;
    private int failureCount;
    private int allowedFailuresCount;
    private boolean isRun;

    public boolean getIsRun(){
        return isRun;
    }

    public DatagramSocket getClient(){
        return client;
    }

    public InetAddress getServerAddress(){
        return serverAddress;
    }

    public int getServerPort(){
        return serverPort;
    }

    public int getAllowedFailuresCount(){
        return allowedFailuresCount;
    }

    public void setAllowedFailuresCount(int allowedFailuresCount){
        this.allowedFailuresCount = allowedFailuresCount;
    }

    private void setState(ConnectorStateBase<?> state){
        this.state = state;
    }

    private final EventHandler<EventArgs> connected = new EventHandler<>();

    @Override
    public void addFound(Event<EventArgs> methodReference){
        connected.subscribe(methodReference);
    }

    @Override
    public void removeFound(Event<EventArgs> methodReference){
        connected.unSubscribe(methodReference);
    }

    public ConnectorBase(){
        allowedFailuresCount = 10;
    }

    @Override
    public void start(DatagramSocket client, InetAddress address, int port)
            throws ConnectorException
    {
        this.serverAddress = address;
        this.serverPort = port;
        this.client = client;
        state = initStartState();

        try {
            var socketTimeout = 100;
            client.setSoTimeout(socketTimeout);
        } catch (SocketException e) {
            throw new ConnectorException("Не удалось установить таймаут сокета.", e);
        }

        failureCount = 0;
        isRun = true;
        var executor = Executors.newSingleThreadScheduledExecutor();
        connectionFuture = executor.schedule(() -> {
            connect();
            return null;
        }, 1, TimeUnit.SECONDS);
    }

    @Override
    public void stop() throws ConnectorException {
        isRun = false;

        try {
            client.setSoTimeout(0);
        } catch (SocketException e) {
            throw new ConnectorException("Не удалось сбросить таймаут сокета.", e);
        } finally {
            stopConnectionTask();
        }
    }

    protected abstract ConnectorStateBase<?> initStartState();

    private void sendMessage(byte[] message) throws ConnectorException {
        try {
            var packet = new DatagramPacket(message, message.length, serverAddress, serverPort);
            client.send(packet);
        } catch (IOException e) {
            throw new ConnectorException("Не удалось отправить пакет.", e);
        }
    }

    private void finishConnection() throws ConnectorException {
        isRun = false;

        try {
            client.setSoTimeout(0);
        } catch (SocketException e) {
            throw new ConnectorException("Не удалось сбросить таймаут сокета.", e);
        }

        connected.invoke(this, new EventArgs());
        stopConnectionTask();
    }

    private void connect() throws ConnectorException {
        state.send();

        try {
            var buffer = new byte[512];
            var packet = new DatagramPacket(buffer, buffer.length);
            client.receive(packet);
            failureCount = 0;
            state.processMessage(packet.getData());
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

    private void stopConnectionTask() throws ConnectorException {
        try {
            connectionFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new ConnectorException(e);
        }
    }
}
