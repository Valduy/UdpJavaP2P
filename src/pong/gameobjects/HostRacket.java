package pong.gameobjects;

import pong.Point;
import pong.components.InputComponent;
import pong.components.PhysicsComponent;

public class HostRacket extends RacketBase {
    private final PhysicsComponent physics = new PhysicsComponent();
    private final InputComponent inputs = new InputComponent();
    private final double velocityMagnitude = 10;

    private HostBall ball;

    public double getVelocityMagnitude(){
        return velocityMagnitude;
    }

    public PhysicsComponent getPhysics(){
        return physics;
    }

    public InputComponent getInputs() {
        return inputs;
    }

    @Override
    public void start() {
        super.start();
        addComponent(physics);
        addComponent(inputs);

        ball = (HostBall) getGameWorld().getGameObjects().stream()
                .filter(go -> go instanceof HostBall)
                .findFirst()
                .get();
    }

    @Override
    public void update(long dt) {
        super.update(dt);
        processCollisions();
        processInputs();
    }

    private void processCollisions(){
        if (getAABB().isIntersect(ball.getAABB())){
            var racketCenter = getAABB().getCenter();
            var ballCenter = ball.getAABB().getCenter();
            var direction = ballCenter.sub(racketCenter);
            var newVelocity = direction.mul(ball.getSpeedMagnitude());
            ball.getPhysics().setVelocity(newVelocity);
        }
    }

    private void processInputs(){
        if (inputs.getIsUp() && !inputs.getIsDown()){
            physics.setVelocity(new Point(0, -velocityMagnitude));
        }
        if (inputs.getIsDown() && inputs.getIsUp()){
            physics.setVelocity(new Point(0, velocityMagnitude));
        }
    }
}
