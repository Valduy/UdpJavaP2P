package client.views.interfaces;

import client.views.Rectangle;
import events.Event;
import events.EventArgs;

import java.util.Collection;

public interface PongView {
    void addUp(Event<EventArgs> methodReference);
    void removeUp(Event<EventArgs> methodReference);
    void addDown(Event<EventArgs> methodReference);
    void removeDown(Event<EventArgs> methodReference);
    void addCanceled(Event<EventArgs> methodReference);
    void removeCanceled(Event<EventArgs> methodReference);
    void Draw(Collection<Rectangle> objects);
}
