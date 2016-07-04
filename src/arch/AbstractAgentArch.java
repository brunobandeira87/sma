package arch;

import java.util.List;

import jason.RevisionFailedException;
import jason.architecture.AgArch;
import jason.asSyntax.Literal;
import jason.environment.grid.Location;

public abstract class AbstractAgentArch extends AgArch {

	abstract void loggedIn();

	abstract void processPong(String attribute);

	abstract void setSimId(String simId);

	abstract void gsizePerceived(int gsizex, int gsizey) throws RevisionFailedException;

	abstract void numberOfStepsPerceived(int steps) throws RevisionFailedException;

	abstract void lineOfSightPerceived(int lineOfSight) throws RevisionFailedException;

	abstract void opponentPerceived(String opponent) throws RevisionFailedException;

	abstract void corralPerceived(Location location, Location location2) throws RevisionFailedException;

	abstract void simulationEndPerceived(String score, String result);

	abstract void stepPerceived(int step, Literal posLiteral,
			List<Literal> percepts, long deadline) throws RevisionFailedException;

}
