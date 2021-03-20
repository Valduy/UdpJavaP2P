package client.presenters.implementations;

import client.KeyEventArgs;
import client.KeyState;
import client.ReceiveEventArgs;
import client.Receiver;
import client.messages.Inputs;
import client.presenters.interfaces.PongPresenter;
import client.services.interfaces.MessageBoxService;
import client.shapes.Rectangle;
import client.views.interfaces.ChildView;
import client.views.interfaces.GameView;
import com.company.network.EndPoint;
import events.Event;
import events.EventArgs;
import events.EventHandler;
import game.GameLoop;
import game.GameObject;
import game.GameWorld;
import pong.components.AABBComponent;
import pong.components.PositionComponent;

import javax.swing.*;
import java.io.IOException;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public abstract class PongPresenterBase implements PongPresenter {
    private class PongTask extends SwingWorker<Void, Object> {
        private final GameLoop loop;

        private final EventHandler<EventArgs> updated = new EventHandler<>();

        public void addUpdated(Event<EventArgs> methodReference){
            updated.subscribe(methodReference);
        }

        public void removeUpdated(Event<EventArgs> methodReference){
            updated.unSubscribe(methodReference);
        }

        public PongTask(GameWorld world){
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
        protected void process(List<Object> chunks) {
            updated.invoke(this, new EventArgs());
        }

        private void onUpdated(Object sender, EventArgs e){
            publish(new Object());
        }
    }

    private final GameView view;
    private final GameWorld world = new GameWorld();
    private final Inputs hostInputs = new Inputs();
    private final ArrayList<GameObject> drawable = new ArrayList<>();
    private final MessageBoxService messageBoxService;

    private DatagramSocket socket;
    private Collection<EndPoint> clients;
    private PongTask pongTask;
    private Receiver receiver;
    private Future<?> receiverFuture;

    private int leftScore;
    private int rightScore;
    private int allowedFailuresCount;
    private int actualFailuresCount;
    private boolean isNotified;

    protected final double width;
    protected final double height;
    protected final double ballSize = 10;
    protected final double racketHeight = ballSize * 2;
    protected final double racketWidth = ballSize;
    protected final double indent = ballSize;

    protected GameView getGameView(){
        return view;
    }

    protected Collection<EndPoint> getClients(){
        return clients;
    }

    protected Inputs getHostInputs(){
        return hostInputs;
    }

    protected DatagramSocket getSocket(){
        return socket;
    }

    @Override
    public ChildView getView() {
        return view;
    }

    public int getLeftScore(){
        return leftScore;
    }

    public void setLeftScore(int leftScore){
        this.leftScore = leftScore;
    }

    public int getRightScore(){
        return rightScore;
    }

    public void setRightScore(int rightScore){
        this.rightScore = rightScore;
    }

    public int getAllowedFailuresCount(){
        return allowedFailuresCount;
    }

    public void setAllowedFailuresCount(int allowedFailuresCount){
        this.allowedFailuresCount = allowedFailuresCount;
    }

    private final EventHandler<EventArgs> ended = new EventHandler<>();

    public void addEnded(Event<EventArgs> methodReference){
        ended.subscribe(methodReference);
    }

    public void removeEnded(Event<EventArgs> methodReference){
        ended.unSubscribe(methodReference);
    }

    public PongPresenterBase(MessageBoxService messageBoxService, GameView view, double width, double height){
        this.messageBoxService = messageBoxService;
        this.view = view;
        this.width = width;
        this.height = height;
        initWorld(world);
        view.setFieldSize((int)width, (int)height);
        allowedFailuresCount = 10;
    }

    public void start(DatagramSocket socket, Collection<EndPoint> clients) throws IOException {
        this.socket = socket;
        this.clients = clients;
        isNotified = false;
        actualFailuresCount = 0;
        resetWorld(world);
        view.addUp(this::onUp);
        view.addDown(this::onDown);
        view.addCanceled(this::onCanceled);
        receiver = new Receiver(socket, 512);
        receiver.addReceived(this::onReceived);
        var executor = Executors.newSingleThreadExecutor();
        receiverFuture = executor.submit(receiver);
        pongTask = new PongTask(world);
        pongTask.addUpdated(this::onUpdated);
        pongTask.execute();
    }

    protected void addDrawable(GameObject go){
        drawable.add(go);
    }

    protected void removeDrawable(GameObject go){
        drawable.remove(go);
    }

    protected abstract void initWorld(GameWorld world);
    protected abstract void resetWorld(GameWorld world);

    protected void onUpdated(Object sender, EventArgs e){
        //System.out.printf("Обновляю состояние мира (%s)...\n", System.currentTimeMillis());
        draw();
        updateScore();
        checkFailures();
    }

    protected void onReceived(Object sender, ReceiveEventArgs e){
        actualFailuresCount = 0;
        isNotified = false;
    }

    private void draw(){
        var toDraw = new ArrayList<Rectangle>();

        for (var go : drawable){
            var position = ((PositionComponent) go.getComponent(PositionComponent.class)).getPosition();
            var aabb = ((AABBComponent) go.getComponent(AABBComponent.class));
            var rectangle = new Rectangle(position.getX(), position.getY(), aabb.getWidth(), aabb.getHeight());
            toDraw.add(rectangle);
        }

        view.draw(toDraw);
    }

    private void updateScore(){
        view.setScore(leftScore, rightScore);
    }

    private void checkFailures(){
        actualFailuresCount++;

        if (!isNotified && actualFailuresCount >= allowedFailuresCount){
            isNotified = true;
            messageBoxService.showMessageDialog("Кажется, оппонент отключился...");
        }
    }

    private void onUp(Object sender, KeyEventArgs e){
        synchronized (getHostInputs()){
            getHostInputs().isUp = e.getKeyState() == KeyState.Pressed;
        }
    }

    private void onDown(Object sender, KeyEventArgs e){
        synchronized (getHostInputs()){
            getHostInputs().isDown = e.getKeyState() == KeyState.Pressed;
        }
    }

    private void onCanceled(Object sender, EventArgs e){
        view.removeUp(this::onUp);
        view.removeDown(this::onDown);
        view.removeCanceled(this::onCanceled);

        try {
            pongTask.cancel();
            pongTask.get();
        } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
        } catch (ExecutionException executionException) {
            executionException.printStackTrace();
        }

        try {
            receiver.cancel();
            receiverFuture.get();
        } catch (IOException ioException) {
            if (!getSocket().isClosed()){
                ioException.printStackTrace();
            }
        } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
        } catch (ExecutionException executionException) {
            executionException.printStackTrace();
        }

        ended.invoke(this, new EventArgs());
    }
}
