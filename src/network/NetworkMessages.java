package network;

public enum NetworkMessages {
    HELLO("HLLO"),
    BYE("GBYE"),
    INITIAL("INTL"),
    INFO("INFO");

    public static final int size = 4;
    public final String label;

    NetworkMessages(String label){
        this.label = label;
    }
}
