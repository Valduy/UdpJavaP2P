package client.views.implementations;

import client.KeyEventArgs;
import client.KeyState;
import client.shapes.Rectangle;
import client.views.GameScreen;
import client.views.interfaces.GameView;
import events.Event;
import events.EventHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Collection;

public class PongView extends JPanel implements GameView {
    private final JPanel scorePanel = new JPanel();
    private final JLabel leftScore = new JLabel();
    private final JLabel rightScore = new JLabel();
    private final GameScreen screen = new GameScreen();

    private final EventHandler<KeyEventArgs> up = new EventHandler<>();

    @Override
    public void addUp(Event<KeyEventArgs> methodReference) {
        up.subscribe(methodReference);
    }

    @Override
    public void removeUp(Event<KeyEventArgs> methodReference) {
        up.unSubscribe(methodReference);
    }

    private final EventHandler<KeyEventArgs> down = new EventHandler<>();

    @Override
    public void addDown(Event<KeyEventArgs> methodReference) {
        down.subscribe(methodReference);
    }

    @Override
    public void removeDown(Event<KeyEventArgs> methodReference) {
        down.unSubscribe(methodReference);
    }

    private final EventHandler<KeyEventArgs> canceled = new EventHandler<>();

    @Override
    public void addCanceled(Event<KeyEventArgs> methodReference) {
        canceled.subscribe(methodReference);
    }

    @Override
    public void removeCanceled(Event<KeyEventArgs> methodReference) {
        canceled.unSubscribe(methodReference);
    }

    @Override
    public void setFieldSize(int width, int height) {
        setSize(new Dimension(width, height));
    }

    public PongView(){
        init();
        setUpKeyListeners();
    }

    @Override
    public void draw(Collection<Rectangle> objects) {
        screen.draw(objects);
    }

    @Override
    public JComponent toComponent() {
        return this;
    }

    private void init(){
        setLayout(new BorderLayout());

        var grid = new GridLayout(1, 2);
        scorePanel.setVisible(true);
        scorePanel.setLayout(grid);
        add(scorePanel, BorderLayout.NORTH);

        leftScore.setVisible(true);
        leftScore.setHorizontalAlignment(SwingConstants.CENTER);
        leftScore.setText("0");
        scorePanel.add(leftScore);

        rightScore.setVisible(true);
        rightScore.setHorizontalAlignment(SwingConstants.CENTER);
        rightScore.setText("0");
        scorePanel.add(rightScore);

        add(scorePanel, BorderLayout.NORTH);

        screen.setVisible(true);
        screen.setMinimumSize(new Dimension(600, 450)); // TODO: убрать хардкод
        add(screen, BorderLayout.CENTER);
    }

    private void setUpKeyListeners(){
        getInputMap().put(KeyStroke.getKeyStroke("pressed W"), "wPressed");
        getActionMap().put("wPressed", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                System.out.println("up pressed");
                up.invoke(this, new KeyEventArgs(KeyState.Pressed));
            }
        });

        getInputMap().put(KeyStroke.getKeyStroke("released W"), "wReleased");
        getActionMap().put("wReleased", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                System.out.print("up released");
                up.invoke(this, new KeyEventArgs(KeyState.Released));
            }
        });

        getInputMap().put(KeyStroke.getKeyStroke("pressed S"), "sPressed");
        getActionMap().put("sPressed", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                System.out.println("down pressed");
                down.invoke(this, new KeyEventArgs(KeyState.Pressed));
            }
        });

        getInputMap().put(KeyStroke.getKeyStroke("released S"), "sReleased");
        getActionMap().put("sReleased", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                System.out.print("down released");
                down.invoke(this, new KeyEventArgs(KeyState.Released));
            }
        });
    }
}
