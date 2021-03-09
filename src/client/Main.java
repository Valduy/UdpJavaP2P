package client;

import client.services.implementations.SwingMessageBoxSerivice;
import client.views.implementations.MainFrame;
import client.views.interfaces.ChildView;

public class Main {
    public static void main(String[] args) {
        new MainFrame();
        var md = new SwingMessageBoxSerivice();
        md.showMessageDialog("gsaasgadsg");
    }
}
