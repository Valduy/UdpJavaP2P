package client.models.implementations;

import client.models.interfaces.MenuModel;
import events.Event;
import events.EventArgs;
import events.EventHandler;

public class MatchSearchEngine implements MenuModel {
    private final EventHandler<EventArgs> found = new EventHandler<>();

    @Override
    public void addFound(Event<EventArgs> methodReference) {
        found.subscribe(methodReference);
    }

    @Override
    public void removeFound(Event<EventArgs> methodReference) {
        found.unSubscribe(methodReference);
    }

    @Override
    public void startSearch() {

    }

    @Override
    public void stopSearch() {

    }
}
