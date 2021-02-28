package com.company.network;

public enum NetworkMessages {
    HLLO("HLLO"),
    GBYE("GBYE"),
    INIT("INIT"),
    INFO("INFO");

    private final String label;
    public static final int size = 4;

    NetworkMessages(String label){
        this.label = label;
    }

    @Override
    public String toString(){
        return label;
    }
}
