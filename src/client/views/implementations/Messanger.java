package client.views.implementations;

import client.views.interfaces.MessangerView;
import events.Event;
import events.EventArgs;
import events.EventHandler;

import javax.swing.*;
import java.awt.*;

public class Messanger extends JComponent implements MessangerView {
    private final EventHandler<EventArgs> messaged = new EventHandler<>();
    private final TextArea textArea = new TextArea();
    private final JPanel messangerPanel = new JPanel();
    private final TextField textField = new TextField();
    private final Button button = new Button();

    private String lastMessage;

    @Override
    public String getLastMessage() {
        return lastMessage;
    }

    @Override
    public void addMessaged(Event<EventArgs> methodReference) {
        messaged.subscribe(methodReference);
    }

    @Override
    public void removeMessaged(Event<EventArgs> methodReference) {
        messaged.unSubscribe(methodReference);
    }

    public Messanger(){
        setLayout(new BorderLayout());
        textArea.setVisible(true);
        add(textArea, BorderLayout.CENTER);

        var grid = new GridLayout(1, 2);
        messangerPanel.setLayout(grid);
        add(messangerPanel, BorderLayout.SOUTH);

        textField.setVisible(true);
        messangerPanel.add(textField);

        button.setVisible(true);
        messangerPanel.add(button);
        button.addActionListener((e) ->{
            lastMessage = textField.getText();
            textField.setText("");
            messaged.invoke(this, new EventArgs());
        });
    }

    @Override
    public void addMessage(String message) {
        textArea.append(message);
    }

    @Override
    public JComponent toComponent() {
        return this;
    }
}
