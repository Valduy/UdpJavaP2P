package client.views.implementations;

import client.views.interfaces.MainView;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame implements MainView {
    private JComponent component;

    public MainFrame(){
        super();
        setSize(600, 450);
        setResizable(false);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    @Override
    public void setComponent(JComponent component) {
        remove(component);
        this.component = component;
        add(component);
    }
}
