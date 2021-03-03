package client.presenters.implementations;

import client.presenters.interfaces.FieldPresenter;
import client.presenters.interfaces.MenuPresenter;
import client.views.interfaces.MainView;

public class MainPresenter {
    private final MainView view;
    private final MenuPresenter menuPresenter;
    private final FieldPresenter fieldPresenter;

    public MainPresenter(
            MainView view,
            MenuPresenter menuPresenter,
            FieldPresenter fieldPresenter)
    {
        this.view = view;
        this.menuPresenter = menuPresenter;
        this.fieldPresenter = fieldPresenter;
        view.setComponent(menuPresenter.getView().toComponent());
    }
}
