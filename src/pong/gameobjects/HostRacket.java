package pong.gameobjects;

import pong.components.InputComponent;
import pong.components.PhysicsComponent;

public class HostRacket extends RacketBase {
    private final PhysicsComponent physics = new PhysicsComponent();
    private final InputComponent inputs = new InputComponent();
    private final double velocityMagnitude = 0.05;

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
            var racketCenter = getPosition().getPosition().add(getAABB().getCenter());
            var ballCenter = ball.getPosition().getPosition().add(ball.getAABB().getCenter());
            var direction = ballCenter.sub(racketCenter).normalize();
            var newVelocity = direction.mul(ball.getSpeedMagnitude());
            ball.getPhysics().setVelocity(newVelocity);
        }
    }

    private void processInputs(){
        if (inputs.getIsUp() && !inputs.getIsDown()){
            physics.getVelocity().setY(-velocityMagnitude);
        }
        else if (inputs.getIsDown() && !inputs.getIsUp()){
            physics.getVelocity().setY(velocityMagnitude);
        }
        else {
            physics.getVelocity().setY(0);
        }
    }
}
