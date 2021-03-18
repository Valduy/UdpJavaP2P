package client;

import events.EventArgs;

public class ReceiveEventArgs extends EventArgs {
    private final byte[] received;

    public byte[] getReceived() {
        return received;
    }

    public ReceiveEventArgs(byte[] packet){
        received = packet;
    }
}
