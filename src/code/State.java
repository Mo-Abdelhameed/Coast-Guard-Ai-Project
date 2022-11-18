package code;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class State implements Cloneable{

    Point position;
    HashMap<Point, Integer> ships;
    HashMap<Point, Integer> wrecks;
    HashSet<Point> stations;
    ArrayList<String> availableActions = new ArrayList<>();
    State parent;
    int remainingCapacity;
    int savedPeople;
    int deadPeople;
    int savedBoxes;
    int destroyedBoxes;
    int survivingPeople;
    int remainingBoxes;

    public State(Point position, int survivingPeople, HashMap<Point, Integer> ships,
                 HashMap<Point, Integer> wrecks, HashSet<Point> stations) {
        this.position = position;
        this.ships = ships;
        this.wrecks = wrecks;
        this.stations = stations;
        this.parent = null;
        this.remainingCapacity = CoastGuard.capacity;
        this.savedBoxes = 0;
        this.savedPeople = 0;
        this.deadPeople = 0;
        this.destroyedBoxes = 0;
        this.survivingPeople = survivingPeople;
        this.remainingBoxes = 0;
        setAvailableActions();
    }

    public void expand(){

    }

    public boolean isGoalState(){
        return survivingPeople == 0 && remainingBoxes == 0;
    }

    public void setAvailableActions(){

        if(canDrop())
            availableActions.add("drop");

        if(canRetrieve())
            availableActions.add("retrieve");

        if(canPickUp())
            availableActions.add("pickup");

        if(canMoveLeft())
            availableActions.add("left");

        if(canMoveDown())
            availableActions.add("down");

        if(canMoveRight())
            availableActions.add("right");

        if(canMoveUp())
            availableActions.add("up");
    }

    public boolean canMoveLeft(){
        return position.getY() > 0;
    }

    public boolean canMoveRight(){
        return position.getY() < CoastGuard.m;
    }

    public boolean canMoveUp(){
        return position.getX() > 0;
    }

    public boolean canMoveDown(){
        return position.getX() < CoastGuard.n;
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

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
