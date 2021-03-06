package com.company.server.matches;

public class MatchException extends Exception{
    private String message;
    private Exception cause;

    public MatchException(){

    }

    public MatchException(String message){
        this.message = message;
    }

    public MatchException(Exception cause){
        this.cause = cause;
    }

    public MatchException(String message, Exception cause){
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
