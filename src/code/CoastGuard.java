package code;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.Stack;

import com.sun.management.OperatingSystemMXBean;

public class CoastGuard {
    static int m, n; // m -> number of columns, n -> number of columns.
    static int capacity; // initial capacity of the coast guard
    static HashSet<State> visited; // Hashset to save all visited states and to help prevent redundant states
    static String utilization;     // String to save the value of utilization
    static long startTime, endTime;  // variables to save start and end time of execution
    static Stack<State> visualizationStack; // Stack to keep a track of the states to visualize


    // parseGrid() to parse input String grid to construct the initial state (world) by using the following format:
    /*
    M;N;C;cgX,cgY;
    I1X,I1Y,I2X,I2Y, :::IiX,IiY;
    S1X,S1Y,S1Passengers,S2X,S2Y,S2Passengers, :::SjX,SjY,SjPassengers;
    where
         M and N represent the width and height of the grid respectively.
         C is the maximum number of passengers the coast guard boat can carry at time.
         cgX and cgY are the initial coordinates of the coast guard boat.
         IiX; IiY are the x and y coordinates of the ith station.
         SjX; SjY are the x and y coordinates of the jth ship.
         SjPassengers is the initial number of passengers on board jth ship.

    "3,4;97;1,2;0,1;3,2,65;"   is an example where
                                    3 is the number of columns , 4 is the number of rows
                                    97 is our initial capacity
                                    we starting at position (1, 2)
                                    there exist a station at location (0, 1)
                                    and a ship at location (3 ,2) with 65 people on it to be saved
     */

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



