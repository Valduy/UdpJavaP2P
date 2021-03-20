package client.presenters.implementations;

import client.ReceiveEventArgs;
import client.messages.WorldState;
import client.services.interfaces.MessageBoxService;
import client.views.interfaces.GameView;
import com.company.network.EndPoint;
import com.company.network.MessageHelper;
import com.company.network.NetworkMessages;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import events.EventArgs;
import game.GameWorld;
import pong.Point;
import pong.gameobjects.BallBase;
import pong.gameobjects.HostBall;
import pong.gameobjects.HostRacket;
import pong.gameobjects.RacketBase;

import java.io.IOException;
import java.io.StringReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.Collection;

public class ClientPongPresenter extends PongPresenterBase{
    private final Gson gson = new Gson();

    private RacketBase leftRacket;
    private RacketBase rightRacket;
    private BallBase ball;

    private final ArrayList<WorldState> worldStates = new ArrayList<>();

    public ClientPongPresenter(MessageBoxService messageBoxService, GameView view, double width, double height){
        super(messageBoxService, view, width, height);
    }

    @Override
    public void start(DatagramSocket socket, Collection<EndPoint> clients) throws IOException {
        System.out.println("Запускаю презентер клиента...");
        super.start(socket, clients);
    }

    @Override
    protected void initWorld(GameWorld world){
        leftRacket = new HostRacket();
        rightRacket = new HostRacket();
        ball = new HostBall();

        world.addGameObject(leftRacket);
        world.addGameObject(rightRacket);
        world.addGameObject(ball);
        addDrawable(leftRacket);
        addDrawable(rightRacket);
        addDrawable(ball);

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
    }

    @Override
    protected void onUpdated(Object sender, EventArgs e){
        super.onUpdated(sender, e);
        try {
            sendInputs();
            changeState();
        } catch (IOException ioException) {
            // TODO:...
            ioException.printStackTrace();
        }
    }

    private void sendInputs() throws IOException {
        byte[] message;

        synchronized (getHostInputs()){
            var data = gson.toJson(getHostInputs());
            message = MessageHelper.getMessage(NetworkMessages.INFO, data);
        }

        try {
            for (var host : getClients()){
                var packet = new DatagramPacket(message, message.length, host.address, host.port);
                getSocket().send(packet);
            }
        } catch (IOException e){
            if (!getSocket().isClosed()){
                throw e;
            }
        }
    }

    private void changeState(){
        synchronized (worldStates){
            if (!worldStates.isEmpty()){
                var state = worldStates.get(worldStates.size() - 1);

                var leftPosition = state.positions.get(0);
                leftRacket.getPosition().setPosition(new Point(leftPosition.x, leftPosition.y));

                var rightPosition = state.positions.get(1);
                rightRacket.getPosition().setPosition(new Point(rightPosition.x, rightPosition.y));

                var ballPosition = state.positions.get(2);
                ball.getPosition().setPosition(new Point(ballPosition.x, ballPosition.y));

                setLeftScore(state.leftScore);
                setRightScore(state.rightScore);
                worldStates.clear();
            }
        }
    }

    @Override
    protected void onReceived(Object sender, ReceiveEventArgs e){
        super.onReceived(sender, e);
        var data = MessageHelper.toString(e.getReceived());
        var reader = new JsonReader(new StringReader(data));

        try{
            WorldState state = gson.fromJson(reader, WorldState.class);

            synchronized (worldStates){
                worldStates.add(state);
            }
        } catch (JsonSyntaxException ex){
            System.out.printf("Получен некорректный json: %s", data);
        }
    }
}
