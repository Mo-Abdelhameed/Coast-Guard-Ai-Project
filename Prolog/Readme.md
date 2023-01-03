# Prolog Version

### Explanation of the successor state axioms:

•	state(X, Y2, C, Ships, result(left, S)):-
  state(X, Y, C, Ships, S),
  Y2 is Y-1,
  Y > 0.

•	state(X, Y2, C, Ships, result(right, S)):-
  state(X, Y, C, Ships, S),
  grid(Size, _),
  Y2 is Y+1,
  Y2 < Size.

•	state(X2, Y, C, Ships, result(up, S)):-
  state(X, Y, C, Ships, S),
  X2 is X-1,
  X > 0.

•	state(X2, Y, C, Ships, result(down, S)):-
  state(X, Y, C, Ships, S),
  grid(_, Size),
  X2 is X+1,
  X2 < Size.
  
Arguments:

	X --> The number of the row in which the agent is located.
	Y --> The number of the column in the old cell in which the agent is moving from.
	X2 --> The number of the row in the new cell in which the agent is moving to.
	Y2 --> The number of the column in the new cell in which the agent is moving to.
	C --> The remaining capacity of the agent at this state.
	Ships --> List containing the ships that needs to be rescued at this state.
	S --> The old state which the agent is performing the action on.


The previous four axioms means that, if the agent is at a certain location and it has an adjacent cell in one of the four directions, then this implies that the agent can be in a new state where its location has changed based on the direction of the movement. For each movement in one of the 4 directions, a check is performed on the target cell to see if it exists or not.


The following axiom suggests that, if the current location of the agent contains a ship and the agent does not have a full capacity, then it can deduce a new state in which the capacity of the agent is decreased by one and the ship is considered saved.

•	state(X, Y, C2, Ships1, result(pickup, S)):-
    state(X, Y, C, Ships, S),
    member([X,Y], Ships),
    C \== 0,
    deleteFromList([X,Y], Ships, Ships1),
    C2 is C-1.

Arguments:

	X --> The number of the row in which the agent is located.
	Y --> The number of the column in the old cell in which the agent is moving from.
	C --> The remaining capacity of the agent at the old state.
	C2 --> The remaining capacity of the agent at the new state.
	Ships --> List containing the ships that needs to be rescued at the old state.
	Ships1 --> List containing the ships that needs to be rescued at the new state.
	S --> The old state which the agent is performing the action on.	


The following axiom states that, if the current location of the agent contains a station and the agent is carrying at least one passenger, then it can deduce a new state where it drops the passenger at the station and resets its capacity to the initial value.

•	state(X, Y, Init, Ships, result(drop, S)):-
                state(X, Y, C, Ships, S),
                station(X, Y),
                capacity(Init),
                C \== Init.

Arguments:

	X --> The number of the row in which the agent is located.
	Y --> The number of the column in the old cell in which the agent is moving from.
	C --> The remaining capacity of the agent at the new state.
	Init --> The capacity of the agent at the initial state.
	Ships --> List containing the ships that needs to be rescued at this state.
	S --> The old state which the agent is performing the action on.	

### Description of the fluents:

•	state(X, Y, C, Ships, s0):-
    agent_loc(X, Y),
    capacity(C),
    ships_loc(Ships).

Arguments:

  X, Y --> Location of the agent.
  C --> The remaining capacity of the agent at this state
  Ships --> List containing the ships that needs to be rescued at this state.
  s0 --> The name of the state. (This fluent is written to formulate the initial state)

True iff in state (S) the agent is at location (X, Y) with remaining capacity (C) and remaining ships to rescue (Ships). 

### Helper Fluents:

The following fluent states that, if the agent is at a station and there are no more ships to be rescued, then this state is considered a goal state after performing the “drop” action. Given a state, this fluent returns true if it’s a goal state and false if not.
 
•	goal_helper(result(drop, S)):-
               state(X, Y, _, [], S),
               station(X, Y).

•	ids(result(A, S), L):-
	(call_with_depth_limit(goal_helper(result(A, S)), L, R), number(R));
	(call_with_depth_limit(goal_helper(result(A, S)), L, R), R=depth_limit_exceeded, 
             L1 is  L+1, 
             ids(result(A, S), L1)).

Calls goal_helper with iterative deepening search instead of DFS.


•	deleteFromList(A, B, C).

Arguments: 

	A --> Element to be deleted from a list.
	B --> A List from which element A is to be deleted.
	C --> The list resulted from deleting A from B.

True iff B contains {A} and C is B – {A}.

### Main Fluent:

goal(result(A, S)):-
	ids(result(A, S), 1).

Performs iterative deepening search starting from depth = 1, to look for goal states.
True iff result(A, S) is a goal state.


## Test Cases:

KB1:

![alt text](https://github.com/moo3030/Coast-Guard-Ai-Project/blob/main/Results/KB1.png)
 
Output: 

![alt text](https://github.com/moo3030/Coast-Guard-Ai-Project/blob/main/Results/Out1.png)
 
 
Execution Time: 
	
•	IDS: 2 minutes.

•	Depth limited search: 20 seconds.

KB2:

![alt text](https://github.com/moo3030/Coast-Guard-Ai-Project/blob/main/Results/KB2.png)

Output:

![alt text](https://github.com/moo3030/Coast-Guard-Ai-Project/blob/main/Results/Out2.png)

Execution Time: 
	
•	IDS: 3.7 seconds.

•	Depth limited search: returns instantly.


KB3:

 ![alt text](https://github.com/moo3030/Coast-Guard-Ai-Project/blob/main/Results/KB3.png)

Output:

 ![alt text](https://github.com/moo3030/Coast-Guard-Ai-Project/blob/main/Results/Out3.png)


Execution Time: 

Both IDS and depth limited search returns instantly.

KB4:

![alt text](https://github.com/moo3030/Coast-Guard-Ai-Project/blob/main/Results/KB4.png)
 
Output:

 ![alt text](https://github.com/moo3030/Coast-Guard-Ai-Project/blob/main/Results/Out4.png)


Execution Time: 

•	IDS: 3.3 seconds.

•	Depth limited search: Returns instantly. 
