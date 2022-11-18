package code;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

public class CoastGuard {

    Point position;
    HashMap<Point, Integer> ships;
    HashMap<Point, Integer> wrecks;
    HashSet<Point> stations;

    public static String solve(String grid, String strategy, boolean visualize){
        return "";
    }


    public void timeStamp(){
        Iterator<Map.Entry<Point, Integer>> iterator = wrecks.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Point, Integer> mapElement = iterator.next();
            mapElement.setValue(mapElement.getValue()-1);

        }
    }


    public static void main(String[] args) {
        Point p1 = new Point(1,1);
        Point p2 = new Point(1,9);

        HashMap<Point, Integer> map = new HashMap<>();
        map.put(p1, 1);
        System.out.println(map.containsKey(p2));

    }

}
