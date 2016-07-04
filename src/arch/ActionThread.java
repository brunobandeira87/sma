package arch;

import jason.asSemantics.ActionExec;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

class ActionThread extends Thread {
	private static final int actionTimeout = 1500; // timeout to send an action
	private AbstractAgentArch arch;
	private ServerProxyThread proxy;
	private Logger logger;
	private ActionExec lastAction;
	private String lastAct;
	private String lastActionInCurrentCycle;
	private Queue<ActionExec> toExecute = new ConcurrentLinkedQueue<ActionExec>();
	private Lock lock = new ReentrantLock();
	private Condition cycle = lock.newCondition();
	private long timestartcycle = 0;
	private long timeLastAction = 0;
	private int cycleCounter = 0;

	ActionThread(AbstractAgentArch arch, ServerProxyThread proxy, Logger logger) {
		super("ActionThread");
		this.arch = arch;
		this.proxy = proxy;
		this.logger = logger;
	}

	private void setLastAct(String act) {
		lastAct = act;
	}

	public String getLastAct() {
		return lastAct;
	}

	public void addAction(ActionExec action) {
		lock.lock();
		try {
			if (lastAction != null)
				toExecute.offer(lastAction);
			lastAction = action;
		} finally {
			lock.unlock();
		}
	}

	public void newCycle(int step) {
		finishLastCycle(step);
		timestartcycle = System.currentTimeMillis();
	}

	private void finishLastCycle(int step) {
		cycleCounter++;
		if (step == 1)
			cycleCounter = 1;
		StringBuilder notsent = new StringBuilder();
		// set all actions as successfully executed
		List<ActionExec> feedback = arch.getTS().getC().getFeedbackActions();
		synchronized (feedback) {
			while (!toExecute.isEmpty()) {
				ActionExec action = toExecute.poll();
				action.setResult(true);
				feedback.add(action);
				if (!toExecute.isEmpty())
					notsent.append(action.getActionTerm() + " ");
			}
		}
		go(); // reset the wait
		// prepare msg to print out

		long timetoact = (lastActionInCurrentCycle != null && timestartcycle > 0) ? 0 : (timeLastAction - timestartcycle);
		if (logger.isLoggable(Level.FINER)) {
			logger.info(arch.getAgName() + " --->" + " Sent action " + lastActionInCurrentCycle + " - cycle " + step + " (in " + timetoact + " ms)");
			logger.info(arch.getAgName() + " ---> The following was not sent: " + notsent);
		}
		setLastAct(lastActionInCurrentCycle);
		lastActionInCurrentCycle = null;
	}

	private void go() {
		lock.lock();
		try {
			cycle.signal();
		} finally {
			lock.unlock();
		}
	}

	private boolean waitSleep() throws InterruptedException {
		lock.lock();
		try {
			return !cycle.await(actionTimeout, TimeUnit.MILLISECONDS);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void run() {
		while (true) {
			lastAction = null;
			try {
				waitSleep();
				if (lastAction != null) {
					lastActionInCurrentCycle = lastAction.getActionTerm().getTerm(0).toString();
					proxy.sendAction(lastActionInCurrentCycle);
					if (logger.isLoggable(Level.FINEST))
						logger.info(arch.getAgName() + " ---> Action "
								+ lastActionInCurrentCycle + " sent to server!");
					toExecute.offer(lastAction);
					timeLastAction = System.currentTimeMillis();
				} else {
					if (logger.isLoggable(Level.FINEST))
						logger.info(arch.getAgName()
								+ " ---> NO action sent to server!");
				}
			} catch (InterruptedException e) {
				logger.warning(arch.getAgName() + " -X-> Actuator broke!");
				return; // condition to stop the thread
			} catch (Exception e) {
				toExecute.clear();
			}
		}
	}
}
