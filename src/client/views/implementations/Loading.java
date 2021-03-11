package client.views.implementations;

import client.views.interfaces.LoadView;

import javax.swing.*;
import java.awt.*;

public class Loading extends JComponent implements LoadView {
    private final JLabel label = new JLabel();

    public Loading(){
        setLayout(new GridLayout(1, 1));
        label.setVisible(true);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setText("Соединение...");
        add(label);
    }

    @Override
    public JComponent toComponent() {
        return this;
    }
}
