package client.views.interfaces;

import client.views.KeyEventArgs;
import client.views.Rectangle;
import events.Event;
import events.EventArgs;

import java.awt.*;
import java.util.Collection;

public interface GameView {
    void addUp(Event<KeyEventArgs> methodReference);
    void removeUp(Event<KeyEventArgs> methodReference);
    void addDown(Event<KeyEventArgs> methodReference);
    void removeDown(Event<KeyEventArgs> methodReference);
    void addCanceled(Event<KeyEventArgs> methodReference);
    void removeCanceled(Event<KeyEventArgs> methodReference);
    void setFieldSize(int width, int height);
    void draw(Collection<Rectangle> objects);
}
