package code;

import java.util.*;

public class CoastGuard {
    static int m, n; // m -> number of columns, rows -> number of columns.
    static int capacity;
    static State state;

    public static void parseGrid(String grid){
        String[] items = grid.split(";");

        m= Integer.parseInt(items[0].split(",")[0]);
        n= Integer.parseInt(items[0].split(",")[1]);

        capacity= Integer.parseInt(items[1]);

        Point pos = new Point(Integer.parseInt(items[2].split(",")[0]),
                Integer.parseInt(items[2].split(",")[1]));

        HashSet<Point> stations = new HashSet<>();
        String[] stationsL = items[3].split(",");
        for (int i = 0; i < stationsL.length-1; i+=2) {
            stations.add(new Point(Integer.parseInt(stationsL[i]), Integer.parseInt(stationsL[i+1])));
        }

        HashMap<Point, Integer> ships = new HashMap<>();
        String[] shipsL = items[4].split(",");
        int totalPassengers = 0;
        for (int i = 0; i < shipsL.length-2; i+=3) {
            ships.put(new Point(Integer.parseInt(shipsL[i]), Integer.parseInt(shipsL[i+1])),
                    Integer.parseInt(shipsL[i+2]));
            totalPassengers+=Integer.parseInt(shipsL[i+2]);
        }

        state = new State(pos,totalPassengers, ships, new HashMap<Point, Integer>(), stations);

        //Keep for Debugging
//        System.out.println(m+"x"+n);
//        System.out.println(capacity);
//        System.out.println(pos.x+", "+pos.y);
//        System.out.println("Stations");
//        Iterator i = stations.iterator();
//        while (i.hasNext()){
//            Point P = (Point)i.next();
//            System.out.println(P.x+", "+P.y);
//        }
//        System.out.println("Ships");
//        for (Map.Entry<Point, Integer> set : ships.entrySet()) {
//            Point P = (Point)set.getKey();
//            System.out.println(P.x+", "+P.y + " = " + set.getValue());
//        }
    }

    public static String solve(String grid, String strategy, boolean visualize) throws CloneNotSupportedException {

        Point position = new Point(1,2);
        capacity = 97;
        m = 3;
        n = 4;
        HashSet<Point> stations = new HashSet<>();
        stations.add(new Point(0,1));
        HashMap<Point, Integer> ships = new HashMap<>();
        ships.put(new Point(3,2),65);
        HashMap<Point, Integer> wrecks = new HashMap<>();
        State initial = new State(position,65,ships,wrecks,stations);

        switch (strategy){
            case "BF":
                return bfs(initial);
            case "DF":
                return dfs(initial);
            case "ID":
                return iterativeDfs(initial);
        }
        return "";
    }

    public static String bfs(State initial) throws CloneNotSupportedException {
        Queue <State> q = new LinkedList<>();
        q.add(initial);
        int n_nodes = 0, deaths = 0, boxes = 0;
        String path = "" ;
        while(!q.isEmpty()){
            State currState = q.remove();
            n_nodes++;
            if (currState.isGoalState()){
                deaths = currState.deadPeople;
                boxes = currState.savedBoxes;
                path = getSolution(currState);
                break;
            }
            ArrayList<State> children = expand(currState);
            q.addAll(children);
        }
        return path +";" + deaths +";" + boxes + ";"+ n_nodes;
    }

    public static String dfs(State initial) throws CloneNotSupportedException {
        int n_nodes = 0, deaths = 0, boxes = 0;
        String path = "" ;

        Stack <State> s = new Stack<>();
        s.add(initial);
        while(!s.isEmpty()){
            State currState = s.pop();
            n_nodes++;
            if (currState.isGoalState()){
                deaths = currState.deadPeople;
                boxes = currState.savedBoxes;
                path = getSolution(currState);
                break;
            }
            ArrayList<State> children = expand(currState);
            s.addAll(children);
        }
        return path +";" + deaths +";" + boxes + ";"+ n_nodes;
    }

    public static String iterativeDfs(State initial) throws CloneNotSupportedException{
        int n_nodes = 0, deaths = 0, boxes = 0;
        String path = "" ;
        int limit = 0 ;
        while(true){
            Stack <State> s = new Stack<>();
            s.add(initial);
            while(!s.isEmpty()){
                State currState = s.pop();
                n_nodes++;
                if (currState.isGoalState()){
                    deaths = currState.deadPeople;
                    boxes = currState.savedBoxes;
                    path = getSolution(currState);
                    return path +";" + deaths +";" + boxes + ";"+ n_nodes;
                }
                if (currState.depth<limit) {
                    ArrayList<State> children = expand(currState);
                    s.addAll(children);
                }
            }
            limit++;
        }
    }

    public static String getSolution (State s){
        String res = "";
        State temp = s ;
        while (temp.parent != null){
            res = ","+temp.parentAction + res ;
            temp = temp.parent;
        }
        return res.substring(1) ;
    }

    public static ArrayList<State> expand(State parent) throws CloneNotSupportedException {
        ArrayList<State> children = new ArrayList<State> ();
        for(String action : parent.availableActions){
            State child = performAction(parent, action);
            child.depth++;
            children.add(child);
        }
        return children ;
    }

    public static State performAction(State parent, String action) throws CloneNotSupportedException {
        State child = (State) parent.clone();
        child.parent = parent;
        child.parentAction = action;
        switch (action){
            case "left":
            case "right":
            case "up":
            case "down": child.move(action); break;
            case "pickup": child.pickUp(); break;
            case "retrieve": child.retrieve(); break;
            case "drop": child.drop();
        }
        timeStep(child);
        child.setAvailableActions();
        return child;
    }

    public static void timeStep(State s) {
        damageBoxes(s);
        killPeople(s);
    }

    public static void killPeople(State s) {
        Iterator<Map.Entry<Point, Integer>> shipIterator = s.ships.entrySet().iterator();
        while (shipIterator.hasNext()) {
            Map.Entry<Point, Integer> e = shipIterator.next();

            if(e.getValue() == 1) {
                s.wrecks.put(e.getKey(), 1);
                s.ships.remove(e.getKey());
                //System.out.println(s.position.x +":"+s.position.y + getSolution(s));
                s.remainingBoxes++;

            }
            else{
                //System.out.println(s.position.x +":"+s.position.y + getSolution(s)+"SP"+e.getValue());
                e.setValue(e.getValue()-1);
            }
            s.survivingPeople--;
            s.deadPeople++;
        }
    }

    public static void damageBoxes(State s){
        Iterator<Map.Entry<Point, Integer>> wreckIterator = s.wrecks.entrySet().iterator();
        while (wreckIterator.hasNext()) {
            Map.Entry<Point, Integer> e = wreckIterator.next();

            if(e.getValue() == 19) {
                s.wrecks.remove(e.getKey());
                s.remainingBoxes--;
                s.destroyedBoxes++;
            }
            else
                e.setValue(e.getValue()+1);
        }
    }

    public static void main(String[] args) throws CloneNotSupportedException {
        parseGrid("5,7;63;4,2;6,2,6,3;0,0,17,0,2,73,3,0,30;");

    }
}
