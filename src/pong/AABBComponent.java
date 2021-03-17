package pong;

import game.Component;

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
        if (this.getMax().getX() < other.getMin().getX() || this.getMin().getX() > other.getMax().getX()) return false;
        if (this.getMax().getY() < other.getMin().getY() || this.getMin().getY() > other.getMax().getY()) return false;
        return true;
    }
}
