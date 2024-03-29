package connectors.matches;

import com.company.network.*;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import connectors.ConnectorBase;
import connectors.ConnectorException;

import java.io.StringReader;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class MatchConnector extends ConnectorBase<P2PConnectionMessage> {
    private class HelloState extends ConnectorStateBase<MatchConnector>{
        private final byte[] message;

        public HelloState(MatchConnector context) {
            super(context);
            var gson = new Gson();
            var client = getClient();
            InetAddress address = getLanIp();
            EndPoint privateEndPoint = new EndPoint(address, client.getLocalPort());
            var data = gson.toJson(privateEndPoint);
            message = MessageHelper.getMessage(NetworkMessages.HLLO, data);
        }

        @Override
        public void send() throws ConnectorException {
            getContext().sendMessage(message);
        }

        @Override
        public void processMessage(byte[] received) throws ConnectorException {
            if (MessageHelper.getMessageType(received) == NetworkMessages.HLLO){
                getContext().setState(new WaitState(getContext()));
            }
        }
    }

    private class WaitState extends ConnectorStateBase<MatchConnector>{
        private final byte[] message;

        public WaitState(MatchConnector context) {
            super(context);
            message = MessageHelper.getMessage(NetworkMessages.HLLO);
        }

        @Override
        public void send() throws ConnectorException {
            getContext().sendMessage(message);
        }

        @Override
        public void processMessage(byte[] received) throws ConnectorException {
            if (MessageHelper.getMessageType(received) == NetworkMessages.INIT){
                var gson = new Gson();
                var data = MessageHelper.toString(received);
                var reader = new JsonReader(new StringReader(data));
                P2PConnectionMessage message = gson.fromJson(reader, P2PConnectionMessage.class);
                setResult(message);
                getContext().finishConnection();
            }
        }
    }

    private final InetAddress lanIp;

    private InetAddress getLanIp(){
        return lanIp;
    }

    public MatchConnector(DatagramSocket client, InetAddress address, int port) throws ConnectorException {
        super(client, address, port);

        try {
            lanIp = LanIpHelper.getLocalHostLANAddress();
        } catch (UnknownHostException e) {
            throw new ConnectorException("Не удалось узнать IP машины в LAN", e);
        }
    }

    @Override
    public P2PConnectionMessage call() throws ConnectorException {
        setResult(null);
        return super.call();
    }

    @Override
    protected ConnectorStateBase<?> initStartState() {
        return new HelloState(this);
    }
}
