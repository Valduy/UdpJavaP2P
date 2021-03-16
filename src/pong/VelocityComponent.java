package pong;

import game.Component;

public class VelocityComponent extends Component {
    private Point velocity;

    public Point getVelocity() {
        return velocity;
    }

    public void setVelocity(Point velocity) {
        this.velocity = velocity;
    }
}
