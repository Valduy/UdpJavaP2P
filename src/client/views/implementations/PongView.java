package client.views.implementations;

import client.KeyEventArgs;
import client.KeyState;
import client.shapes.Rectangle;
import client.views.interfaces.GameView;
import events.Event;
import events.EventHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Collection;

public class PongView extends JPanel implements GameView, KeyListener {
    private Collection<Rectangle> objects;

    private EventHandler<KeyEventArgs> up = new EventHandler<>();

    @Override
    public void addUp(Event<KeyEventArgs> methodReference) {
        up.subscribe(methodReference);
    }

    @Override
    public void removeUp(Event<KeyEventArgs> methodReference) {
        up.unSubscribe(methodReference);
    }

    private EventHandler<KeyEventArgs> down = new EventHandler<>();

    @Override
    public void addDown(Event<KeyEventArgs> methodReference) {
        down.subscribe(methodReference);
    }

    @Override
    public void removeDown(Event<KeyEventArgs> methodReference) {
        down.unSubscribe(methodReference);
    }

    private EventHandler<KeyEventArgs> canceled = new EventHandler<>();

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
        setBackground(Color.black);
    }

    @Override
    public void draw(Collection<Rectangle> objects) {
        this.objects = objects;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (objects != null){
            for (var o : objects){
                g.setColor(Color.white);
                g.drawRect((int)o.getX(), (int)o.getY(), (int)o.getWidth(), (int)o.getHeight());
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent keyEvent) {

    }

    @Override
    public void keyPressed(KeyEvent keyEvent) {
        switch (keyEvent.getKeyCode()) {
            case KeyEvent.VK_W:
                up.invoke(this, new KeyEventArgs(KeyState.Pressed));
                break;
            case KeyEvent.VK_S:
                down.invoke(this, new KeyEventArgs(KeyState.Pressed));
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent keyEvent) {
        switch (keyEvent.getKeyCode()) {
            case KeyEvent.VK_W:
                up.invoke(this, new KeyEventArgs(KeyState.Released));
                break;
            case KeyEvent.VK_S:
                down.invoke(this, new KeyEventArgs(KeyState.Released));
                break;
        }
    }
}
