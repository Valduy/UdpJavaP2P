package connectors.match;

import com.company.network.EndPoint;
import com.company.network.LanIpHelper;
import com.company.network.MessageHelper;
import com.company.network.NetworkMessages;
import com.google.gson.Gson;
import connectors.ConnectorBase;
import connectors.ConnectorException;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class MatchConnector extends ConnectorBase {
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
            sendMessage(message);
        }

        @Override
        public void processMessage(byte[] received) throws ConnectorException {
            if (MessageHelper.getMessageType(received) == NetworkMessages.HLLO){
                setState(new WaitState(context));
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
            sendMessage(message);
        }

        @Override
        public void processMessage(byte[] received) throws ConnectorException {
            if (MessageHelper.getMessageType(received) == NetworkMessages.INIT){
                // TODO: прочитать P2P и сменить стейт
            }
        }
    }

    private InetAddress lanIp;

    protected InetAddress getLanIp(){
        return lanIp;
    }

    @Override
    public void start(DatagramSocket client, InetAddress address, int port) throws ConnectorException {
        try {
            lanIp = LanIpHelper.getLocalHostLANAddress();
        } catch (UnknownHostException e) {
            throw new ConnectorException("Не удалось узнать IP машины в LAN", e);
        }

        super.start(client, address, port);
    }

    @Override
    protected ConnectorStateBase<?> initStartState() {
        return new HelloState(this);
    }
}
