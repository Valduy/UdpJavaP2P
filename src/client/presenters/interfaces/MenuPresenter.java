package client.presenters.interfaces;

import events.Event;
import events.EventArgs;

public interface MenuPresenter extends ChildPresenter {
    void addFound(Event<EventArgs> methodReference);
    void removeFound(Event<EventArgs> methodReference);
}
