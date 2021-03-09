package connectors;

import events.Event;
import events.EventArgs;
import events.EventHandler;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.*;

public abstract class ConnectorBase<TResult> implements Connector<TResult> {
    protected abstract class ConnectorStateBase<TConnector extends ConnectorBase>{
        private final TConnector context;

        public TConnector getContext(){
            return context;
        }

        public ConnectorStateBase(TConnector context){
            this.context = context;
        }

        public abstract void send() throws ConnectorException;
        public abstract void processMessage(byte[] received) throws ConnectorException;
    }

    private Exception exception;
    private DatagramSocket client;
    private ScheduledFuture<?> connectionFuture;
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

    protected void setState(ConnectorStateBase<?> state){
        this.state = state;
    }

    protected Exception getException(){
        return exception;
    }

    protected void setException(Exception exception){
        this.exception = exception;
    }

    protected ScheduledFuture<?> getConnectionFuture() {
        return connectionFuture;
    }

    private final EventHandler<EventArgs> connected = new EventHandler<>();

    @Override
    public void addConnected(Event<EventArgs> methodReference){
        connected.subscribe(methodReference);
    }

    @Override
    public void removeConnected(Event<EventArgs> methodReference){
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
        exception = null;
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
        connectionFuture = executor.scheduleAtFixedRate(() -> {
            try {
                connect();
            } catch (ConnectorException e) {
                connected.invoke(this, new EventArgs());
                setException(e);
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    @Override
    public void stop() throws ConnectorException {
        isRun = false;

        try {
            stopConnectionTask();
            client.setSoTimeout(0);
        } catch (SocketException e) {
            throw new ConnectorException("Не удалось сбросить таймаут сокета.", e);
        }
    }

    protected abstract ConnectorStateBase<?> initStartState();

    protected void sendMessage(byte[] message) throws ConnectorException {
        try {
            var packet = new DatagramPacket(message, message.length, serverAddress, serverPort);
            client.send(packet);
        } catch (IOException e) {
            throw new ConnectorException("Не удалось отправить пакет.", e);
        }
    }

    protected void finishConnection() throws ConnectorException {
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
                throw new ConnectorException("Произошла ошибка при попытке принять пакет", e);
            }
        }

        if (failureCount >= allowedFailuresCount){
            throw new ConnectorException("Не удается установить соединение с сервером");
        }
    }

    private void stopConnectionTask() throws ConnectorException {
        if (!connectionFuture.isDone() && !connectionFuture.isCancelled()){
            connectionFuture.cancel(true);
        }
    }
}
