package client.views.interfaces;

import events.*;

public interface MenuView extends ChildView {
    void setIsInSearch(boolean isInSearch);

    void addSearchClicked(Event<EventArgs> methodReference);
    void removeSearchClicked(Event<EventArgs> methodReference);

    void addStopClicked(Event<EventArgs> methodReference);
    void removeStopClicked(Event<EventArgs> methodReference);
}
