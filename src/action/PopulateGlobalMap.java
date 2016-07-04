package action;

import map.GlobalMap;
import map.MapObject.MapEntity;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.StringTerm;
import jason.asSyntax.Term;

public class PopulateGlobalMap extends DefaultInternalAction{

	public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception 
	{
		Term X = args[0]; //Minha posi��o X
		Term Y = args[1]; //Minha posi��o Y
		Term Ox = args[2]; //Posi��o do objeto X
		Term Oy = args[3]; //Posi��o do objeto Y
		Term Oid = args[4]; //Id do objeto
		Term Kind = args[4]; //Tipo do objeto
		
		int ox = (int)((NumberTerm)Ox).solve();
		int oy = (int)((NumberTerm)Oy).solve();
		String oid = ((StringTerm)Oid).getString();
		
		String kind = ((StringTerm)Kind).getString();
		switch(kind)
		{
		case "cow":
			GlobalMap.getInstance().set(ox,oy,oid, MapEntity.COW);
			break;
		case "ally":
			GlobalMap.getInstance().set(ox,oy,oid, MapEntity.COW);
			break;
		case "switch":
			GlobalMap.getInstance().set(ox,oy,oid, MapEntity.ALLY_CORRAL_SWITCH);
			break;
		case "corral":
			GlobalMap.getInstance().set(ox,oy,oid, MapEntity.ALLY_CORRAL);
			break;
		}
		
		return true;
	}
}
