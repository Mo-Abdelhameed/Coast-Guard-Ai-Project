:- include('KB.pl').
% :- include('KB2.pl').
% :- include('KB3.pl').
% :- include('KB4.pl').


deleteFromList(A, [A|B], B).
    deleteFromList(A, [B, C|D], [B|E]) :-
	    deleteFromList(A, [C|D], E).

state(X, Y, C, Ships, s0):-
    agent_loc(X, Y),
    capacity(C),
    ships_loc(Ships).

state(X, Y2, C, Ships, result(left, S)):-
    state(X, Y, C, Ships, S),
    Y2 is Y-1,
    Y > 0.

state(X, Y2, C, Ships, result(right, S)):-
    state(X, Y, C, Ships, S),
    grid(Size, _),
    Y2 is Y+1,
    Y2 < Size.

state(X2, Y, C, Ships, result(up, S)):-
    state(X, Y, C, Ships, S),
    X2 is X-1,
    X > 0.

state(X2, Y, C, Ships, result(down, S)):-
    state(X, Y, C, Ships, S),
    grid(_, Size),
    X2 is X+1,
    X2 < Size.

state(X, Y, C2, Ships1, result(pickup, S)):-
    state(X, Y, C, Ships, S),
    member([X,Y], Ships),
    C \== 0,
    deleteFromList([X,Y], Ships, Ships1),
    C2 is C-1.

state(X, Y, Init, Ships, result(drop, S)):-
    state(X, Y, C, Ships, S),
    station(X, Y),
    capacity(Init),
    C \== Init.

goal_helper(result(drop, S)):-
    state(X, Y, _, [], S),
    station(X, Y).

goal(result(A, S)):-
	ids(result(A, S), 1).

ids(result(A, S), L):-
	(call_with_depth_limit(goal_helper(result(A, S)), L, R), number(R));
	(call_with_depth_limit(goal_helper(result(A, S)), L, R), R=depth_limit_exceeded, L1 is L+1, ids(result(A, S), L1)).
