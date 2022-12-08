package code;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Objects;

public class State implements Cloneable, Comparable{

    Point position;                                             // our current position
    Hashtable<Point, Integer> ships;                            // Hashtable of ships' locations and remaining people on them
    Hashtable<Point, Integer> wrecks;                           // Hashtable of wrecked ships and remaining time for their blackbox to be retrieved
    HashSet<Point> stations;                                    // Hashtable of stations locations
    ArrayList<String> availableActions = new ArrayList<>();     // Arraylist of the available actions in this current state
    State parent;                                               // Pointer to the parent state to keep a linkage
    String parentAction;                                        // String indicating the action from the parent State that got us to this current state
    int remainingCapacity;                                      // Remaining Capacity of the coast guard that allows him to rescue more people
    int savedPeople;                                            // Saved people so far , to this current state
    int deadPeople;                                             // Dead people so far , to this current state
    int survivingPeople;                                        // Remaining people on ships , to this current state
    int savedBoxes;                                             // Saved boxes so far , to this current state
    int destroyedBoxes;                                         // Destroyed boxes so far , to this current state
    int remainingBoxes;                                         // Remaining boxes to be retrieved , to this current state
    int depth;                                                  // Depth of the current state in our tree
    double heuristicDeaths;                                     // Variable to save the result of the heuristic function concerning people
    double heuristicBoxes;                                      // Variable to save the result of the heuristic function concerning boxes

    // Empty constructor
    public State(){}

    // State constructor for root state , expanded/children states are cloned
    public State(Point position, int survivingPeople, Hashtable<Point, Integer> ships, Hashtable<Point, Integer> wrecks, HashSet<Point> stations) {
        this.position = position;
        this.ships = ships;
        this.wrecks = wrecks;
        this.stations = stations;
        this.parent = null;             // set null as default(root) then overwritten for children states
        this.parentAction = "";
        this.remainingCapacity = CoastGuard.capacity; // set as full capacity initially(root)
        this.survivingPeople = survivingPeople;
        setAvailableActions();          // sets the available actions to the current state
    }

    // checks whether we are currently in a goal state or not by our definition of a goal state which is:
    // No remaining people to be saved & no remaining boxes to be retrieved and currently the coast guard is not carrying anyone
    public boolean isGoalState(){
        return survivingPeople == 0 && remainingBoxes == 0 && remainingCapacity == CoastGuard.capacity;
    }

    // Fills available actions arraylist with actions in form of a string
    // These are the actions which the coast guard perform to expand a state
    public void setAvailableActions(){
        availableActions = new ArrayList<>();
        if(canPickUp())
            availableActions.add("pickup");

        if(canRetrieve())
            availableActions.add("retrieve");

        if(canDrop())
            availableActions.add("drop");

        if(canMoveLeft())
            availableActions.add("left");

        if(canMoveDown())
            availableActions.add("down");

        if(canMoveRight())
            availableActions.add("right");

        if(canMoveUp())
            availableActions.add("up");
    }

    // retrieve() to retrieve a black box
    public void retrieve(){
        savedBoxes++;
        remainingBoxes--;
        wrecks.remove(position);
    }

    // drop() to drop saved people from the coast guard into a station
    public void drop(){
        savedPeople += CoastGuard.capacity - remainingCapacity;
        remainingCapacity = CoastGuard.capacity;
    }

    // pick up () to save as many people as the coast guard can by picking them up from the ship onto the coast guard
    public void pickUp(){
        int shipSurvivors = ships.get(position);
        int n = Math.min(shipSurvivors,remainingCapacity);
        ships.put(position,shipSurvivors-n);
        remainingCapacity -= n;
        survivingPeople -= n;
        if(shipSurvivors - n == 0){
            ships.remove(position);
            wrecks.put(position,0);
            remainingBoxes++;
        }
    }

    // move() to change coast guard position
    public void move (String direction){
        switch (direction){
            case "left":
                position.y--; break;
            case "right":
                position.y++; break;
            case "up":
                position.x--; break;
            case "down":
                position.x++; break;
        }
    }

