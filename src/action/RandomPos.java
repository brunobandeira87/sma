package action;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.Term;

import java.util.Random;

public class RandomPos extends DefaultInternalAction 
{

	@Override 
	public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception 
	{ 
		Term x = args[0]; 
		Term y = args[1]; 
		Term newX = args[2]; 
		Term newY = args[3];
		// Unfold terms 
		double vx = ((NumberTerm)x).solve(); 
		double vy = ((NumberTerm)y).solve();
		Random r = new Random(); 
		int nextInt = r.nextInt(2); 
		double vNewX = r.nextBoolean() ? (vx + nextInt) : (vx - nextInt); 
		nextInt = r.nextInt(2); 
		double vNewY = r.nextBoolean() ? (vy + nextInt) : (vy - nextInt); // Update output terms 
		un.unifies(newX, new NumberTermImpl(vNewX)); 
		un.unifies(newY, new NumberTermImpl(vNewY));
		// everything ok, so returns true 
		return true;
	}

}
