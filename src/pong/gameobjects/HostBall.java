package pong.gameobjects;

import pong.components.PhysicsComponent;

public class HostBall extends BallBase {
    private final PhysicsComponent physics = new PhysicsComponent();

    private final double speedMagnitude = 0.05;

    public PhysicsComponent getPhysics(){
        return physics;
    }

    public double getSpeedMagnitude(){
        return speedMagnitude;
    }

    @Override
    public void start() {
        super.start();
        addComponent(physics);
    }

    @Override
    public void update(long dt) {
        super.update(dt);
    }
}
