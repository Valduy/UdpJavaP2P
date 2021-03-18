package client.presenters.implementations;

import client.views.Rectangle;
import client.views.interfaces.GameView;
import events.Event;
import events.EventArgs;
import events.EventHandler;
import game.GameLoop;
import game.GameWorld;
import pong.Point;
import pong.host.Arbiter;
import pong.host.Field;
import pong.host.HostBall;
import pong.host.HostRacket;

import javax.swing.*;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class HostPongPresenter {
    private class HostPongTask extends SwingWorker<Void, Void>{
        private final GameLoop loop;
        private final EventHandler<EventArgs> updated = new EventHandler<>();

        public void addUpdated(Event<EventArgs> methodReference){
            updated.subscribe(methodReference);
        }

        public void removeUpdated(Event<EventArgs> methodReference){
            updated.unSubscribe(methodReference);
        }

        public HostPongTask(GameWorld world){
            loop = new GameLoop(world);
            loop.addUpdated(this::onUpdated);
        }

        public void cancel(){
            loop.cancel();
        }

        @Override
        protected Void doInBackground() throws Exception {
            loop.call();
            return null;
        }

        @Override
        protected void process(List<Void> chunks) {
            for (var chunk : chunks){
                updated.invoke(this, new EventArgs());
            }
        }

        private void onUpdated(Object sender, EventArgs e){
            publish();
        }
    }

    private final GameView view;

    private final double width;
    private final double height;
    private final double ballSize = 10;
    private final double racketHeight = ballSize * 2;
    private final double racketWidth = ballSize;
    private final double indent = ballSize;

    private final GameWorld world = new GameWorld();
    private final Field field = new Field();
    private final HostRacket leftRacket = new HostRacket();
    private final HostRacket rightRacket = new HostRacket();
    private final HostBall ball = new HostBall();
    private final Arbiter arbiter = new Arbiter();

    private DatagramSocket socket;
    private HostPongTask pongTask;

    private final EventHandler<EventArgs> ended = new EventHandler<>();

    public void addEnded(Event<EventArgs> methodReference){
        ended.subscribe(methodReference);
    }

    public void removeEnded(Event<EventArgs> methodReference){
        ended.unSubscribe(methodReference);
    }

    public HostPongPresenter(GameView view, double width, double height){
        this.view = view;
        this.width = width;
        this.height = height;
        initWorld();
    }

    public void start(DatagramSocket socket){
        // TODO: нарулить приемопередачу через одни порт
        this.socket = socket;
        resetWorld();
        pongTask = new HostPongTask(world);
        pongTask.addUpdated(this::onUpdated);
        pongTask.execute();
    }

    private void initWorld(){
        world.addGameObject(field);
        world.addGameObject(leftRacket);
        world.addGameObject(rightRacket);
        world.addGameObject(ball);
        world.addGameObject(arbiter);

        field.setWidth(width);
        field.setHeight(height);

        leftRacket.getAABB().setMin(new Point(0, 0));
        leftRacket.getAABB().setMax(new Point(racketWidth, racketHeight));

        rightRacket.getAABB().setMin(new Point(0, 0));
        rightRacket.getAABB().setMax(new Point(racketWidth, racketHeight));
    }

    private void resetWorld(){
        leftRacket.getPosition().setPosition(new Point(indent, (height - racketHeight) / 2));
        rightRacket.getPosition().setPosition(new Point(width - racketWidth - indent, (height - racketHeight) / 2));
        ball.getPosition().setPosition(new Point((width - ballSize) / 2, (height - ballSize) / 2));
        arbiter.reset();
    }

    private void onUpdated(Object sender, EventArgs e){
        view.draw(getObjects());
        // TODO: отправка
        // TODO: очередь приема
    }

    private Collection<Rectangle> getObjects(){
        var result = new ArrayList<Rectangle>();

        var leftPosition = leftRacket.getPosition().getPosition();
        var r1 = new Rectangle(leftPosition.getX(), leftPosition.getY(), racketWidth, racketHeight);
        result.add(r1);

        var rightPosition = rightRacket.getPosition().getPosition();
        var r2 = new Rectangle(rightPosition.getX(), rightPosition.getY(), racketWidth, racketHeight);
        result.add(r2);

        var ballPosition = ball.getPosition().getPosition();
        var r3 = new Rectangle(ballPosition.getX(), ballPosition.getY(), ballSize, ballSize);
        result.add(r3);

        return result;
    }
}
