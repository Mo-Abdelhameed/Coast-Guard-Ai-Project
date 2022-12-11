# Coast-Guard-Ai-Project
#### Coast Guard is an AI project that aims at implementing a search based agent  whose goal is to maximize the number of people it can save from a dangerous situations.

### A brief discussion of the problem:
For this problem we have a grid of cells with some constant width and height. Sinking ships that contain a few passengers are scattered across the grid. For each time step a life is lost on top of each ship. Moreover, when a ship sinks (All passengers are dead) it becomes a wreck. Each wreck contains a black box that is retrievable with 20 timesteps from the moment the last passenger died in.  The grid also contains at least on station that the passengers can be dropped at. When a passenger reaches a station, he/she is considered safe.
Somewhere on the grid there lies the Coast Guard, it has a fixed capacity and a total of 7 actions that can be performed. 
The actions are:

•	Movement in the four directions (left, right, up and down).

•	Pickup (picks up passengers from a certain ship).

•	Retrieve (retrieves a black box from a wreck).

•	Drop (drops passengers at a station).

Our goal is to develop an agent who aims at maximizing the number of people saved as a priority, then maximizing the retrieved black boxes. We reach a goal state when there are no people left to save and no black boxes to retrieve.

### A discussion of your implementation of the search-tree node ADT:
The State class represents the search-tree node. 

Each State contains the following attributes:

•	Position of the Coast Guard.

•	Positions and number of passengers on all ships.

•	Positions of all wrecks and the black box health for each wreck.

•	Positions of All stations. 

•	The available actions that can be done in this state.

•	Number of saved, dead and remaining people.

•	Number of saved, destroyed and remaining black boxes.

•	The parent (State and action) from which this state arises from.

•	Depth of this state within the search tree.

•	Two heuristic values, one for the passengers and one for the black boxes.

The State class contains the following methods:

•	public boolean isGoalState(): return true iff the state is a goal state.

•	public void setAvailableActions(): Determines the actions that can be performed in this state.

•	public void retrieve(): Retrieves a black box from the current position.

•	public void drop():  Drops the passengers carried by the Coast Guard on a Station.

•	public void move(String direction): Moves the Coast Guard one step in a certain direction.

•	Seven validators to check if a certain action can be taken in the current state or not:

1.	public boolean canMoveLeft()
2.	public boolean canMoveRight()
3.	public boolean canMoveUp()
4.	public boolean canMoveDown()
5.	public boolean canPickUp()
6.	public boolean canDrop()
7.	public boolean canRetrieve()

•	public Point getFarthestItem(Point current, Hashtable<Point, Integer> table): 
returns the position of the farthest ship from the Coast Guard.

•	public double boxCost(): 
Estimates the number of boxes that will be destroyed from this state to the goal state (Admissible).

•	public void farthestShipHeuristic(boolean a_star): 
Considers the farthest ship only, if(a_star = true) returns f(n) + h(n), otherwise returns h(n)

•	public void AllShipsHeuristic(boolean a_star): 
  Considers the all ships, if(a_star = true) returns f(n) + h(n), otherwise returns h(n)

•	public void maxHeuristic(boolean a_star): returns the maximum between the last two methods


### A discussion of your implementation of the Search problem, the Coast Guard problem and the main functions:
The Class Coast Guard contains all the implementation regarding the searching algorithms and updating and manipulating the States.
Coast Guard class has the following static methods:
•	public static State parseGrid(String grid): Converts a string into a State object.

•	public static String GenGrid(): Generates a random state as a String.

•	public static String solve(String grid, String strategy, boolean visualize):
returns the plan resulted from following a certain search strategy.
Possible strategies are (BF, DF, ID, AS1, AS2, GR1, GR2)
When visualize is true, a grid representing each state in the plan is printed.

•	public static String bfs(State initial): returns a plan constructed by BFS search.

•	public static String dfs(State initial): returns a plan constructed by DFS search.

•	public static String iterativeDFs(State initial): returns a plan constructed by Iterative Deepining search.

•	public static String heuristicBased(State initial, boolean a_star): 
returns a plan constructed by A-Star search when a_star is true, otherwise returns a plan constructed by Greedy search.

•	public static String getSolution(State s): returns the path from the root node to the given state.

•	public static ArrayList<State> expand(State parent): 
return ArrayList of all children of the parent state.

•	public static State performAction(State parent, String action):
returns the state resulting from applying the given action on the given state.

•	public static void killPeople(State s): Updates the number of people of each ship.

•	Public static void damageBoxes(State s): Updates the health of each black box for each wreck.

•	public static String computeUtilization(): computes the running time and memory using for each search strategy applied on a search problem.

### A discussion of how you implemented the various search algorithms:
•	BFS: 
1.	A queue is initialized with the initial state in it.
2.	Dequeue a state.
3.	If it’s a goal state, halt and return the path.
4.	Else, expand the state and push the children in the queue(if they are not repeated states).
5.	if the queue is not empty, go to step 2.

