// Agent basis
/* agent perceives an area that is a square with the size N with
   the agent in the center. Each agent is able to see N*N cells. */

/* Initial beliefs and rules */
/* BELIEFS */


/* RULES */



/* initial DESIRES */
!search.

+!search : not pos(_,_,_)
<- .wait("+pos(_,_,_)");
   !search.
   
+!search : pos(_,_,_)
<- !move;
   /*-pos(_,_,_);*/
   !search.
   
/*+!search : pos(_,_,_) & cow(_,Cx,Cy)
<- +target(Cx,Cy);
   !move.
   
 +!move : pos(X,Y,_) & target(Cx,Cy) & Cx \== X & Cy \== Y
	action.FindPos(X,Y,Cx,Cy,NewX,NewY);
   	action.FindDirection(X,Y,NewX,NewY,Direction);
   	moveTo(Direction);
   	!move. */
   
+!move : pos(X,Y,_) 
<- action.RandomPos(X,Y,NewX,NewY);
   action.FindDirection(X,Y,NewX,NewY,Direction);
   moveTo(Direction);
   !search.
   