

   


/* Initial beliefs and rules */
/* BELIEFS */


/* RULES */



/* initial DESIRES */
!waitSwitch.
!search.

+!waitSwitch : not pos(_,_,_)
	.wait("+pos(_,_,_)");
	.wait("+switch(_,_,_)");
	.!move.
	
+!waitSwitch : pos(X,Y,_) & switch(corral,Cx,Cy)
<- action.FindDirectionDoorman(X,Y,Cx,Cy,Direction);
   moveTo(Direction);
   !waitSwitch.
   
+!search : pos(_,_,_) 
<- !move;
   -pos(_,_,_);
   !search.
   
+!move : pos(X,Y,ID)
<- action.UnknownPos(X,Y,NewX,NewY,ID);
   action.FindDirection(X,Y,NewX,NewY,Direction);
   moveTo(Direction);
   !search.