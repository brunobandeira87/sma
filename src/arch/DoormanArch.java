package arch;

import jason.JasonException;
import jason.RevisionFailedException;
import jason.asSemantics.ActionExec;
import jason.asSemantics.Agent;
import jason.asSemantics.Message;
import jason.asSyntax.Literal;
import jason.asSyntax.NumberTerm;
import jason.environment.grid.Location;
import jason.mas2j.ClassParameters;
import jason.runtime.Settings;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import map.GlobalMap;
import map.MapObject.MapEntity;

public class DoormanArch extends AbstractAgentArch {
	
	private static int agentIdCounter = 0;

	private Logger logger;
    private ServerProxyThread proxy;
	private ConnectionMonitor monitor;
	private ActionThread actionThread;
	
	private String simId = null;
	private int steps;

	private String teamId;
	private int agId;
	
	private String opponent;

	private int simStep = 0;

	private List<Literal> percepts;

	private Location lastLocation;
	@Override
	public void initAg(String agClass, ClassParameters bbPars, String asSrc, Settings stts) throws JasonException {
		super.initAg(agClass, bbPars, asSrc, stts);
        
		logger = getTS().getLogger();
		logger.setLevel(Level.FINE);
		
		
	    teamId = stts.getUserParameter("teamid");
	    if (teamId == null)
	        logger.info("*** No 'teamid' parameter!!!!");
	    else
	    	teamId = teamId.trim();

		
		String username = stts.getUserParameter("username");
        if (username.startsWith("\""))
        	username = username.substring(1,username.length()-1);
        if (username.endsWith("#nid")) {
        	agId = ++agentIdCounter;
        	username = username.replaceAll("#nid", ""+ agId);
        }

        String password = stts.getUserParameter("password");
        if (password.startsWith("\""))
        	password = password.substring(1,password.length()-1);
        

        String host = stts.getUserParameter("host");
		int port = Integer.parseInt(stts.getUserParameter("port"));
		
		proxy = new ServerProxyThread(this, logger, host, port, username, password);
        monitor = new ConnectionMonitor(logger, proxy);

        actionThread = new ActionThread(this, proxy, logger);
        
        actionThread.start();
        proxy.start();
        monitor.start();
	}

	public int getAgId() {
		return agId;
	}

	void loggedIn() {
		logger.info("---------------- login ----------------");
	}

	void setSimId(String id) {
		this.simId = id;
    }

	private boolean addBel(String literal) throws RevisionFailedException {
		return getTS().getAg().addBel(Literal.parseLiteral(literal));
	}

	private void updateBel(String old, Literal literal) throws RevisionFailedException {
		Agent ag = getTS().getAg();
		ag.delBel(Literal.parseLiteral(old));
        ag.addBel(literal);
	}
	
	void gsizePerceived(int w, int h) throws RevisionFailedException {
		GlobalMap.create(w, h);
        addBel("gsize("+w+","+h+")");
	}

	/**
	 * Perceives the number of simulation steps.
	 * 
	 * @param steps The number of simulation steps
	 * @throws RevisionFailedException
	 */
	void numberOfStepsPerceived(int steps) throws RevisionFailedException {
        addBel("steps("+steps+")");

        this.steps = steps;
	}
	
	void lineOfSightPerceived(int lineOfSight) throws RevisionFailedException {
		addBel("line_of_sight("+lineOfSight+")");
	}
	
	void opponentPerceived(String opponent) throws RevisionFailedException {
        addBel("opponent(" + opponent + ")");
        
        this.opponent = opponent;
	}
	
	void corralPerceived(Location loc, Location loc2) throws RevisionFailedException {
        addBel("corral(" + loc.x + "," + loc.y + "," + loc2.x + "," + loc2.y + ")");
        for(int i = loc.y; i < loc2.y; i++)
        {
        	for(int j = loc.x; j< loc2.x; j++)
        	{
        		GlobalMap.getInstance().set(j, i, "corral", MapEntity.ALLY_CORRAL);
        	}
        }
	}
	
    void stepPerceived(int step, Literal pos, List<Literal> percepts, long deadline)
    		throws RevisionFailedException {
    	if (logger.isLoggable(Level.FINER))
    		logger.info(getAgId() + " --> Step Perceived ");

        this.setSimStep(step);
        updateBel("step(_)", Literal.parseLiteral("step(" + step + ")"));

        this.setLastLocation((int) ((NumberTerm) pos.getTerm(0)).solve(),
        		             (int) ((NumberTerm) pos.getTerm(1)).solve());
        updateBel("pos(_,_,_)", pos);

		this.setPercepts(percepts);
		actionThread.newCycle(this.simStep);
		GlobalMap.getInstance().updateMap();
	}

	protected final void setPercepts(List<Literal> percepts) {
		this.percepts = percepts;
	}
    
    @Override
    public final List<Literal> perceive() {
        if (percepts != null) return percepts;
		return super.perceive();
    }

	public int getSimStep() {
		return simStep;
	}

	private void setSimStep(int simStep) {
		this.simStep = simStep;
	}

	public Location getLastLocation() {
		return lastLocation;
	}

	private void setLastLocation(Location lastLocation) {
		this.lastLocation = lastLocation;
	}
	
	private void setLastLocation(int x, int y) {
		this.setLastLocation(new Location(x, y));
	}
	
	void simulationEndPerceived(String score, String result) {
        logger.info("End of simulation :" + score + " - " + result);
	}

	void processPong(String pong) {
		if (logger.isLoggable(Level.FINEST))
			logger.info(getAgName() + " ---> processPong(" + pong + ")");

		monitor.processPong(pong);
	}
	
	// ACTION

	@Override
	public void act(ActionExec action, List<ActionExec> feedback) {
		logger.info(getAgName() + " ---> Act: " + action.getActionTerm());
		
		if (action.getActionTerm().getFunctor().equals("moveTo")) {
			actionThread.addAction(action);
		} else {
			//super.act(action, feedback);	
		}
		
		try {
			broadcast(new Message());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void stopAg() {
		monitor.interrupt();
		proxy.finish();
		proxy.interrupt();

		super.stopAg();
	}
	
}
