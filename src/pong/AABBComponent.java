package pong;

import game.Component;

public class AABBComponent extends Component {
    private Point position = new Point();
    private Point size = new Point();

    public Point getPosition(){
        return position;
    }

    public void setPosition(Point position){
        if (position == null){
            throw new IllegalArgumentException();
        }

        this.position = position;
    }

    public Point getSize(){
        return size;
    }

    public void setSize(Point size){
        if (size == null){
            throw new IllegalArgumentException();
        }

        this.size = size;
    }

    public double getX() {
        return position.getX();
    }

    public void setX(double x) {
        position.setX(x);
    }

    public double getY() {
        return position.getY();
    }

    public void setY(double y) {
        position.setY(y);
    }

    public double getWidth() {
        return size.getX();
    }

    public void setWidth(double width) {
        size.setX(width);
    }

    public double getHeight() {
        return size.getY();
    }

    public void setHeight(double height) {
        size.setY(height);
    }

    public Point getMin(){
        return getPosition();
    }

    public double getMinX(){
        return getX();
    }

    public double getMinY(){
        return getY();
    }

    public Point getMax(){
        return new Point(getMaxX(), getMaxY());
    }

    public double getMaxX(){
        return getX() + getWidth();
    }

    public double getMaxY(){
        return getY() + getHeight();
    }

    public Point getCenter(){
        return getMin().add(getMax()).dev(2);
    }

    public boolean isIntersect(AABBComponent other){
        if (this.getMaxX() < other.getMinX() || this.getMinX() > other.getMaxX()) return false;
        if (this.getMaxY() < other.getMinY() || this.getMinY() > other.getMaxY()) return false;
        return true;
    }
}
