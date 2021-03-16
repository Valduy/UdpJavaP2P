package pong;

public class Point {
    private double x;
    private double y;

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public Point(){

    }

    public Point(double x, double y){
        this.x = x;
        this.y = y;
    }

    public double magnitude(){
        return Math.sqrt(x * x + y * y);
    }

    public Point normalize(){
        var magnitude = magnitude();
        return new Point(x / magnitude, y / magnitude);
    }

    public Point add(Point other){
        return new Point(this.x + other.x, this.y + other.y);
    }

    public Point sub(Point other){
        return new Point(this.x - other.x, this.y - other.y);
    }

    public Point mul(double num){
        return new Point(this.x * num, this.y * num);
    }

    public Point dev(double num){
        return new Point(this.x / num, this.y / num);
    }
}
