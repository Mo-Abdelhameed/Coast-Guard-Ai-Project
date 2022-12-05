package code;
import java.awt.geom.Point2D;
public class Point extends Point2D implements Cloneable{

    double x, y;

    public Point(double x, double y){
        this.x = x;
        this.y = y;
    }

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

    public int distanceL1(Point p){
        return (int)(Math.abs(this.x - p.x) + Math.abs(this.y - p.y));
    }

    @Override
    public Object clone() {
        return super.clone();
    }

    public String toString(){
        return "(" +  this.x + ", " + this.y + ")";
    }
}
