package connectors.matchmaker.states;

import connectors.matchmaker.MatchmakerConnector;
import connectors.matchmaker.MatchmakerConnectorException;

public abstract class MatchmakerConnectorState {
    public final MatchmakerConnector context;

    public MatchmakerConnectorState(MatchmakerConnector context){
        this.context = context;
    }

    public abstract void send() throws MatchmakerConnectorException;
    public abstract void processMessage(byte[] received) throws MatchmakerConnectorException;
}
