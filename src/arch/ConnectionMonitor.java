package arch;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/** checks the connection (using ping) */
class ConnectionMonitor extends Thread {

	private static final int PONG_WAIT_TIME = 4000;

	private long sentTime;
	private int count;
	private boolean ok;

	private Logger logger;
	private ServerProxyThread proxy;
	
	public ConnectionMonitor(Logger logger, ServerProxyThread proxy) {
		this.logger = logger;
		this.proxy = proxy;
	}

	public void run() {
		count = 0;
		ok = true;
		
		int d = new Random().nextInt(10000);
		while (proxy.isRunning()) {
			try {
				if (proxy.isConnected())
					sleep(20000 + d);
				else
					sleep(2000);

				count++;

				ok = false;
				sentTime = System.currentTimeMillis();
				if (proxy.isConnected()) {
					proxy.sendPing("test:" + count);
					waitPong();
				}

				if (!ok) {
					logger.info(" ---> LOOSE connection... reconnecting!");
					proxy.connect(); // reconnect
				}
			} catch (InterruptedException e) {
				logger.log(Level.WARNING, " ---> Error monitoring connection ", e);
			} catch (Exception e) {
				logger.log(Level.WARNING, " ---> Error in communication ", e);
			}
		}
	}

	private synchronized void waitPong() throws Exception {
		wait(PONG_WAIT_TIME);
	}

	protected synchronized void processPong(String pong) {
		long time = System.currentTimeMillis() - sentTime;
		logger.info(" ---> Pong " + pong + " in " + time + " milisec");
		ok = true;
		notify();
	}
}
