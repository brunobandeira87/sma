

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
   -pos(_,_,_);
   !search.
   
+!search : pos(X,Y,_) & cow(Cid,Cx,Cy)
<- .broadcast(tell, cow(Cid,Cx,Cy));
   !move.
   
+!search : pos(X,Y,_) & switch(Cid,Cx,Cy)
<- .broadcast(tell, switch(Cid,Cx,Cy));
   !move.

+!search : pos(X,Y,_) & fence(Cid,Cx,Cy)
<- .broadcast(tell, fence(Cid,Cx,Cy));
   !move.
   
+!search : pos(X,Y,_) & corral(Cx,Cy, Type)
<- .broadcast(tell, corral(Cx,Cy, Type));
   !move.
   
+!move : pos(X,Y,ID)
<- action.UnknownPos(X,Y,NewX,NewY,ID);
   action.FindDirection(X,Y,NewX,NewY,Direction);
   moveTo(Direction);
   !search.
   
