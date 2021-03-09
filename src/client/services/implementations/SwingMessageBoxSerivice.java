package client.services.implementations;

import client.services.interfaces.MessageBoxService;

import javax.swing.*;

public class SwingMessageBoxSerivice implements MessageBoxService {
    public void showMessageDialog(String message){
        JOptionPane.showMessageDialog(null, message);
    }
}