    // GenGrid() to create a random world/problem
    /*   where
                5 <= (m,n) <= 15
                The coast guard boat is at a random location
                Several ships are scattered at random locations, and each has a random initial number of passengers p,
                    where 0 < p <= 100
                Several stations are at random locations.
                There are no wrecks.
                No two items are in the same cell.
     */
    public static String GenGrid(){
        Random r = new Random();
        int m = r.nextInt((15 - 5) + 1) + 5;
        int n = r.nextInt((15 - 5) + 1) + 5;
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

    // solve() to solve a given problem/grid in its string format with the chosen strategy ,having a visualize the solution option
    public static String solve(String grid, String strategy, boolean visualize) throws CloneNotSupportedException {

        State initial = parseGrid(grid);
        visited = new HashSet<>();
        visualizationStack = new Stack<>();
        String result = "Not a valid strategy";
        switch (strategy){
            case "BF":
                result = bfs(initial); break;
            case "DF":
                result =  dfs(initial); break;
            case "ID":
                result =  iterativeDfs(initial); break;
            case "GR1":
                result =  heuristicBased(initial, "GR1"); break;
            case "GR2":
                result =  heuristicBased(initial, "GR2"); break;
            case "AS1":
                result =  heuristicBased(initial, "AS1"); break;
            case "AS2":
                result =  heuristicBased(initial, "AS2"); break;
        }
        if (visualize)
            visualizeAnswer();
        return result;
    }

    // bfs() breadth first search strategy starting with initializing an empty queue then
    // we add to it our initial state (root) then
    // ** we de-queue a state, check if it's a goal state **
    // if so we return the solution
    // else we expand its children and add them to the queue
    // back to ** step until a goal state is found
    public static String bfs(State initial) throws CloneNotSupportedException {
        Queue <State> q = new LinkedList<>();
        q.add(initial);
        int n_nodes = 0, deaths = 0, boxes = 0;
        String path = "" ;
        startTime = System.nanoTime();
        while(!q.isEmpty()){
            State currState = q.remove();
            n_nodes++;
            if (currState.isGoalState()){
                utilization = computeUtilization();
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

    // dfs() depth first search strategy starting with initializing an empty stack then
    // we add to it our initial state (root) then
    // ** we pop a state, check if it's a goal state **
    // if so we return the solution
    // else we expand its children and push them to the stack
    // back to ** step until a goal state is found
    public static String dfs(State initial) throws CloneNotSupportedException {
        int n_nodes = 0, deaths = 0, boxes = 0;
        String path = "" ;
        Stack <State> s = new Stack<>();
        s.add(initial);
        startTime = System.nanoTime();
        while(!s.isEmpty()){
            State currState = s.pop();
            n_nodes++;
            if (currState.isGoalState()){
                utilization = computeUtilization();
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


    // iterativeDfs() Iterative deepening search which uses dfs strategy with a limit of the depth
    public static String iterativeDfs(State initial) throws CloneNotSupportedException{
        int n_nodes = 0, deaths = 0, boxes = 0;
        String path = "" ;
        int limit = 0 ;
        startTime = System.nanoTime();
        while(true){
            Stack <State> s = new Stack<>();
            s.add(initial);
            while(!s.isEmpty()){
                State currState = s.pop();
                n_nodes++;
                if (currState.isGoalState()){
                    utilization = computeUtilization();
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

    // heuristicBased() to use greedy or A star search strategies with 2 different heuristic functions by
    // using a priority queue and sorting the states based on their heuristic variables
    public static String heuristicBased(State initial, String heuristic) throws CloneNotSupportedException {
        PriorityQueue <State> q = new PriorityQueue<>();
        boolean a_star = heuristic.equals("AS1") || heuristic.equals("AS2") || heuristic.equals("AS3");
        if(heuristic.equals("GR1") || heuristic.equals("AS1"))
            initial.farthestShipHeuristic(a_star);
        else if(heuristic.equals("GR2") || heuristic.equals("AS2"))
            initial.allShipsHeuristic(a_star);

        q.add(initial);
        int n_nodes = 0, deaths = 0, boxes = 0;
        String path = "" ;
        startTime = System.nanoTime();
        while(!q.isEmpty()){
            State currState = q.remove();
            n_nodes++;
            if (currState.isGoalState()){
                utilization = computeUtilization();
                deaths = currState.deadPeople;
                boxes = currState.savedBoxes;
                path = getSolution(currState);
                break;
            }
            ArrayList<State> children = expand(currState);
            for(State child : children)
                if(heuristic.equals("GR1") || heuristic.equals("AS1")) {
                    child.farthestShipHeuristic(a_star);
                }
                else if(heuristic.equals("GR2") || heuristic.equals("AS2"))
                    child.allShipsHeuristic(a_star);

            q.addAll(children);
        }

        return path +";" + deaths +";" + boxes + ";"+ n_nodes;
    }

    // getSolution() to get the solution from given state recursively using parent pointer in each state
    public static String getSolution(State s){
        String res = "";
        State temp = s ;
        visualizationStack.add(temp);
        while (temp.parent != null){
            res = ","+temp.parentAction + res ;
            temp = temp.parent;
            visualizationStack.add(temp);
        }
        return res.substring(1) ;
    }

    // expand() to get the children of a given state by cloning the parent and performing an action the cloned state in the performAction() method
    // and we only add new/unvisited states to prevent redundant states overhead
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

    // performAction() we perform the action by first, cloning the parent then doing the action
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

    // timeStep() to update all the variables needed after every action
    public static void timeStep(State s) {
        damageBoxes(s);
        killPeople(s);
    }

    // killPeople() to kill 1 person from each existing ship and if it becomes empty switch it to wreck with a blackbox of value 1
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
                s.remainingBoxes++;

            }
            else{
                e.setValue(e.getValue()-1);
            }
            s.survivingPeople--;
            s.deadPeople++;
        }
    }

    // damageBoxes() to damage all available blackboxes by 1 and destroy/remove them if they reach 20
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


    public static String computeUtilization(){
        endTime = System.nanoTime();
        OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        memoryMXBean.gc();
        String memoryUsage = (String.format("Used heap memory: %.2f MB", (double)memoryMXBean.getHeapMemoryUsage().getUsed() /(1024*1024)));
        long totalTimeInMilliSeconds = (endTime - startTime) / 1000000;
        String time = "Total CPU time: " + totalTimeInMilliSeconds + "ms";
        return memoryUsage + "\n" + time;
    }


    public static void visualizeAnswer(){
        while (!visualizationStack.isEmpty())
            System.out.println(visualizationStack.pop());
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
        int i = 0;
        for(String str : arr) {

            System.out.println("Grid " + i++);
            System.out.println();

            System.out.println("BFS: " + solve(str, "BF", false));
            System.out.println(utilization);
            System.out.println("----------------------------");

            System.out.println("DFS: " + solve(str, "DF", false));
            System.out.println(utilization);
            System.out.println("----------------------------");

            System.out.println("ID: " + solve(str, "ID", false));
            System.out.println(utilization);
            System.out.println("----------------------------");

            System.out.println("GR1: " + solve(str, "GR1", false));
            System.out.println(utilization);
            System.out.println("----------------------------");

            System.out.println("GR2: " + solve(str, "GR2", false));
            System.out.println(utilization);
            System.out.println("----------------------------");

            System.out.println("A-Star 1: " + solve(str, "AS1", false));
            System.out.println(utilization);
            System.out.println("----------------------------");

            System.out.println("A-Star 2: " + solve(str, "AS2", false));
            System.out.println(utilization);

            System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
        }

        System.out.println(solve(grid9, "AS2", true));
    }
}
