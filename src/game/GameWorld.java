package game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class GameWorld {
    private final ArrayList<GameObject> gameObjects = new ArrayList<>();

    public Collection<GameObject> getGameObjects(){
        return Collections.unmodifiableCollection(gameObjects);
    }

    public void addGameObject(GameObject gameObject){
        gameObject.setGameWorld(this);
        gameObjects.add(gameObject);
    }

    public void removeGameObject(GameObject gameObject){
        gameObject.setGameWorld(null);
        gameObjects.remove(gameObject);
    }

    public void start(){
        for (var go : gameObjects){
            go.start();
        }
    }
    
    public void update(long dt){
        for (var go : gameObjects){
            go.Update(dt);
        }
    }
}
