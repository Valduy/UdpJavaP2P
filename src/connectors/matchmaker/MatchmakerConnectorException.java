package connectors.matchmaker;

public class MatchmakerConnectorException extends Exception{
    public final Exception sourceException;
    public final String message;

    public MatchmakerConnectorException(Exception e){
        sourceException = e;
        message = e.getMessage();
    }

    public MatchmakerConnectorException(String message, Exception e){
        this.message = message;
        sourceException = e;
    }
}
