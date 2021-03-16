package game;

import java.util.HashMap;

public abstract class GameObject {
    private final HashMap<Class<? extends Component>, Component> components = new HashMap<>();
    private GameWorld gameWorld;

    public GameWorld getGameWorld() {
        return gameWorld;
    }

    public void setGameWorld(GameWorld gameWorld) {
        this.gameWorld = gameWorld;
    }

    public void addComponent(Component component){
        this.addComponent(component.getClass(), component);
    }

    public void addComponent(Class<? extends Component> clazz, Component component){
        component.setContext(this);
        components.put(clazz, component);
    }

    public Component getComponent(Class<? extends Component> clazz){
        return components.get(clazz);
    }

    public void removeComponent(Class<Component> clazz){
        var component = components.remove(clazz);
        component.setContext(null);
    }

    public void start(){

    }

    public void Update(long dt){
        for (var component : components.values()){
            component.Update(dt);
        }
    }
}
