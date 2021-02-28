package com.company.network;

public enum NetworkMessages {
    Hello("HLLO"),
    Bye("GBYE"),
    Initial("INTL"),
    Info("INFO");

    public static final int size = 4;
    public final String label;

    NetworkMessages(String label){
        this.label = label;
    }
}
