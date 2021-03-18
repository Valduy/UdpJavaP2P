package client;

import client.KeyState;
import events.EventArgs;

public class KeyEventArgs extends EventArgs {
    private final KeyState keyState;

    public KeyState getKeyState(){
        return keyState;
    }

    public KeyEventArgs(KeyState keyState){
        this.keyState = keyState;
    }
}
