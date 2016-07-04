package action;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.StringTerm;
import jason.asSyntax.Term;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import map.GlobalMap;
import map.MapObject.MapEntity;

public class UnknownPos extends DefaultInternalAction 
{

	private static final long serialVersionUID = 8252157779821757194L;

	@Override 
	public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception 
	{ 
		Term x = args[0]; 
		Term y = args[1]; 
		Term newX = args[2]; 
		Term newY = args[3];
		Term ID = args[4];
		// Unfold terms 
		int vx = (int)((NumberTerm)x).solve(); 
		int vy = (int)((NumberTerm)y).solve();
		
		List<Point> places = new ArrayList<>();
		
		if(GlobalMap.getInstance().get(vx - 1, vy - 1).getEntity() == MapEntity.UNKNOWN)
		{
			places.add(new Point(vx - 1, vy - 1));
		}
		if(GlobalMap.getInstance().get(vx - 1, vy + 1).getEntity() == MapEntity.UNKNOWN)
		{
			places.add(new Point(vx - 1, vy + 1));
		}
		if(GlobalMap.getInstance().get(vx + 1, vy - 1).getEntity() == MapEntity.UNKNOWN)
		{
			places.add(new Point(vx + 1, vy - 1));
		}
		if(GlobalMap.getInstance().get(vx + 1, vy + 1).getEntity() == MapEntity.UNKNOWN)
		{
			places.add(new Point(vx + 1, vy + 1));
		}
		
		if(GlobalMap.getInstance().get(vx, vy - 1).getEntity() == MapEntity.UNKNOWN)
		{
			places.add(new Point(vx, vy - 1));
		}
		if(GlobalMap.getInstance().get(vx, vy + 1).getEntity() == MapEntity.UNKNOWN)
		{
			places.add(new Point(vx, vy + 1));
		}
		if(GlobalMap.getInstance().get(vx - 1, vy).getEntity() == MapEntity.UNKNOWN)
		{
			places.add(new Point(vx - 1, vy));
		}
		if(GlobalMap.getInstance().get(vx + 1, vy).getEntity() == MapEntity.UNKNOWN)
		{
			places.add(new Point(vx + 1, vy));
		}
		
		Random rnd = new Random();
		
		Point destiny = new Point(0,0);
		if(places.size() > 0)
		{
			destiny = places.get(rnd.nextInt(places.size()));
		}
		else
		{
			destiny = new Point(rnd.nextInt(10) - 5, rnd.nextInt(10) - 5);
		}
		
		GlobalMap.getInstance().set(vx, vy, "grass", MapEntity.GRASS);
		GlobalMap.getInstance().set(destiny.x, destiny.y, ID.toString(), MapEntity.ALLY);
		
		int vNewX = destiny.x;
		int vNewY = destiny.y;
		un.unifies(newX, new NumberTermImpl(vNewX)); 
		un.unifies(newY, new NumberTermImpl(vNewY));
		// everything ok, so returns true 
		return true;
	}
}