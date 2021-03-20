package client.views.implementations;

import client.views.interfaces.MenuView;
import events.Event;
import events.EventArgs;
import events.EventHandler;

import javax.swing.*;
import java.awt.*;

public class Menu extends JComponent implements MenuView {
    private final JLabel searchLabel = new JLabel();
    private final JPanel menuPanel = new JPanel();
    private final JButton searchButton = new JButton();
    private final JButton stopButton = new JButton();

    private final EventHandler<EventArgs> searchClicked = new EventHandler<>();
    private final EventHandler<EventArgs> stopClicked = new EventHandler<>();

    @Override
    public void setIsInSearch(boolean isInSearch) {
        searchLabel.setVisible(isInSearch);
        stopButton.setEnabled(isInSearch);
        searchButton.setEnabled(!isInSearch);
    }

    @Override
    public void addSearchClicked(Event<EventArgs> methodReference) {
        searchClicked.subscribe(methodReference);
    }

    @Override
    public void removeSearchClicked(Event<EventArgs> methodReference) {
        searchClicked.unSubscribe(methodReference);
    }

    @Override
    public void addStopClicked(Event<EventArgs> methodReference) {
        stopClicked.subscribe(methodReference);
    }

    @Override
    public void removeStopClicked(Event<EventArgs> methodReference) {
        stopClicked.unSubscribe(methodReference);
    }

    public Menu(){
        setLayout(new GridLayout(2, 1));

        searchLabel.setText("Поиск игры...");
        searchLabel.setHorizontalAlignment(SwingConstants.CENTER);
        searchLabel.setVisible(false);
        add(searchLabel);

        menuPanel.setLayout(new GridLayout(3, 1));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(0, 100, 100, 100));
        add(menuPanel);

        searchButton.setText("Поиск");
        searchButton.addActionListener(e -> searchClicked.invoke(searchButton, new EventArgs()));
        menuPanel.add(searchButton);

        stopButton.setText("Стоп");
        stopButton.setEnabled(false);
        stopButton.addActionListener(e -> stopClicked.invoke(stopButton, new EventArgs()));
        menuPanel.add(stopButton);
    }

    @Override
    public JComponent toComponent() {
        return this;
    }
}
