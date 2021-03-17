package pong;

import game.Component;

public class PositionComponent extends Component {
    private Point position;

    public Point getPosition(){
        return position;
    }

    public void setPosition(Point position){
        this.position = position;
    }
}
