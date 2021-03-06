package connectors.matchmaker.states;

import com.company.network.MessageHelper;
import com.company.network.NetworkMessages;
import com.company.network.UserStatus;
import connectors.matchmaker.MatchmakerConnector;
import connectors.matchmaker.MatchmakerConnectorException;

public class WaitState extends MatchmakerConnectorState{
    private final byte[] message;

    public WaitState(MatchmakerConnector context) {
        super(context);
        message = MessageHelper.getMessage(NetworkMessages.INFO);
    }

    @Override
    public void send() throws MatchmakerConnectorException {
        context.sendMessage(message);
    }

    @Override
    public void processMessage(byte[] received) {
        if (MessageHelper.getMessageType(received) == NetworkMessages.INFO){
            var data = MessageHelper.toString(received).trim();
            var status = UserStatus.valueOf(data);

            switch (status){
                case WAIT:
                    break;
                case CONN:
                    context.setState(new InitialState(context));
                    break;
                case ABSN:
                    context.setState(new HelloState(context));
                    break;
            }
        }
    }

}