    // simple checks for actions
    public boolean canMoveLeft(){
        return position.getY() > 0;
    }

    public boolean canMoveRight(){
        return position.getY() < CoastGuard.m - 1;
    }

    public boolean canMoveUp(){
        return position.getX() > 0;
    }

    public boolean canMoveDown(){
        return position.getX() < CoastGuard.n - 1;
    }

    public boolean canPickUp(){
        return  ships.containsKey(position) && remainingCapacity > 0;
    }

    public boolean canDrop(){
        return  stations.contains(position) && remainingCapacity < CoastGuard.capacity;
    }

    public boolean canRetrieve(){
        return wrecks.containsKey(position);
    }

    // to get the farthest ship from our current position
    public Point getFarthestItem(Point current, Hashtable<Point, Integer> table){
        Point farthest = new Point(current.x, current.y);
        int maxDist = Integer.MIN_VALUE;
        for(Map.Entry<Point, Integer> e : table.entrySet()){
            Point p = e.getKey();
            int distance = current.distanceL1(p);
            if (maxDist < distance){
                maxDist = distance;
                farthest = p;
            }
        }
        return  farthest;
    }

    // to get the cost of the remaining boxes ==> get the number of boxes which we wont be able to save
    public double boxCost(){
        int result = 0;
        for(Map.Entry<Point, Integer> e : wrecks.entrySet()){
            Point boxPosition = e.getKey();
            int boxLife = e.getValue();
            int distance = position.distanceL1(boxPosition);
            if(distance + boxLife > 19)
                result++;
        }
        return result;
    }

    // Computes the number of guaranteed deaths from the farthest ship.
    public void farthestShipHeuristic(boolean a_star){
        Hashtable<Point, Integer> ships = (Hashtable<Point, Integer>)this.ships.clone();
        Hashtable<Point, Integer> wrecks = (Hashtable<Point, Integer>)this.wrecks.clone();
        Point position = (Point) this.position.clone();
        this.heuristicDeaths = 0;
        this.heuristicBoxes = 0;
        if(a_star){
            this.heuristicDeaths = deadPeople;
            this.heuristicBoxes = destroyedBoxes;
        }

        if(!this.ships.isEmpty()){
            Point farthest = getFarthestItem(position, ships);
            this.heuristicDeaths += Math.min(ships.get(farthest), position.distanceL1(farthest));
            return;
        }

        if(!this.wrecks.isEmpty()){
            this.heuristicBoxes += boxCost();
        }
    }

    // Computes the number of guaranteed deaths from ships.
    public void allShipsHeuristic(boolean a_star){
        Hashtable<Point, Integer> ships = (Hashtable<Point, Integer>)this.ships.clone();
        Hashtable<Point, Integer> wrecks = (Hashtable<Point, Integer>)this.wrecks.clone();
        Point position = (Point) this.position.clone();
        this.heuristicDeaths = 0;
        this.heuristicBoxes = 0;
        if(a_star){
            this.heuristicDeaths = deadPeople;
            this.heuristicBoxes = destroyedBoxes;
        }

        if(!this.ships.isEmpty()){
            for(Map.Entry<Point, Integer> e : ships.entrySet()){
                int peopleOnShip = e.getValue();
                Point shipPosition = e.getKey();
                this.heuristicDeaths += Math.min(peopleOnShip, position.distanceL1(shipPosition));
            }
            return;
        }
        if(!this.wrecks.isEmpty()){
            this.heuristicBoxes += boxCost();
        }
    }

    // Picks the maximum of the above heuristics
    public void maxHeuristic(boolean a_star){
        this.farthestShipHeuristic(a_star);
        State s1 = new State();
        s1.heuristicDeaths = this.heuristicDeaths;
        s1.heuristicBoxes = this.heuristicBoxes;
        this.allShipsHeuristic(a_star);
        State s2 = new State();
        s2.heuristicDeaths = this.heuristicDeaths;
        s2.heuristicBoxes = this.heuristicBoxes;
        State max = s1.compareTo(s2) > 0 ? s1 : s2;
        this.heuristicDeaths = max.heuristicDeaths;
        this.heuristicBoxes = max.heuristicBoxes;
    }

