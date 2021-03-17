package pong.host;

import game.GameObject;
import pong.Point;

public class Arbiter extends GameObject {
    private Field field;
    private Ball ball;
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
        ball = (Ball) getGameWorld().getGameObjects().stream()
                .filter(go -> go instanceof Ball)
                .findFirst()
                .get();

        field = (Field) getGameWorld().getGameObjects().stream()
                .filter(go -> go instanceof Field)
                .findFirst()
                .get();
    }

    @Override
    public void update(long dt) {
        super.update(dt);
        checkGoals();
    }

    private void checkGoals(){
        var ballAABB = ball.getAABB();
        var ballPosition = ball.getPosition();
        var currentPosition = ballPosition.getPosition();

        if (currentPosition.getX() >= field.getWidth()){
            leftScore++;
            resetBall();
            // TODO: засчитать гол левому и рестартовать
        }
        else if (currentPosition.getX() <= -ballAABB.getWidth()){
            rightScore++;
            resetBall();
            // TODO: засчитать гол правому и рестартовать
        }
    }

    private void restart(){

    }

    private void resetBall(){
        ball.getPhysics().setVelocity(new Point(0, 0));
        var ballAABB = ball.getAABB();
        ball.getPosition().setPosition(new Point(
                (field.getWidth() - ballAABB.getWidth()) / 2,
                (field.getHeight() - ballAABB.getHeight()) / 2));
    }
}
