package code;

import java.util.*;

public class CoastGuard {

//    static Point position;
//    static HashMap<Point, Integer> ships;
//    static HashMap<Point, Integer> wrecks;
//    static HashSet<Point> stations;
//    Collection<State> queue;
    static int m, n; // m -> number of rows, n -> number of columns.
    static int capacity;
//    static int savedPeople;
//    static int deadPeople;
//    static int savedBoxes;
//    static int destroyedBoxes;


    public static String solve(String grid, String strategy, boolean visualize){


        //State initial = new State(position, ships, wrecks, stations, null, capacity, savedPeople, deadPeople, savedBoxes, destroyedBoxes);
        return "";
    }


    public void timeStamp(State s) {

        damageBoxes(s);
        killPeople(s);

    }

    public void killPeople(State s) {
        Iterator<Map.Entry<Point, Integer>> shipIterator = s.ships.entrySet().iterator();
        while (shipIterator.hasNext()) {
            Map.Entry<Point, Integer> e = shipIterator.next();

            if(e.getValue() == 1) {
                s.wrecks.put(e.getKey(), 1);
                s.ships.remove(e.getKey());
            }
            else
                e.setValue(e.getValue()-1);
        }
    }

    public void damageBoxes(State s){
        Iterator<Map.Entry<Point, Integer>> wreckIterator = s.wrecks.entrySet().iterator();
        while (wreckIterator.hasNext()) {
            Map.Entry<Point, Integer> e = wreckIterator.next();

            if(e.getValue() == 19)
                s.wrecks.remove(e.getKey());
            else
                e.setValue(e.getValue()+1);
        }
    }

    public static void main(String[] args) {
        Point p1 = new Point(1,1);
        Point p2 = new Point(1,1);

        HashMap<Point, Integer> map = new HashMap<>();
        map.put(p1, 1);

        System.out.println(map.containsKey(p2));

    }

}
