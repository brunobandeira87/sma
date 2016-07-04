

/* Initial beliefs and rules */
/* BELIEFS */


/* RULES */



/* initial DESIRES */
!search.
!gotoCow.


  
+!goToCow : pos(X,Y,_) & cow(Cid,Cx,Cy) & corral(CoX, CoY)
<-  action.FindDirection(X,Y,NewX,NewY,Direction); 
	action.PopulateGlobalMap(X, Y, Cx, Cy, Cid, "cow");
	moveTo(Direction);
	!returnCow.
	
+!returnCow : pos(X,Y,_) & cow(Cid,Cx,Cy) & corral(CoX, CoY)
<- action.ReturnCow(X, Y, Cx, Cy, Cid, CoX, CoY, Direction);
	moveTo(Direction);
	!returnCow.

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
   