•	DFS: Same as BFS but replace the queue with a stack.

•	Iterative Deepening Search: Using a variable to control the depth of Multiple DFS searches.

•	Greedy: Same as BFS but replace the queue with Priority Queue and compare state based on the heuristic value.

•	A-Star: Same as Greedy search but compare states based on cost + heuristic value.

### A discussion of the heuristic functions you employed and, in the case of greedy or A*, an argument for their admissibility:
The first heuristic function (Farthest Ships):
Let D = Distance between Coast Guard and the farthest ship
Let P = Number of passengers in the farthest ship
H1(n) = Min(D, P)
Admissibility Argument:
Suppose that the farthest ship has P passengers and the distance between the Coast Guard and the ship is D. In the best case if Coast Guard decided to move towards this ship and save these passengers right away,  it will need D steps to reach the ship. Thus, if the number of passengers on the ship is > D, then at least D passengers are guaranteed to die. If the number of passengers on the ship is < D, then all passengers on this ship are guaranteed to die. Hence Min(D, P) is an admissible function.
The second heuristic function (All Ships):
Let S be an arbitrary ship
Let D = Distance between Coast Guard and the S
Let P = Number of passengers in S
H2(n) = sum over All S (Min( Ds, Ps ))
Admissibility Argument:
For any arbitrary ship S, let P = number of passengers on S, let D = distance between Coast Guard and S. In the best case (for S) if Coast Guard decided to move towards S, it will need D steps to reach S. Therefore, S is guaranteed to lose Min(D, P).
We did not make any assumptions about S. Thus, the above statement is true for any ship.

The third heuristic function max (H1, H2):

Box heuristic: 
Let B be an arbitrary black box
Let D = distance between Coast Guard and B
Let H = health of B
H(n) = Sum over all (B) such that ( D + H > 19 )
Admissibility Argument:
If a box has health = H and the distance between the Coast Guard and the box is D, by the time the Coast Guard reaches B the health will be D + H. Therefore if D + H > 20, the box is guaranteed to be destroyed.

 ### A discussion of of the performance of the different algorithms implemented in terms of completeness, optimality, RAM usage, CPU utilization, and the number of expanded nodes :

Completeness and Optimality:
  
1)Breadth First Search:
  
●	Completeness: Complete as it always reaches a goal state.
  
●	Optimality: Not optimal as it can lead to a lot of people dying as it is uninformed search that does not put the cost function in consideration.

2)Depth First Search:
  
●	Completeness: Complete as it always reaches a goal state.
  
●	Optimality: Not optimal. The leaf nodes are all goal states and DFS will always pick a leaf node as a goal state, resulting in killing many or all passengers.

3)Iterative Deepening search
  
●	Completeness: Complete as it always reaches a goal state .
  
●	Optimality: Not optimal as it can lead to a lot of people dying as it is uninformed search that does not put the cost function in consideration.

4)Greedy search
  
●	Completeness: Complete as it always reaches a goal state .
  
●	Optimality: Not optimal.

5)A-Star search
  
●	Completeness: Complete as it always reaches a goal state .
  
●	Optimality: Optimal.



Memory Usage and Run Time:
The following screenshots compares the different algorithms on 10 different grids from the public test file.


![alt text](https://github.com/moo3030/Coast-Guard-Ai-Project/blob/main/Results/0.png)


![alt text](https://github.com/moo3030/Coast-Guard-Ai-Project/blob/main/Results/1.png)

![alt text](https://github.com/moo3030/Coast-Guard-Ai-Project/blob/main/Results/2.png)

![alt text](https://github.com/moo3030/Coast-Guard-Ai-Project/blob/main/Results/3.png)

![alt text](https://github.com/moo3030/Coast-Guard-Ai-Project/blob/main/Results/4.png)

![alt text](https://github.com/moo3030/Coast-Guard-Ai-Project/blob/main/Results/5.png)

![alt text](https://github.com/moo3030/Coast-Guard-Ai-Project/blob/main/Results/6.png)

![alt text](https://github.com/moo3030/Coast-Guard-Ai-Project/blob/main/Results/7.png)

![alt text](https://github.com/moo3030/Coast-Guard-Ai-Project/blob/main/Results/8.png)

![alt text](https://github.com/moo3030/Coast-Guard-Ai-Project/blob/main/Results/9.png)


•	Notice that for all grids BFS always has the biggest memory usage followed by iterative deepening search.
•	Iterative Deepening search has the biggest node expansions compared to other algorithms.
•	A-Star with H2 results in less expansions than A-Star with H1 because H2 is dominating H1.


 
References:
1.	java.lang.management.ManagementFactory;
https://docs.oracle.com/javase/7/docs/api/java/lang/management/ManagementFactory.html
2.	java.lang.managment.MemoryMXBean
https://docs.oracle.com/javase/7/docs/api/java/lang/management/MemoryMXBean.html
These packages were used to calculate Memory Usage.

