package pong.host;

import game.GameObject;
import pong.AABBComponent;
import pong.PhysicsComponent;
import pong.PositionComponent;

public class Ball extends GameObject {
    private final PositionComponent position = new PositionComponent();
    private final AABBComponent aabb = new AABBComponent();
    private final PhysicsComponent physics = new PhysicsComponent();

    private final double speedMagnitude = 10;

    public PositionComponent getPosition(){
        return position;
    }

    public AABBComponent getAABB(){
        return aabb;
    }

    public PhysicsComponent getPhysics(){
        return physics;
    }

    public double getSpeedMagnitude(){
        return speedMagnitude;
    }

    @Override
    public void start() {
        super.start();
        addComponent(position);
        addComponent(aabb);
        addComponent(physics);
    }

    @Override
    public void update(long dt) {
        super.update(dt);
    }
}
