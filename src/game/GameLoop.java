package game;

import events.Event;
import events.EventArgs;
import events.EventHandler;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class GameLoop implements Callable<Void> {
    private final GameWorld gameWorld;
    private final long dt = 1000 / 30;
    private ScheduledFuture<?> future;
    private boolean isRun;

    public GameWorld getGameWorld(){
        return gameWorld;
    }

    public boolean getIsRun(){
        return isRun;
    }

    private final EventHandler<EventArgs> started = new EventHandler<>();

    public void addStarted(Event<EventArgs> methodReference){
        started.subscribe(methodReference);
    }

    public void removeStarted(Event<EventArgs> methodReference){
        started.unSubscribe(methodReference);
    }

    private final EventHandler<EventArgs> updated = new EventHandler<>();

    public void addUpdated(Event<EventArgs> methodReference){
        updated.subscribe(methodReference);
    }

    public void removeUpdated(Event<EventArgs> methodReference){
        updated.unSubscribe(methodReference);
    }

    public GameLoop(GameWorld gameWorld){
        this.gameWorld = gameWorld;
    }

    @Override
    public Void call() throws Exception {
        gameWorld.start();
        started.invoke(this, new EventArgs());
        isRun = true;
        var executor = Executors.newSingleThreadScheduledExecutor();
        future = executor.scheduleWithFixedDelay(this::gameFrame, 0, dt, TimeUnit.MILLISECONDS);
        future.get();
        isRun = false;
        return null;
    }

    public void cancel(){
        future.cancel(false);
    }

    private void gameFrame(){
        gameWorld.update(dt);
        updated.invoke(this, new EventArgs());
    }
}
