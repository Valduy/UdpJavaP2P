package client.presenters.implementations;

import client.ReceiveEventArgs;
import client.messages.Inputs;
import client.messages.Position;
import client.messages.WorldState;
import client.views.interfaces.GameView;
import com.company.network.EndPoint;
import com.company.network.MessageHelper;
import com.company.network.NetworkMessages;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import events.EventArgs;
import game.GameWorld;
import pong.Point;
import pong.gameobjects.Arbiter;
import pong.gameobjects.Field;
import pong.gameobjects.HostBall;
import pong.gameobjects.HostRacket;

import java.io.IOException;
import java.io.StringReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.Collection;

public class HostPongPresenter extends PongPresenterBase{
    private final Gson gson = new Gson();

    private Field field;
    private HostRacket leftRacket;
    private HostRacket rightRacket;
    private HostBall ball;
    private Arbiter arbiter;

    private final ArrayList<Inputs> clientInputs = new ArrayList<>();

    public HostPongPresenter(GameView view, double width, double height){
        super(view, width, height);
        System.out.println("Создаю презентер хоста...");
    }

    @Override
    public void start(DatagramSocket socket, Collection<EndPoint> clients) throws IOException {
        super.start(socket, clients);
        System.out.println("Запускаю презентер хоста...");
    }

    @Override
    protected void initWorld(GameWorld world){
        field = new Field();
        leftRacket = new HostRacket();
        rightRacket = new HostRacket();
        ball = new HostBall();
        arbiter = new Arbiter();

        world.addGameObject(field);
        world.addGameObject(leftRacket);
        world.addGameObject(rightRacket);
        world.addGameObject(ball);
        world.addGameObject(arbiter);
        addDrawable(leftRacket);
        addDrawable(rightRacket);
        addDrawable(ball);

        field.setWidth(width);
        field.setHeight(height);

        leftRacket.getAABB().setMin(new Point(0, 0));
        leftRacket.getAABB().setMax(new Point(racketWidth, racketHeight));

        rightRacket.getAABB().setMin(new Point(0, 0));
        rightRacket.getAABB().setMax(new Point(racketWidth, racketHeight));

        ball.getAABB().setMin(new Point(0, 0));
        ball.getAABB().setMax(new Point(ballSize, ballSize));
    }

    @Override
    protected void resetWorld(GameWorld world){
        leftRacket.getPosition().setPosition(new Point(indent, (height - racketHeight) / 2));
        rightRacket.getPosition().setPosition(new Point(width - racketWidth - indent, (height - racketHeight) / 2));
        ball.getPosition().setPosition(new Point((width - ballSize) / 2, (height - ballSize) / 2));
        arbiter.reset();
    }

    @Override
    protected void onUpdated(Object sender, EventArgs e){
        super.onUpdated(sender, e);
        try {
            sendState();
            processInputs();
        } catch (IOException ioException) {
            // TODO:...
            ioException.printStackTrace();
        }
    }

    private void sendState() throws IOException {
        var state = getWorldState();
        var data = gson.toJson(state);
        var message = MessageHelper.getMessage(NetworkMessages.INFO, data);

        for (var client : getClients()){
            var packet = new DatagramPacket(message, message.length, client.address, client.port);
            getSocket().send(packet);
        }
    }

    private WorldState getWorldState(){
        //System.out.println("Делаем снимок состояния мира...");
        var result = new WorldState();
        result.positions = new ArrayList<>();

        var leftPosition = new Position();
        leftPosition.x = leftRacket.getPosition().getPosition().getX();
        leftPosition.y = leftRacket.getPosition().getPosition().getY();
        result.positions.add(leftPosition);

        var rightPosition = new Position();
        rightPosition.x = rightRacket.getPosition().getPosition().getX();
        rightPosition.y = rightRacket.getPosition().getPosition().getY();
        result.positions.add(rightPosition);

        var ballPosition = new Position();
        ballPosition.x = ball.getPosition().getPosition().getX();
        ballPosition.y = ball.getPosition().getPosition().getY();
        result.positions.add(ballPosition);

        result.leftScore = arbiter.getLeftScore();
        result.rightScore = arbiter.getRightScore();
        return result;
    }

    private void processInputs(){
        //System.out.println("Обрабатываем инпуты...");
        processHostInputs();
        processClientInputs();
    }

    private void processHostInputs(){
        //System.out.println("Обрабатываем инпуты хоста...");
        synchronized (getHostInputs()){
            var racketInputs = leftRacket.getInputs();
            racketInputs.setIsUp(getHostInputs().isUp);
            racketInputs.setIsDown(getHostInputs().isDown);
        }
    }

    private void processClientInputs(){
        //System.out.println("Обрабатываем инпуты клиента...");
        // TODO: использую только последнее сообщение, что не здорово, но для примера сойдет.
        synchronized (clientInputs){
            if (!clientInputs.isEmpty()){
                var input = clientInputs.get(clientInputs.size() - 1);
                var racketInputs = rightRacket.getInputs();
                racketInputs.setIsUp(input.isUp);
                racketInputs.setIsDown(input.isDown);
                clientInputs.clear();
            }
        }
    }

    @Override
    protected void onReceived(Object sender, ReceiveEventArgs e){
        var data = MessageHelper.toString(e.getReceived());
        var reader = new JsonReader(new StringReader(data));
        Inputs inputs = gson.fromJson(reader, Inputs.class);

        synchronized (clientInputs){
            clientInputs.add(inputs);
        }
    }
}
