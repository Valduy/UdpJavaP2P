package client.views.implementations;

import client.KeyEventArgs;
import client.KeyState;
import client.shapes.Rectangle;
import client.views.interfaces.GameView;
import events.Event;
import events.EventArgs;
import events.EventHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

public class PongView extends JPanel implements GameView {
    private final JPanel menu = new JPanel();
    private final JButton quitButton = new JButton();

    private final int fieldWidth;
    private final int fieldHeight;

    private Collection<Rectangle> objects;
    private int leftScore;
    private int rightScore;
    private boolean isMenuShown;

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

    private final EventHandler<EventArgs> canceled = new EventHandler<>();

    @Override
    public void addCanceled(Event<EventArgs> methodReference) {
        canceled.subscribe(methodReference);
    }

    @Override
    public void removeCanceled(Event<EventArgs> methodReference) {
        canceled.unSubscribe(methodReference);
    }

    @Override
    public void setFieldSize(int width, int height) {
        setSize(new Dimension(width, height));
    }

    public PongView(int fieldWidth, int fieldHeight){
        this.fieldWidth = fieldWidth;
        this.fieldHeight = fieldHeight;
        init();
        setUpKeyListeners();
    }

    @Override
    public void draw(Collection<Rectangle> objects, int leftScore, int rightScore) {
        this.objects = objects;
        this.leftScore = leftScore;
        this.rightScore = rightScore;
        revalidate();
        repaint();
    }

    @Override
    public JComponent toComponent() {
        return this;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        var g2d = setUpGraphics(g);
        g2d.setColor(Color.gray);
        drawScore(g2d);
        drawLine(g2d);
        g.setColor(Color.white);
        drawObjects(g);
    }

    private void init(){
        setBackground(Color.black);
        setLayout(new GridLayout(1, 1));
        setMinimumSize(new Dimension(fieldWidth, fieldHeight));

        quitButton.setText("Покинуть матч");
        quitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                isMenuShown = false;
                menu.setVisible(false);
                canceled.invoke(this, new EventArgs());
            }
        });

        menu.setBackground(Color.white);
        menu.setLayout(new GridLayout(1, 1));
        menu.setBorder(BorderFactory.createEmptyBorder(200, 200, 200, 200));
        menu.add(quitButton);

        add(menu);
        menu.setVisible(false);
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

        getInputMap().put(KeyStroke.getKeyStroke("pressed ESCAPE"), "escPressed");
        getActionMap().put("escPressed", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                menu.setVisible(!isMenuShown);
            }
        });

        getInputMap().put(KeyStroke.getKeyStroke("released ESCAPE"), "escReleased");
        getActionMap().put("escReleased", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                isMenuShown = !isMenuShown;
            }
        });
    }

    private Graphics2D setUpGraphics(Graphics g){
        var g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        return g2d;
    }

    private void drawScore(Graphics2D g2d){
        var font = new Font("RIBBON", Font.PLAIN, getHeight() / 3);
        var metrics = g2d.getFontMetrics(font);
        g2d.setFont(font);

        var left = leftScore < 100 ? Integer.toString(leftScore) : "?";
        var leftWidth = metrics.stringWidth(left);

        g2d.drawString(
                left,
                (getWidth() / 2 - leftWidth) / 2,
                (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent());

        var right = rightScore < 100 ? Integer.toString(rightScore) : "?";
        var rightWidth = metrics.stringWidth(right);

        g2d.drawString(
                right,
                (int)(1.5 * getWidth() - rightWidth) / 2,
                (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent());
    }

    private void drawLine(Graphics2D g2d){
        var dashed = new BasicStroke(
                9,
                BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND,
                9,
                new float[]{27},
                0);
        g2d.setStroke(dashed);
        g2d.drawLine(getWidth() / 2, 0, getWidth() / 2, getHeight());
    }

    private void drawObjects(Graphics g){
        if (objects != null){
            for (var o : objects){
                g.fillRoundRect((int)o.getX(), (int)o.getY(), (int)o.getWidth(), (int)o.getHeight(), 20, 20);
            }
        }
    }
}
