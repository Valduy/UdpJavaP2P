package client.presenters.implementations;

import client.ReceiveEventArgs;
import client.Receiver;
import client.messages.Inputs;
import client.messages.Position;
import client.messages.WorldState;
import client.shapes.Rectangle;
import client.views.interfaces.GameView;
import com.company.network.EndPoint;
import com.company.network.MessageHelper;
import com.company.network.NetworkMessages;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import events.Event;
import events.EventArgs;
import events.EventHandler;
import game.GameLoop;
import game.GameWorld;
import pong.Point;
import pong.gameobjects.Arbiter;
import pong.gameobjects.Field;
import pong.gameobjects.HostBall;
import pong.gameobjects.HostRacket;

import javax.swing.*;
import java.io.IOException;
import java.io.StringReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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

    private final Gson gson = new Gson();
    private final Collection<EndPoint> clients;
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

    private final ArrayList<Inputs> inputs = new ArrayList<>();

    private DatagramSocket socket;
    private HostPongTask pongTask;
    private Receiver receiver;
    private Future<?> receiverFuture;

    private final EventHandler<EventArgs> ended = new EventHandler<>();

    public void addEnded(Event<EventArgs> methodReference){
        ended.subscribe(methodReference);
    }

    public void removeEnded(Event<EventArgs> methodReference){
        ended.unSubscribe(methodReference);
    }

    public HostPongPresenter(GameView view, Collection<EndPoint> clients, double width, double height){
        this.clients = clients;
        this.view = view;
        this.width = width;
        this.height = height;
        initWorld();
    }

    public void start(DatagramSocket socket) throws IOException {
        this.socket = socket;
        resetWorld();
        receiver = new Receiver(socket.getLocalPort(), 512);
        receiver.addReceived(this::onReceived);
        var executor = Executors.newSingleThreadExecutor();
        receiverFuture = executor.submit(receiver);
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
        try {
            view.draw(getObjects());
            sendState();
            processInputs();
        } catch (IOException ioException) {
            // TODO:...
            ioException.printStackTrace();
        }
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

    private void sendState() throws IOException {
        var state = getWorldState();
        var data = gson.toJson(state);
        var message = MessageHelper.getMessage(NetworkMessages.INFO, data);

        for (var client : clients){
            var packet = new DatagramPacket(message, message.length, client.address, client.port);
            socket.send(packet);
        }
    }

    private WorldState getWorldState(){
        var result = new WorldState();
        result.positions = new ArrayList<>();

        var leftPosition = new Position();
        leftPosition.x = leftRacket.getPosition().getPosition().getX();
        leftPosition.y = leftRacket.getPosition().getPosition().getY();
        result.positions.add(leftPosition);

        var rightPosition = new Position();
        leftPosition.x = rightRacket.getPosition().getPosition().getX();
        leftPosition.y = rightRacket.getPosition().getPosition().getY();
        result.positions.add(rightPosition);

        var ballPosition = new Position();
        ballPosition.x = ball.getPosition().getPosition().getX();
        ballPosition.y = ball.getPosition().getPosition().getY();
        result.positions.add(ballPosition);

        return result;
    }

    private void processInputs(){
        // TODO: использую только последнее сообщение, что не здорово, но для примера сойдет.
        if (!inputs.isEmpty()){
            var input = inputs.get(inputs.size() - 1);
            var racketInputs = rightRacket.getInputs();
            racketInputs.setIsUp(input.isUp);
            racketInputs.setIsDown(input.isDown);
            inputs.clear();
        }
    }

    private void onReceived(Object sender, ReceiveEventArgs e){
        var data = MessageHelper.toString(e.getReceived());
        var reader = new JsonReader(new StringReader(data));
        Inputs message = gson.fromJson(reader, Inputs.class);

        synchronized (inputs){
            inputs.add(message);
        }
    }
}
