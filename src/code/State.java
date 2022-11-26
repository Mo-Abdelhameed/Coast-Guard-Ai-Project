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
    double heuristicDeaths;
    double heuristicBoxes;

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

    public Point getNearestItem(Point current, Hashtable<Point, Integer> table, boolean h2){
        Point nearest = new Point(current.x, current.y);
        int minLoss = Integer.MIN_VALUE;
        for(Map.Entry<Point, Integer> e : table.entrySet()){
            Point p = e.getKey();
            int distance = h2 ? e.getValue() - current.distanceL1(p) : - current.distanceL1(p);
            if (minLoss < distance){
                minLoss = distance;
                nearest = p;
            }
        }
        return  nearest;
    }

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

    public int deathCost(){
        Hashtable<Point, Integer> ships = (Hashtable<Point, Integer>)this.ships.clone();
        Point position = (Point) this.position.clone();
        int result = 0;

        ArrayList<Integer> distances = new ArrayList<>();
        ArrayList<Integer> peopleInShips = new ArrayList<>();

        if(!ships.isEmpty()){
            while (!ships.isEmpty()) {
                Point nearest = getNearestItem(position, ships, false);
                distances.add(position.distanceL1(nearest));
                peopleInShips.add(ships.get(nearest));
                position = nearest;
                ships.remove(nearest);
            }
            result += distances.get(0);
            for (int j = 1; j < distances.size(); j++) {
                distances.set(j, distances.get(j-1) + distances.get(j));
                int t = Math.min(distances.get(j), peopleInShips.get(j));
                result += t + j;
            }
        }
        return result;
    }

    public double boxCost(){
        Hashtable<Point, Integer> wrecks = (Hashtable<Point, Integer>)this.wrecks.clone();
        Point position = (Point) this.position.clone();
        int result = 0;
        ArrayList<Integer> distances = new ArrayList<>();
        ArrayList<Integer> boxTime = new ArrayList<>();
        if(!wrecks.isEmpty()){
            while (!wrecks.isEmpty()) {
                Point nearest = getNearestItem(position, wrecks, false);
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
        return (result / 20);
    }

    public void h1k(boolean a_star){
        Hashtable<Point, Integer> ships = (Hashtable<Point, Integer>)this.ships.clone();
        Hashtable<Point, Integer> wrecks = (Hashtable<Point, Integer>)this.wrecks.clone();
        Point position = (Point) this.position.clone();
        ArrayList<Integer> distances = new ArrayList<>();
        ArrayList<Integer> peopleInShips = new ArrayList<>();

        if(a_star){
            this.heuristicDeaths = deadPeople;
            this.heuristicBoxes = destroyedBoxes;
        }


        if(!this.ships.isEmpty()){
            this.heuristicDeaths += deathCost();
            return;
        }

        if(!this.wrecks.isEmpty()){
            this.heuristicBoxes += boxCost();
            return;
        }
    }

    public void h3(boolean a_star){
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
            Point farthest = getFarthestItem(position, wrecks);
            int distance = position.distanceL1(farthest);
            int boxLife = wrecks.get(farthest);
            this.heuristicBoxes += distance + boxLife < 20 ? 0 : 1;
        }
    }

    public void h2(boolean a_star){
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
            Point nearest = getNearestItem(position, ships, true);
            this.heuristicDeaths += Math.min(ships.get(nearest), position.distanceL1(nearest));
            return;
        }

        if(!this.wrecks.isEmpty()){
            Point nearest = getNearestItem(position, wrecks, true);
            int distance = position.distanceL1(nearest);
            int boxLife = wrecks.get(nearest);
            this.heuristicBoxes += distance + boxLife < 20 ? 0 : 1;
        }
    }

    public void h1(boolean a_star){
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
            Point nearest = getNearestItem(position, ships, false);
            this.heuristicDeaths += Math.min(ships.get(nearest), position.distanceL1(nearest));
            return;
        }

        if(!this.wrecks.isEmpty()){
            Point nearest = getNearestItem(position, wrecks, false);
            int distance = position.distanceL1(nearest);
            int boxLife = wrecks.get(nearest);
            this.heuristicBoxes += distance + boxLife < 20 ? 0 : 1;
        }
    }

    public void h1_h2_h3(boolean a_star){

        this.h1(a_star);

        State s1 = new State();
        s1.heuristicDeaths = this.heuristicDeaths;
        s1.heuristicBoxes = this.heuristicBoxes;

        this.h2(a_star);
        State s2 = new State();
        s2.heuristicDeaths = this.heuristicDeaths;
        s2.heuristicBoxes = this.heuristicBoxes;

        this.h3(a_star);
        State s3 = new State();
        s3.heuristicDeaths = this.heuristicDeaths;
        s3.heuristicBoxes = this.heuristicBoxes;

        ArrayList<State> states = new ArrayList<>();
        states.add(s1);
        states.add(s2);
        states.add(s3);
        Collections.sort(states);
        State max = states.get(2);
        this.heuristicDeaths = max.heuristicDeaths;
        this.heuristicBoxes = max.heuristicBoxes;
    }

    public int distanceToNearestStation(){
        int min = Integer.MAX_VALUE;
        for(Point p : this.stations){
            if(min > this.position.distanceL1(p)){
                min = this.position.distanceL1(p);
            }
        }
        return min;
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

        if(this.heuristicDeaths == t.heuristicDeaths){
            if(this.heuristicBoxes - t.heuristicBoxes > 0) return 1;
            if(this.heuristicBoxes - t.heuristicBoxes < 0) return -1;
            return 0;
        }

        if(this.heuristicDeaths - t.heuristicDeaths > 0) return 1;
        return -1;
    }

    public String toString(){
        return  "Position: " + this.position + "\n" + "Ships: "  + this.ships + "\n" + "Wrecks: " + this.wrecks;
    }
}
