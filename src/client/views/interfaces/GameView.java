package client.views.interfaces;

import client.KeyEventArgs;
import client.shapes.Rectangle;
import events.Event;
import events.EventArgs;

import java.util.Collection;

public interface GameView extends ChildView {
    void addUp(Event<KeyEventArgs> methodReference);
    void removeUp(Event<KeyEventArgs> methodReference);
    void addDown(Event<KeyEventArgs> methodReference);
    void removeDown(Event<KeyEventArgs> methodReference);
    void addCanceled(Event<EventArgs> methodReference);
    void removeCanceled(Event<EventArgs> methodReference);
    void setFieldSize(int width, int height);
    void setScore(int left, int right);
    void draw(Collection<Rectangle> objects);
}
