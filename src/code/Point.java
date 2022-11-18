package code;
import java.awt.geom.Point2D;
public class Point extends Point2D{

    private double x, y;

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

    public static void main(String[] args) {
        Point p = new Point(0,0);
        Point p1 = new Point(1,1);
        System.out.println(p.distance(p1));
    }
}
