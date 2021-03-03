package client.presenters.implementations;

import client.models.interfaces.MenuModel;
import client.presenters.interfaces.MenuPresenter;
import client.views.interfaces.ChildView;
import client.views.interfaces.MenuView;
import events.Event;
import events.EventArgs;

public class ConcreteMenuPresenter implements MenuPresenter {
    private final MenuView view;
    private final MenuModel model;

    @Override
    public void addFound(Event<EventArgs> methodReference) {
        model.addFound(methodReference);
    }

    @Override
    public void removeFound(Event<EventArgs> methodReference) {
        model.removeFound(methodReference);
    }

    public ConcreteMenuPresenter(MenuView view, MenuModel model){
        this.view = view;
        this.model = model;

        view.addSearchClicked(this::onSearchClicked);
        view.addStopClicked(this::onStopClicked);
    }

    @Override
    public ChildView getView() {
        return view;
    }

    private void onSearchClicked(Object sender, EventArgs e){
        model.startSearch();
    }

    private void onStopClicked(Object sender, EventArgs e){
        model.stopSearch();
    }
}
