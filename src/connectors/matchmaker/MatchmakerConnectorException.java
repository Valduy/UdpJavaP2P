package connectors.matchmaker;

public class MatchmakerConnectorException extends Exception{
    private String message;
    private Exception cause;

    public MatchmakerConnectorException(){

    }

    public MatchmakerConnectorException(String message){
        this.message = message;
    }

    public MatchmakerConnectorException(Exception cause){
        this.cause = cause;
    }

    public MatchmakerConnectorException(String message, Exception cause){
        this.message = message;
        this.cause = cause;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public Exception getCause() {
        return cause;
    }
}
