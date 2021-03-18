package pong.gameobjects;

import game.GameObject;
import pong.Point;

public class Arbiter extends GameObject {
    private enum Direction{
        Left,
        Right,
    }

    private final long timeToRestart = 2000;

    private Field field;
    private HostBall ball;
    private Direction direction;
    private boolean isRestarted;
    private long restartTime;
    private int leftScore;
    private int rightScore;

    public int getLeftScore(){
        return leftScore;
    }

    public int getRightScore(){
        return rightScore;
    }

    @Override
    public void start() {
        super.start();
        ball = (HostBall) getGameWorld().getGameObjects().stream()
                .filter(go -> go instanceof HostBall)
                .findFirst()
                .get();

        field = (Field) getGameWorld().getGameObjects().stream()
                .filter(go -> go instanceof Field)
                .findFirst()
                .get();

        isRestarted = true;
        restartTime = System.currentTimeMillis() + timeToRestart;
    }

    @Override
    public void update(long dt) {
        super.update(dt);
        checkGoals();
        restart();
    }

    public void reset(){
        leftScore = rightScore = 0;
    }

    private void checkGoals(){
        var ballAABB = ball.getAABB();
        var ballPosition = ball.getPosition();
        var currentPosition = ballPosition.getPosition();

        if (currentPosition.getX() >= field.getWidth()){
            leftScore++;
            direction = Direction.Right;
            startRestart();
        }
        else if (currentPosition.getX() <= -ballAABB.getWidth()){
            rightScore++;
            direction = Direction.Left;
            startRestart();
        }
    }

    private void restart(){
        if (!isRestarted){
            if (restartTime <= System.currentTimeMillis()){
                isRestarted = true;
                var physics = ball.getPhysics();

                switch (direction){
                    case Left:
                        physics.setVelocity(new Point(-ball.getSpeedMagnitude(), 0));
                        break;
                    case Right:
                        physics.setVelocity(new Point(ball.getSpeedMagnitude(), 0));
                        break;
                }
            }
        }
    }

    private void startRestart(){
        resetBall();
        isRestarted = false;
        restartTime = System.currentTimeMillis() + timeToRestart;
    }

    private void resetBall(){
        ball.getPhysics().setVelocity(new Point(0, 0));
        var ballAABB = ball.getAABB();
        ball.getPosition().setPosition(new Point(
                (field.getWidth() - ballAABB.getWidth()) / 2,
                (field.getHeight() - ballAABB.getHeight()) / 2));
    }
}
