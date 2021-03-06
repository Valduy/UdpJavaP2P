package connectors.matchmaker.states;

import com.company.network.MessageHelper;
import com.company.network.NetworkMessages;
import com.google.gson.Gson;
import connectors.matchmaker.MatchmakerConnector;
import connectors.matchmaker.MatchmakerConnectorException;

import java.nio.ByteBuffer;

public class InitialState extends MatchmakerConnectorState{
    private final byte[] message;
    private final Gson gson = new Gson();

    public InitialState(MatchmakerConnector context) {
        super(context);
        message = MessageHelper.getMessage(NetworkMessages.INIT);
    }

    @Override
    public void send() throws MatchmakerConnectorException {
        context.sendMessage(message);
    }

    @Override
    public void processMessage(byte[] received) throws MatchmakerConnectorException {
        if (MessageHelper.getMessageType(received) == NetworkMessages.INIT){
            var data = MessageHelper.toByteArray(received);
            var port = ByteBuffer.wrap(data).getInt();
            context.finishConnection(port);
        }
    }
}
