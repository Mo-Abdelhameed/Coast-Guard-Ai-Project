package code;

import java.util.*;

public class CoastGuard {
    static int m, n; // m -> number of columns, rows -> number of columns.
    static int capacity;
    static HashSet<State> visited;

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
//        int m = 3, n = 4;
        int capacity = r.nextInt((100 - 30) + 1) + 30;
        int posX = r.nextInt((n - 1) + 1) + 0;
        int posY = r.nextInt((m - 1) + 1) + 0;
        int numStations = r.nextInt((m*n-2 - 1) + 1) + 1;
        int[] stations= new int[numStations*2];
        for (int i = 0; i < stations.length-1; i+=2) {
            boolean exit = false;
            int x, y;
            while(!exit){
                x = r.nextInt((n - 1) + 1) + 0;
                y = r.nextInt((m - 1) + 1) + 0;
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
                x = r.nextInt((n - 1) + 1) + 0;
                y = r.nextInt((m - 1) + 1) + 0;
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

        visited = new HashSet<>();

        switch (strategy){
            case "BF":
                return bfs(initial);
            case "DF":
                return dfs(initial);
            case "ID":
                return iterativeDfs(initial);
            case "GR1":
                return heuristicBased(initial, "GR1");
            case "GR2":
                return heuristicBased(initial, "GR2");
            case "AS1":
                return heuristicBased(initial, "AS1");
            case "AS2":
                return heuristicBased(initial, "AS2");
        }
        return "Not a valid strategy";
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
            visited.clear();
        }
    }

    public static String heuristicBased(State initial, String heuristic) throws CloneNotSupportedException {
        PriorityQueue <State> q = new PriorityQueue<>();
        boolean a_star = heuristic.equals("AS1") || heuristic.equals("AS2");
        if(heuristic.equals("GR1") || heuristic.equals("AS1"))
            initial.h1(a_star);
        else if(heuristic.equals("GR2") || heuristic.equals("AS2"))
            initial.h2(a_star);

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
            for(State child : children)
                if(heuristic.equals("GR1") || heuristic.equals("AS1")) {
                    child.h1(a_star);
                }
                else
                    child.h2(a_star);
            q.addAll(children);
        }

        return path +";" + deaths +";" + boxes + ";"+ n_nodes;
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
            if(!visited.contains(child)) {
                children.add(child);
                visited.add(child);
            }
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
                s.wrecks.put(e.getKey(), 0);
                s.ships.remove(e.getKey());
                s.remainingBoxes++;

            }
            else{
                e.setValue(e.getValue()-1);
            }
            s.survivingPeople--;
            s.deadPeople++;
        }
    }

    public static void damageBoxes(State s){

        Iterator<Map.Entry<Point, Integer>> wreckIterator = s.wrecks.entrySet().iterator();
        ArrayList<Map.Entry<Point, Integer>> entry = new ArrayList<>();

        while (wreckIterator.hasNext())
            entry.add(wreckIterator.next());


        for(int i = 0; i < entry.size(); i++){
            Map.Entry<Point, Integer> e = entry.get(i);
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
        ArrayList<String> arr = new ArrayList<>();
        String grid0 = "5,6;50;0,1;0,4,3,3;1,1,90;";
        String grid1 = "6,6;52;2,0;2,4,4,0,5,4;2,1,19,4,2,6,5,0,8;";
        String grid2 = "7,5;40;2,3;3,6;1,1,10,4,5,90;";
        String grid3 = "8,5;60;4,6;2,7;3,4,37,3,5,93,4,0,40;";
        String grid4 = "5,7;63;4,2;6,2,6,3;0,0,17,0,2,73,3,0,30;";
        String grid5 = "5,5;69;3,3;0,0,0,1,1,0;0,3,78,1,2,2,1,3,14,4,4,9;";
        String grid6 = "7,5;86;0,0;1,3,1,5,4,2;1,1,42,2,5,99,3,5,89;";
        String grid7= "6,7;82;1,4;2,3;1,1,58,3,0,58,4,2,72;";
        String grid8 = "6,6;74;1,1;0,3,1,0,2,0,2,4,4,0,4,2,5,0;0,0,78,3,3,5,4,3,40;";
        String grid9 = "7,5;100;3,4;2,6,3,5;0,0,4,0,1,8,1,4,77,1,5,1,1,6,55,3,2,94,4,3,46;";
        String grid10= "10,6;59;1,7;0,0,2,2,3,0,5,3;1,3,69,3,4,80,4,7,94,4,9,14,5,2,39;";

        arr.add(grid0);
        arr.add(grid1);
        arr.add(grid2);
        arr.add(grid3);
        arr.add(grid4);
        arr.add(grid5);
        arr.add(grid6);
        arr.add(grid7);
        arr.add(grid8);
        arr.add(grid9);
        arr.add(grid10);



//        String example = "3,4;97;1,2;0,1;3,2,65;";
//        String str = GenGrid();
//        System.out.println(str);
//
//        System.out.println("DFS: " + solve(str, "DF", true));
//        System.out.println("Greedy: " + solve(str, "GR1", true));
//        System.out.println("A-Star: " + solve(str, "AS1", true));
//        System.out.println("BFS: " + solve(str, "BF", true));
//        System.out.println("Deepning search: " + solve(str, "ID", true));
//        System.out.println("----------------------------");




        for(String str : arr) {
            System.out.println("DFS: " + solve(str, "DF", true));
            System.out.println("Greedy: " + solve(str, "GR1", true));
            System.out.println("A-Star: " + solve(str, "AS1", true));
            System.out.println("BFS: " + solve(str, "BF", true));
            System.out.println("Deepning search: " + solve(str, "ID", true));
            System.out.println("----------------------------");
        }
    }
}
