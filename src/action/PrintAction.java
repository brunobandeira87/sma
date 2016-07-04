package action;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;

public class PrintAction extends DefaultInternalAction 
{

	@Override
	public Object execute(TransitionSystem ts, Unifier un, Term[] args)
			throws Exception 
	{
		Term str = args[0];
		System.out.println(str); // everything ok, so returns true
		return true;
	}

}
