package Network;

public enum UserStatus{
    WAIT("WAIT"),
    CONNECTED("CONN"),
    ABSENT("ABSN");

    public static final int size = 4;
    public final String label;

    UserStatus(String label){
        this.label = label;
    }
}