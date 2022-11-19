package code;

import java.util.*;

public class CoastGuard {
    static int m, n; // m -> number of columns, rows -> number of columns.
    static int capacity;

    public static State parseGrid(String grid){
        String[] items = grid.split(";");

        m = Integer.parseInt(items[0].split(",")[0]);
        n = Integer.parseInt(items[0].split(",")[1]);

        capacity = Integer.parseInt(items[1]);

        Point pos = new Point(Integer.parseInt(items[2].split(",")[0]),
                Integer.parseInt(items[2].split(",")[1]));

        HashSet<Point> stations = new HashSet<>();
        String[] stationsL = items[3].split(",");
        for (int i = 0; i < stationsL.length-1; i+=2) {
            stations.add(new Point(Integer.parseInt(stationsL[i]), Integer.parseInt(stationsL[i+1])));
        }

        Hashtable<Point, Integer> ships = new Hashtable<>();
        String[] shipsL = items[4].split(",");
        int totalPassengers = 0;
        for (int i = 0; i < shipsL.length-2; i+=3) {
            ships.put(new Point(Integer.parseInt(shipsL[i]), Integer.parseInt(shipsL[i+1])),
                    Integer.parseInt(shipsL[i+2]));
            totalPassengers+=Integer.parseInt(shipsL[i+2]);
        }

        return new State(pos,totalPassengers, ships, new Hashtable<Point, Integer>(), stations);

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

    public static String GenGrid(){
        Random r = new Random();
        int m = r.nextInt((15 - 5) + 1) + 5;
        int n = r.nextInt((15 - 5) + 1) + 5;
        int capacity = r.nextInt((100 - 30) + 1) + 30;
        int posX = r.nextInt((m - 1) + 1) + 0;
        int posY = r.nextInt((n - 1) + 1) + 0;
        int numStations = r.nextInt((m*n-2 - 1) + 1) + 1;
        int[] stations= new int[numStations*2];
        for (int i = 0; i < stations.length-1; i+=2) {
            boolean exit = false;
            int x, y;
            while(!exit){
                x = r.nextInt((m - 1) + 1) + 0;
                y = r.nextInt((n - 1) + 1) + 0;
                if(x==posX && y==posY){
                    continue;
                }
                if(i>0){
                    boolean foundMatch = false;
                    for (int j = 0; j < i; j+=2) {
                        if(x==stations[j] && y==stations[j+1]){
                            foundMatch=true;
                        }
                    }
                    if(foundMatch)
                        continue;
                }
                stations[i] = x;
                stations[i+1] = y;
                exit=true;
            }
        }
        int numShips = r.nextInt((m*n-1-numStations - 1) + 1) + 1;
        int[] ships= new int[numShips*3];
        for (int i = 0; i < ships.length-2; i+=3) {
            boolean exit = false;
            int x, y;
            while(!exit){
                x = r.nextInt((m - 1) + 1) + 0;
                y = r.nextInt((n - 1) + 1) + 0;
                if(x==posX && y==posY){
                    continue;
                }
                boolean foundStation = false;
                for (int j = 0; j < stations.length-1; j+=2) {
                    if(x==stations[j] && y==stations[j+1]){
                        foundStation=true;
                    }
                }
                if(foundStation)
                    continue;
                if(i>0){
                    boolean foundMatch = false;
                    for (int j = 0; j < i; j+=3) {
                        if(x==ships[j] && y==ships[j+1]){
                            foundMatch=true;
                        }
                    }
                    if(foundMatch)
                        continue;
                }
                ships[i] = x;
                ships[i+1] = y;
                ships[i+2] = r.nextInt((100 - 1) + 1) + 1;
                exit=true;
            }
        }
        String result = m+","+n+";"+capacity+";"+posX+","+posY+";";
        for (int i = 0; i < stations.length; i++) {
            if(i==stations.length-1)
                result+= stations[i]+";";
            else
                result+= stations[i]+",";
        }
        for (int i = 0; i < ships.length; i++) {
            if(i==ships.length-1)
                result+= ships[i]+";";
            else
                result+= ships[i]+",";
        }

        return result;
    }

    public static String solve(String grid, String strategy, boolean visualize) throws CloneNotSupportedException {

        State initial = parseGrid(grid);

//        Point position = new Point(1,2);
//        capacity = 97;
//        m = 3;
//        n = 4;
//        HashSet<Point> stations = new HashSet<>();
//        stations.add(new Point(0,1));
//        HashMap<Point, Integer> ships = new HashMap<>();
//        ships.put(new Point(3,2),65);
//        HashMap<Point, Integer> wrecks = new HashMap<>();
//        State initial = new State(position,65,ships,wrecks,stations);

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
                System.out.println(currState.wrecks.size());
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
        ArrayList<Map.Entry<Point, Integer>> entry = new ArrayList<>();
        while (shipIterator.hasNext()) {
            entry.add(shipIterator.next());
        }

        for(int i = 0; i < entry.size(); i++){
            Map.Entry<Point, Integer> e = entry.get(i);
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
        ArrayList<Map.Entry<Point, Integer>> entry = new ArrayList<>();
        while (wreckIterator.hasNext()) {
            entry.add(wreckIterator.next());
        }

        for(int i = 0; i < entry.size(); i++){
            Map.Entry<Point, Integer> e = entry.get(i);
            if(e.getValue() == 20) {
                s.wrecks.remove(e.getKey());
                s.remainingBoxes--;
                s.destroyedBoxes++;
            }
            else
                e.setValue(e.getValue()+1);
        }
    }

    public static void main(String[] args) throws CloneNotSupportedException {
        String example = "3,4;97;1,2;0,1;3,2,65;";
        String str = GenGrid();
//        System.out.println(str);
//        System.out.println(solve("5,6;50;0,1;0,4,3,3;1,1,90;", "DF", true));
//        System.out.println(solve("5,6;50;0,1;0,4,3,3;1,1,90;", "BF", true));
//        System.out.println(solve("5,6;50;0,1;0,4,3,3;1,1,90;", "ID", true));
        solve("5,6;50;0,1;0,4,3,3;1,1,90;", "DF", true);
    }
}
