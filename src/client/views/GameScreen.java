package client.views;

import client.shapes.Rectangle;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;

public class GameScreen extends JPanel {
    private Collection<Rectangle> objects;

    public GameScreen(){
        setBackground(Color.black);
    }

    public void draw(Collection<Rectangle> objects){
        this.objects = objects;
        revalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (objects != null){
            for (var o : objects){
                g.setColor(Color.white);
                g.fillRect((int)o.getX(), (int)o.getY(), (int)o.getWidth(), (int)o.getHeight());
            }
        }
    }
}
