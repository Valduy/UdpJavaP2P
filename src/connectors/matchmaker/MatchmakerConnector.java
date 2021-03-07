package connectors.matchmaker;

import com.company.network.MessageHelper;
import com.company.network.NetworkMessages;
import com.company.network.UserStatus;
import com.google.gson.Gson;
import connectors.ConnectorBase;
import connectors.ConnectorException;

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
            send(message);
        }

        @Override
        public void processMessage(byte[] received) {
            if (MessageHelper.getMessageType(received) == NetworkMessages.HLLO){
                changeState(new MatchmakerConnector.WaitState(getContext()));
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
            send(message);
        }

        @Override
        public void processMessage(byte[] received) {
            if (MessageHelper.getMessageType(received) == NetworkMessages.INFO) {
                var data = MessageHelper.toString(received).trim();
                var status = UserStatus.valueOf(data);

                switch (status) {
                    case CONN:
                        changeState(new InitialState(getContext()));
                        break;
                    case ABSN:
                        changeState(new HelloState(getContext()));
                        break;
                }
            }
        }
    }

    public class InitialState extends ConnectorStateBase<MatchmakerConnector> {
        private final byte[] message;
        private final Gson gson = new Gson();

        public InitialState(MatchmakerConnector context) {
            super(context);
            message = MessageHelper.getMessage(NetworkMessages.INIT);
        }

        @Override
        public void send() throws ConnectorException {
            send(message);
        }

        @Override
        public void processMessage(byte[] received) throws ConnectorException {
            if (MessageHelper.getMessageType(received) == NetworkMessages.INIT){
                var data = MessageHelper.toByteArray(received);
                var port = ByteBuffer.wrap(data).getInt();
                getContext().setMatchPort(port);
                finish();
            }
        }
    }

    private int matchPort;

    @Override
    public Integer getResult() {
        return matchPort;
    }

    private void setMatchPort(int matchPort){
        this.matchPort = matchPort;
    }

    @Override
    protected ConnectorStateBase<?> initStartState() {
        return new HelloState(this);
    }
}
