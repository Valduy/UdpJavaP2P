package connectors.matchmakers;

import com.company.network.MessageHelper;
import com.company.network.NetworkMessages;
import com.company.network.UserStatus;
import connectors.ConnectorBase;
import connectors.ConnectorException;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;

public class MatchmakerConnector extends ConnectorBase<Integer> {
    private class HelloState extends ConnectorStateBase<MatchmakerConnector>{
        private final byte[] message;

        public HelloState(MatchmakerConnector context) {
            super(context);
            message = MessageHelper.getMessage(NetworkMessages.HLLO);
        }

        @Override
        public void send() throws ConnectorException {
            getContext().sendMessage(message);
        }

        @Override
        public void processMessage(byte[] received) {
            if (MessageHelper.getMessageType(received) == NetworkMessages.HLLO){
                getContext().setState(new MatchmakerConnector.WaitState(getContext()));
            }
        }
    }

    private class WaitState extends ConnectorStateBase<MatchmakerConnector> {
        private final byte[] message;

        public WaitState(MatchmakerConnector context) {
            super(context);
            message = MessageHelper.getMessage(NetworkMessages.INFO);
        }

        @Override
        public void send() throws ConnectorException {
            getContext().sendMessage(message);
        }

        @Override
        public void processMessage(byte[] received) {
            if (MessageHelper.getMessageType(received) == NetworkMessages.INFO) {
                var data = MessageHelper.toString(received).trim();
                var status = UserStatus.valueOf(data);

                switch (status) {
                    case CONN:
                        getContext().setState(new InitialState(getContext()));
                        break;
                    case ABSN:
                        getContext().setState(new HelloState(getContext()));
                        break;
                }
            }
        }
    }

    public class InitialState extends ConnectorStateBase<MatchmakerConnector> {
        private final byte[] message;

        public InitialState(MatchmakerConnector context) {
            super(context);
            message = MessageHelper.getMessage(NetworkMessages.INIT);
        }

        @Override
        public void send() throws ConnectorException {
            getContext().sendMessage(message);
        }

        @Override
        public void processMessage(byte[] received) throws ConnectorException {
            if (MessageHelper.getMessageType(received) == NetworkMessages.INIT){
                var data = MessageHelper.toByteArray(received);
                var port = ByteBuffer.wrap(data).getInt();
                getContext().setMatchPort(port);
                getContext().finishConnection();
            }
        }
    }

    private Integer matchPort;

    @Override
    public Integer getResult() throws ConnectorException {
        if (matchPort == null){
            throw new ConnectorException("Не удалось получить порт матча.", getException());
        }

        return matchPort;
    }

    private void setMatchPort(int matchPort){
        this.matchPort = matchPort;
    }

    @Override
    public void start(DatagramSocket client, InetAddress address, int port) throws ConnectorException {
        matchPort = null;
        super.start(client, address, port);
    }

    @Override
    protected ConnectorStateBase<?> initStartState() {
        return new HelloState(this);
    }
}
