package com.company.network;

public enum UserStatus{
    Wait("WAIT"),
    Connected("CONN"),
    Absent("ABSN");

    public static final int size = 4;
    public final String label;

    UserStatus(String label){
        this.label = label;
    }
}