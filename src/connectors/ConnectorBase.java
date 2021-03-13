package connectors;

import events.Event;
import events.EventArgs;
import events.EventHandler;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.*;

public abstract class ConnectorBase<TResult> implements Callable<TResult> {
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

    private DatagramSocket client;
    private ConnectorStateBase<?> state;
    private InetAddress serverAddress;
    private int serverPort;
    private int failureCount;
    private int allowedFailuresCount;
    private boolean isRun;
    private TResult result;

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

    private final EventHandler<EventArgs> connected = new EventHandler<>();

    public ConnectorBase(){
        allowedFailuresCount = 10;
    }

    protected void setResult(TResult result){
        this.result = result;
    }

    public ConnectorBase(DatagramSocket client, InetAddress address, int port){
        this.client = client;
        this.serverAddress = address;
        this.serverPort = port;
    }

    @Override
    public TResult call() throws ConnectorException {
        state = initStartState();
        setSocketTimeout();
        connectionLoop();
        failureCount = 0;
        isRun = true;
        return result;
    }

    public void cancel() throws ConnectorException {
        isRun = false;
        resetSocketTimeout();
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
        cancel();
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

    private void connectionLoop() throws ConnectorException {
        while (isRun){
            connectionFrame();
        }
    }

    private void connectionFrame() throws ConnectorException {
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
}
