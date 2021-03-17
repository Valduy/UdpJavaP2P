package pong.components;

import game.Component;
import pong.Point;

public class AABBComponent extends Component {
    private Point min = new Point();
    private Point max = new Point();

    public Point getMin(){
        return min;
    }

    public void setMin(Point min){
        if (min == null){
            throw new IllegalArgumentException();
        }

        this.min = min;
    }

    public Point getMax() {
        return max;
    }

    public void setMax(Point max) {
        if (max == null){
            throw new IllegalArgumentException();
        }

        this.max = max;
    }

    public Point getSize(){
        return max.sub(min);
    }

    public double getWidth(){
        return max.getX() - min.getX();
    }

    public double getHeight(){
        return max.getY() - min.getY();
    }

    public Point getCenter(){
        return getMin().add(getMax()).dev(2);
    }

    public boolean isIntersect(AABBComponent other){
        var thisPosition = ((PositionComponent) getContext().getComponent(PositionComponent.class)).getPosition();
        var thisMin = thisPosition.add(min);
        var thisMax = thisPosition.add(max);

        var otherPosition = ((PositionComponent) other.getContext().getComponent(PositionComponent.class)).getPosition();
        var otherMin = otherPosition.add(other.getMin());
        var otherMax = otherPosition.add(other.getMax());

        if (thisMax.getX() < otherMin.getX() || thisMin.getX() > otherMax.getX()) return false;
        if (thisMax.getY() < otherMin.getY() || thisMin.getY() > otherMax.getY()) return false;
        return true;
    }
}
