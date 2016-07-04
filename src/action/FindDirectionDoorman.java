package action;

import agent.DoormanAgent;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Atom;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Term;

public class FindDirectionDoorman extends DefaultInternalAction 
{

	private static final long serialVersionUID = 924527961634812654L;

	@Override
	public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception 
	{ 
		if(!(ts.getAg() instanceof DoormanAgent))
		{
			return false;
		}
		DoormanAgent agent = (DoormanAgent) ts.getAg();
		Term x = args[0]; 
		Term y = args[1]; 
		Term newX = args[2];
		Term newY = args[3];
		Term direction = args[4];
		
		//Posi��o atual
		double vx = ((NumberTerm)x).solve();
		double vy = ((NumberTerm)y).solve();
		
		//Nova posi��o de um switch de curral
		double vNewX = ((NumberTerm)newX).solve();
		double vNewY = ((NumberTerm)newY).solve();
		
		double dist = Math.sqrt(Math.pow(vx - vNewX, 2) + Math.pow(vy - vNewY, 2));
		double distAg = Math.sqrt(Math.pow(vx - agent.getClosestCorralX(), 2) + Math.pow(vy - agent.getClosestCorralY(), 2));
		if(dist < distAg)
		{
			agent.setDist(dist);
			agent.setClosestCorralX(vNewX);
			agent.setClosestCorralX(vNewY);
		}
		
		else
		{
			agent.setDist(distAg);
			vNewX = agent.getClosestCorralX();
			vNewY = agent.getClosestCorralY();
		}
		
		String dir = "";
		
		if(vy < vNewY)
		{
			dir = "north";
		}
		else if(vy > vNewY)
		{
			dir = "south";
		}
		
		if(vx < vNewX)
		{
			dir += "west";
		}
		else if(vx > vNewX)
		{
			dir += "east";
		}

		//Direction result = Direction.valueOf(dir);
		
		un.unifies(direction, new Atom(dir)); 

		return true;
	}
}

