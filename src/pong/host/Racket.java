package pong.host;

import game.GameObject;
import pong.*;

public class Racket extends GameObject {
    private final PositionComponent position = new PositionComponent();
    private final AABBComponent aabb = new AABBComponent();
    private final PhysicsComponent physics = new PhysicsComponent();
    private final InputComponent inputs = new InputComponent();
    private final double velocityMagnitude = 10;

    private Ball ball;

    public double getVelocityMagnitude(){
        return velocityMagnitude;
    }

    public PositionComponent getPosition(){
        return position;
    }

    public AABBComponent getAABB(){
        return aabb;
    }

    public PhysicsComponent getPhysics(){
        return physics;
    }

    @Override
    public void start() {
        super.start();
        addComponent(position);
        addComponent(aabb);
        addComponent(physics);
        addComponent(inputs);


        ball = (Ball) getGameWorld().getGameObjects().stream()
                .filter(go -> go instanceof Ball)
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
        if (aabb.isIntersect(ball.getAABB())){
            var racketCenter = aabb.getCenter();
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
