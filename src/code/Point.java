package code;
import java.awt.geom.Point2D;
public class Point extends Point2D implements Cloneable{
    // Point Class identifies a position/location in our grid
    double x, y;
    // x and y attributes are for coordinates
    public Point(double x, double y){
        this.x = x;
        this.y = y;
    }
    // Getters and Setters for x and y
    @Override
    public double getX() {
        return this.x;
    }

    @Override
    public double getY() {
        return this.y;
    }

    @Override
    public void setLocation(double x, double y) {
        this.x = x;
        this.y = y;
    }

    // distanceL1 calculates The Manhattan distance between 2 points
    public int distanceL1(Point p){
        return (int)(Math.abs(this.x - p.x) + Math.abs(this.y - p.y));
    }

    // overriding clone() method so point class can be cloneable
    @Override
    public Object clone() {
        return super.clone();
    }
    // toString method so Point class can printed as "(x, y)"
    public String toString(){
        return "(" +  this.x + ", " + this.y + ")";
    }
}
