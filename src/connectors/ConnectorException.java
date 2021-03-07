package connectors;

public class ConnectorException extends Exception{
    private String message;
    private Exception cause;

    public ConnectorException(){

    }

    public ConnectorException(String message){
        this.message = message;
    }

    public ConnectorException(Exception cause){
        this.cause = cause;
    }

    public ConnectorException(String message, Exception cause){
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
