package com.company.network;

public enum HolePunching {
    CHCK("CHCK"),
    CONF("CONF");

    private final String label;
    public static final int size = 4;

    HolePunching(String label){
        this.label = label;
    }

    @Override
    public String toString(){
        return label;
    }
}
