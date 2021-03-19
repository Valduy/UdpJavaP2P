package pong.components;

import game.Component;
import game.GameObject;
import pong.Point;

public class PhysicsComponent extends Component {
    private Point velocity = new Point();
    private PositionComponent position;

    @Override
    public void setContext(GameObject gameObject) {
        super.setContext(gameObject);
        position = (PositionComponent) getContext().getComponent(PositionComponent.class);
    }

    public Point getVelocity() {
        return velocity;
    }

    public void setVelocity(Point velocity) {
        this.velocity = velocity;
    }

    @Override
    public void update(long dt) {
        super.update(dt);
        var distance = velocity.mul(dt);
        var newPosition = position.getPosition().add(distance);
        position.setPosition(newPosition);
    }
}
