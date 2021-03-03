package client.models.interfaces;

import events.Event;
import events.EventArgs;

public interface MenuModel {
    void addFound(Event<EventArgs> methodReference);
    void removeFound(Event<EventArgs> methodReference);

    void startSearch();
    void stopSearch();
}
