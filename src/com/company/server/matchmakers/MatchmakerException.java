package com.company.server.matchmakers;

public class MatchmakerException extends Exception {
    private String message;
    private Exception cause;

    public MatchmakerException(){

    }

    public MatchmakerException(String message){
        this.message = message;
    }

    public MatchmakerException(Exception cause){
        this.cause = cause;
    }

    public MatchmakerException(String message, Exception cause){
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
