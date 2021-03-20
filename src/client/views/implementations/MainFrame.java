package client.views.implementations;

import client.views.interfaces.MainView;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame implements MainView {
    private JComponent component;

    public MainFrame(){
        super();
        setSize(610, 500);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    @Override
    public void setComponent(JComponent component) {
        if (this.component != null){
            remove(this.component);
        }

        this.component = component;
        add(component);
        component.setVisible(true);
        validate();
        repaint();
    }
}
