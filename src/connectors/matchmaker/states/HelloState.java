package connectors.matchmaker.states;

import com.company.network.MessageHelper;
import com.company.network.NetworkMessages;
import connectors.matchmaker.MatchmakerConnector;
import connectors.matchmaker.MatchmakerConnectorException;

public class HelloState extends MatchmakerConnectorState{
    private final byte[] message;

    public HelloState(MatchmakerConnector context) {
        super(context);
        message = MessageHelper.getMessage(NetworkMessages.HLLO);
    }

    @Override
    public void send() throws MatchmakerConnectorException {
        context.sendMessage(message);
    }

    @Override
    public void processMessage(byte[] received) {
        if (MessageHelper.getMessageType(received) == NetworkMessages.HLLO){
            context.setState(new WaitState(context));
        }
    }

}
