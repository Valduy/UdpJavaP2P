package client.presenters.implementations;

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

    private final double width = 600;
    private final double height = 450;
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

    public HostPongPresenter(){
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
        // TODO: отображение
        // TODO: отправка
        // TODO: очередь приема
    }
}
