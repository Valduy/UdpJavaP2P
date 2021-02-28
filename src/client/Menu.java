package client;

import javax.swing.*;
import java.awt.*;

public class Menu {
    private JPanel mainPanel;
    private JButton searchButton;
    private JButton stopButton;
    private JLabel searchLabel;

    public Menu(){
        mainPanel.setPreferredSize(new Dimension(600, 450));
    }

    public static void main(String[] args) {
        var frame = new JFrame("menu");
        frame.setContentPane(new Menu().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
