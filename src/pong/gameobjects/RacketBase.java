package pong.gameobjects;

import game.GameObject;
import pong.components.AABBComponent;
import pong.components.PositionComponent;

public class RacketBase extends GameObject {
    private final PositionComponent position = new PositionComponent();
    private final AABBComponent aabb = new AABBComponent();

    public PositionComponent getPosition(){
        return position;
    }

    public AABBComponent getAABB(){
        return aabb;
    }

    @Override
    public void start() {
        super.start();
        addComponent(position);
        addComponent(aabb);
    }

    @Override
    public void update(long dt) {
        super.update(dt);
    }
}
