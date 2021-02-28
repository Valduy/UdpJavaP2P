package com.company.network;

public enum UserStatus{
    WAIT("WAIT"),
    CONN("CONN"),
    ABSN("ABSN");

    private final String label;
    public static final int size = 4;

    UserStatus(String label){
        this.label = label;
    }

    @Override
    public String toString(){
        return label;
    }
}