    // over-ride method to be able to clone a state used for expanding/children
    @Override
    public Object clone() throws CloneNotSupportedException {
        State s = (State) super.clone();
        s.position = (Point) s.position.clone();
        s.ships = (Hashtable<Point,Integer>)s.ships.clone();
        s.wrecks = (Hashtable<Point,Integer>)s.wrecks.clone();
        s.stations = (HashSet<Point>)s.stations.clone();
        return s;
    }

    // over-ride method used when checking for redundant states
    @Override
    public boolean equals(Object o) {

        if(!(o instanceof State))
            return false;

        State s = (State) o;

        return  s.position.equals(this.position) && s.ships.equals(this.ships) && s.wrecks.equals(this.wrecks) &&
                s.stations.equals(this.stations) && s.availableActions.equals(this.availableActions) && s.remainingCapacity == this.remainingCapacity &&
                s.savedPeople == this.savedPeople && s.survivingPeople == this.survivingPeople && this.deadPeople == s.deadPeople &&
                s.savedBoxes == this.savedBoxes && this.destroyedBoxes == s.destroyedBoxes && s.remainingBoxes == this.remainingBoxes;
    }

    // over-ride method used so class state be hashable correctly
    @Override
    public int hashCode() {
        return Objects.hash(position, ships, wrecks, stations, availableActions, remainingCapacity, savedBoxes, savedPeople, survivingPeople, deadPeople, destroyedBoxes, remainingBoxes );
    }

    // over-ride method used when sorting/comparing between 2 states using the heuristic variables
    @Override
    public int compareTo(Object o) {
        State t = (State) o;

        if(this.heuristicDeaths == t.heuristicDeaths){
            if(this.heuristicBoxes - t.heuristicBoxes > 0) return 1;
            if(this.heuristicBoxes - t.heuristicBoxes < 0) return -1;
            return 0;
        }

        if(this.heuristicDeaths - t.heuristicDeaths > 0) return 1;
        return -1;
    }

    // All of the following function are used in visualizing the state.

    public String[][] constructGrid(){
        int rows = CoastGuard.n, columns = CoastGuard.m;
        String[][] grid = new String[rows][columns];
        for(int i = 0; i < rows; i++){
            for(int j = 0; j < columns; j++){
                Point current = new Point(i, j);
                String cell = "";
                if(position.equals(current))
                    cell += "(YOU{" + (CoastGuard.capacity - remainingCapacity) + "})";
                if(ships.containsKey(current))
                    cell += "(Ship{" + ships.get(current) + "})";
                if(wrecks.containsKey(current))
                    cell += "(Wreck{" + wrecks.get(current) + "})";
                if(stations.contains(current))
                    cell += "(Station)";

                grid[i][j] = cell;
            }
        }
        return grid;
    }

    public String toString(){

        String state = parentAction.equals("") ? "" : "Performed Action: " + parentAction + "\n\n";
        String[][] grid = constructGrid();
        int cellWidth = 24;

        for(int row = 0; row < grid.length; row++){
            state += fillLine(CoastGuard.m, cellWidth);
            for(int col = 0; col < grid[0].length; col++){
                state += "|";
                state +=  pad(grid[row][col], cellWidth);
            }
            state += "|\n";
        }
        state += fillLine(CoastGuard.m, cellWidth);
        return state;
    }

    public static String fillLine(int width, int cellWidth){
        String result = "|";
        for(int i = 0; i < width; i++) {
            for(int j = 0; j < cellWidth; j++){
                result += "-";
            }
            result += "|";
        }
        return result + "\n";
    }

    public static String pad(String s, int totalLength){
        int difference = totalLength - s.length();
        String result = "";
        result = padLeft(s, (difference/2) + s.length());
        return padRight(result, totalLength);
    }

    public static String padRight(String s, int n) {
        return String.format("%-" + n + "s", s);
    }

    public static String padLeft(String s, int n) {
        return String.format("%" + n + "s", s);
    }

}
