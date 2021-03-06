package connectors.matchmaker;

import events.EventArgs;

public class ConnectionArgs extends EventArgs {
    public final int port;

    public ConnectionArgs(int port){
        this.port = port;
    }
}
