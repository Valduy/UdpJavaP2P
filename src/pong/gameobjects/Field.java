package pong.gameobjects;

import game.GameObject;

import java.util.List;
import java.util.stream.Collectors;

public class Field extends GameObject {
    private HostBall ball;
    private List<HostRacket> rackets;
    private double width;
    private double height;

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    @Override
    public void start() {
        super.start();
        ball = (HostBall) getGameWorld().getGameObjects().stream()
                .filter(go -> go instanceof HostBall)
                .findFirst()
                .get();

        rackets = getGameWorld().getGameObjects().stream()
                .filter(go -> go instanceof HostRacket)
                .map(go -> (HostRacket) go)
                .collect(Collectors.toList());
    }

    @Override
    public void update(long dt) {
        super.update(dt);
        processBallCollisions();
        processRacketsCollisions();
    }

    private void processBallCollisions(){
        var ballPosition = ball.getPosition();
        var ballPhysics = ball.getPhysics();
        var velocity = ball.getPhysics().getVelocity();
        var ballAABB = ball.getAABB();

        if (ballPosition.getPosition().getY() <= 0){
            velocity.setY(Math.max(velocity.getY(), -velocity.getY()));
            ballPhysics.setVelocity(velocity);
        }
        else if (ballPosition.getPosition().getY() >= height - ballAABB.getHeight()){
            velocity.setY(Math.min(velocity.getY(), -velocity.getY()));
            ballPhysics.setVelocity(velocity);
        }
    }

    private void processRacketsCollisions(){
        for (var racket : rackets){
            var racketAABB = racket.getAABB();
            var racketPosition = racket.getPosition();
            var currentPosition = racketPosition.getPosition();
            
            if (currentPosition.getY() <= 0){
                currentPosition.setY(0);
            }
            else if (currentPosition.getY() >= height - racketAABB.getHeight()){
                currentPosition.setY(height - racketAABB.getHeight());
            }
        }
    }
}
