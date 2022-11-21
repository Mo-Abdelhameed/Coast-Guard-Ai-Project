package code;

import java.util.*;

public class State implements Cloneable, Comparable{

    Point position;
    Hashtable<Point, Integer> ships;
    Hashtable<Point, Integer> wrecks;
    HashSet<Point> stations;
    ArrayList<String> availableActions = new ArrayList<>();
    State parent;
    String parentAction;
    int remainingCapacity;
    int savedPeople;
    int deadPeople;
    int survivingPeople;
    int savedBoxes;
    int destroyedBoxes;
    int remainingBoxes;
    int depth;
    int heuristicDeaths;
    int heuristicBoxes;

    public State(){}

    public State(Point position, int survivingPeople, Hashtable<Point, Integer> ships, Hashtable<Point, Integer> wrecks, HashSet<Point> stations) {
        this.position = position;
        this.ships = ships;
        this.wrecks = wrecks;
        this.stations = stations;
        this.parent = null;
        this.parentAction = "";
        this.remainingCapacity = CoastGuard.capacity;
        this.survivingPeople = survivingPeople;
        setAvailableActions();
    }

    public boolean isGoalState(){
        return survivingPeople == 0 && remainingBoxes == 0 && remainingCapacity == CoastGuard.capacity;
    }

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

    public void retrieve(){
        savedBoxes++;
        remainingBoxes--;
        wrecks.remove(position);
    }

    public void drop(){
        savedPeople += CoastGuard.capacity - remainingCapacity;
        remainingCapacity = CoastGuard.capacity;
    }

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

    public Point getNearestItem(Point current, Hashtable<Point, Integer> table){
        Point nearest = new Point(current.x, current.y);
        int minLoss = Integer.MIN_VALUE;
        for(Map.Entry<Point, Integer> e : table.entrySet()){
            Point p = e.getKey();
            int distance =  e.getValue() - current.distanceL1(p);
            if (minLoss < distance){
                minLoss = distance;
                nearest = p;
            }
        }
        return  nearest;
    }

    public void h1(boolean a_star){
        Hashtable<Point, Integer> ships = (Hashtable<Point, Integer>)this.ships.clone();
        Hashtable<Point, Integer> wrecks = (Hashtable<Point, Integer>)this.wrecks.clone();
        Point position = (Point) this.position.clone();
        int result = 0;

        ArrayList<Integer> distances = new ArrayList<>();

        if(!ships.isEmpty()){
            while (!ships.isEmpty()) {
                Point nearest = getNearestItem(position, ships);
                distances.add(position.distanceL1(nearest));
                position = nearest;
                ships.remove(nearest);
            }
            result += distances.get(0);
            for (int j = 1; j < distances.size(); j++) {
                distances.set(j, distances.get(j-1) + distances.get(j));
                int t = distances.get(j);
                result += t + j;
            }
        }

        this.heuristicDeaths = result;

        result = 0;
        distances = new ArrayList<>();
        if(!wrecks.isEmpty()){
            while (!wrecks.isEmpty()) {
                Point nearest = getNearestItem(position, wrecks);
                distances.add(position.distanceL1(nearest));
                position = nearest;
                wrecks.remove(nearest);
            }
            result += distances.get(0);
            for (int j = 1; j < distances.size(); j++) {
                distances.set(j, distances.get(j-1) + distances.get(j));
                int t = distances.get(j);
                result += t + j;
            }
        }
        this.heuristicBoxes = result;

        if(a_star) {
            this.heuristicDeaths += deadPeople;
            this.heuristicBoxes += destroyedBoxes;
        }
    }

    public void h2(boolean a_star){

    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        State s = (State) super.clone();
        s.position = (Point) s.position.clone();
        s.ships = (Hashtable<Point, Integer> )s.ships.clone();
        s.wrecks = (Hashtable<Point, Integer> )s.wrecks.clone();
        s.stations = (HashSet<Point> )s.stations.clone();
        return s;
    }

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

    @Override
    public int hashCode() {
        return Objects.hash(position, ships, wrecks, stations, availableActions, remainingCapacity, savedBoxes, savedPeople, survivingPeople, deadPeople, destroyedBoxes, remainingBoxes );
    }

    @Override
    public int compareTo(Object o) {
        State t = (State) o;

        if(this.heuristicDeaths == t.heuristicDeaths)
            return this.heuristicBoxes - t.heuristicBoxes;

        return this.heuristicDeaths - t.heuristicDeaths;

    }

    public String toString(){
        return  "Position: " + this.position + "\n" + "Ships: "  + this.ships + "\n" + "Wrecks: " + this.wrecks;
    }
}
