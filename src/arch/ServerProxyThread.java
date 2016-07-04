package arch;

import jason.asSyntax.Atom;
import jason.asSyntax.Literal;
import jason.asSyntax.LiteralImpl;
import jason.asSyntax.NumberTermImpl;
import jason.environment.grid.Location;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import asl_src.MassimTerms;

class ServerProxyThread extends Thread {

	@SuppressWarnings("serial")
	private class SocketClosedException extends Exception {
	}

	private AbstractAgentArch arch;

	private Logger logger;

	private int networkport;
	private String networkhost;
	private Socket socket;

	private InputStream inputstream;
	private OutputStream outputstream;

	private boolean connected = false;

	private String username;
	private String password;

	private TransformerFactory transformerfactory;
	private DocumentBuilder documentBuilder;

	private boolean running;

	private String responseId; // the response id of the current cycle

	public ServerProxyThread(AbstractAgentArch arq, Logger logger, String host,
			int port, String username, String password) {
		super("ServerProxyThread");

		this.arch = arq;
		this.logger = logger;

		networkhost = "localhost";
		networkport = 0;

		DocumentBuilderFactory documentbuilderfactory = DocumentBuilderFactory.newInstance();
		transformerfactory = TransformerFactory.newInstance();
		try {
			documentBuilder = documentbuilderfactory.newDocumentBuilder();
			// transformer = TransformerFactory.newInstance().newTransformer();
		} catch (Exception e) {
			//e.printStackTrace();
			if (logger.isLoggable(Level.FINEST))
				logger.log(Level.SEVERE, arch.getAgName() + " ---> Server message - XML error", e);
		}

		if (host.startsWith("\"")) {
			host = host.substring(1, host.length() - 1);
		}
		setPort(port);
		setHost(host);
		setUsername(username);
		setPassword(password);
		
		connect();
	}

	/**
	 * Connect and authenticate
	 */
	public boolean connect() {
		connected = false;
		try {
			socket = new Socket(networkhost, networkport);
			inputstream = socket.getInputStream();
			outputstream = socket.getOutputStream();

			sendAuthentication(username, password);
			if (receiveAuthenticationResult()) {
				arch.loggedIn();
				connected = true;
			} else {
				logger.log(Level.SEVERE, arch.getAgName() + " ---> Authentication failed!");
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, arch.getAgName() + " ---> Connection exception: " + e, e);
		}
		logger.log(Level.SEVERE, "sei la: "+connected);
		return connected;
	}

	public void run() {
		running = true;

		while (running) {
			try {
				if (connected) {
					Document doc = receiveDocument();
					if (doc != null) {
						Element el_root = doc.getDocumentElement();

						if (el_root != null) {
							if (el_root.getNodeName().equals("message")) {
								processMessage(el_root);
							} else {
								logger.log(Level.SEVERE,
										"Unknown document received!");
							}
						} else {
							logger.log(Level.SEVERE,
									"No document element found!");
						}
					}
				} else {
					// wait auto-reconnect
					logger.info("waiting reconnection...");
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e1) {
					}
				}
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Server message - exception: " + e, e);
			}
		}
	}

	public String getHost() {
		return networkhost;
	}

	public void setHost(String host) {
		this.networkhost = host;
	}

	public int getPort() {
		return networkport;
	}

	public void setPort(int port) {
		this.networkport = port;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isRunning() {
		return running;
	}

	public boolean isConnected() {
		return connected;
	}

	public void finish() {
		running = false;
	}
	
	// Receiving and Sending

	private Object syncSend = new Object();

	private void sendDocument(Document doc) {
		try {
			Transformer newTransformer = transformerfactory.newTransformer();

			if (logger.isLoggable(Level.FINE)) {
				ByteArrayOutputStream temp = new ByteArrayOutputStream();
				newTransformer.transform(new DOMSource(doc), new StreamResult(temp));

				logger.fine("Sending:" + temp.toString());
			}

			synchronized (syncSend) {
				newTransformer.transform(new DOMSource(doc), new StreamResult(outputstream));
				outputstream.write(0);
				outputstream.flush();
			}
		} catch (TransformerConfigurationException e) {
			logger.log(Level.SEVERE, "transformer config error", e);
		} catch (TransformerException e) {
			logger.log(Level.SEVERE, "transformer error error", e);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "error in sendDocument -- reconnect", e);

			connect();
		}
	}

	private byte[] receivePacket() throws IOException, SocketClosedException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		int read = inputstream.read();
		while (read != 0) {
			if (read == -1) {
				throw new SocketClosedException();
			}
			buffer.write(read);
			read = inputstream.read();
		}
		return buffer.toByteArray();
	}

	private Document receiveDocument() throws SAXException {
		try {
			byte[] raw = receivePacket();
			return documentBuilder.parse(new ByteArrayInputStream(raw));
		} catch (SocketClosedException e) {
			logger.log(Level.SEVERE, "Server message - exception: " + e, e);

			connected = false;
		} catch (SocketException e) {
			logger.log(Level.SEVERE, "Server message - exception: " + e, e);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Server message - exception: " + e, e);
		}
		return null;
	}

	// Authentication

	private void sendAuthentication(String username, String password)
			throws IOException {
		Document doc = documentBuilder.newDocument();
		Element root = doc.createElement("message");
		root.setAttribute("type", "auth-request");
		doc.appendChild(root);
		Element auth = doc.createElement("authentication");
		auth.setAttribute("username", username);
		auth.setAttribute("password", password);
		root.appendChild(auth);
		this.sendDocument(doc);
	}

	private boolean receiveAuthenticationResult() throws IOException {
		try {
			Document doc = receiveDocument();
			Element root = doc.getDocumentElement();
			if (root == null)
				return false;
			if (!root.getAttribute("type").equalsIgnoreCase("auth-response"))
				return false;

			NodeList nl = root.getChildNodes();
			Element authresult = null;
			for (int i = 0; i < nl.getLength(); i++) {
				Node n = nl.item(i);
				if (n.getNodeType() == Element.ELEMENT_NODE
						&& n.getNodeName().equalsIgnoreCase("authentication")) {
					authresult = (Element) n;
					break;
				}
			}
			if (authresult.getAttribute("result").equalsIgnoreCase("ok"))
				return true;
		} catch (Exception e) {
			//e.printStackTrace();
			if (logger.isLoggable(Level.FINEST))
				logger.log(Level.SEVERE, arch.getAgName() + " ---> Server message - XML error", e);
		}
		return false;
	}

	// Server messages

	private boolean processMessage(Element el_message) {
		if (logger.isLoggable(Level.FINE))
			logger.log(Level.INFO, arch.getAgName() + " ---> Server message received:");

		String type = el_message.getAttribute("type");
		boolean isRequestAction = type.equals("request-action");
		boolean isSimStart = type.equals("sim-start");
		boolean isSimEnd = type.equals("sim-end");

		if (isRequestAction || isSimStart || isSimEnd) {

			long deadline = 0;

			long currenttime = 0;
			try {
				currenttime = Long.parseLong(el_message.getAttribute("timestamp"));
			} catch (NumberFormatException e) {
				logger.log(Level.SEVERE, arch.getAgName() + " ---> Server message - Number format invalid", e);
				return true;
			}

			// get perception
			Element el_perception = null;

			NodeList nl = el_message.getChildNodes();

			String infoelementname = "perception";
			if (isRequestAction) /* infoelementname = "perception" */
				;
			else if (isSimStart)
				infoelementname = "simulation";
			else if (isSimEnd)
				infoelementname = "sim-result";

			for (int i = 0; i < nl.getLength(); i++) {
				Node n = nl.item(i);
				if (n.getNodeType() == Element.ELEMENT_NODE
						&& n.getNodeName().equalsIgnoreCase(infoelementname)
						&& el_perception == null) {
					el_perception = (Element) n;
					break;
				}
			}

			if (isRequestAction) {
				try {
					deadline = Long.parseLong(el_perception.getAttribute("deadline"));
				} catch (NumberFormatException e) {
					logger.log(Level.SEVERE, arch.getAgName() + " ---> Server message - Number format invalid", e);
					return true;
				}

				processRequestAction(el_perception, currenttime, deadline);

			} else if (isSimStart) {
				processSimulationStart(el_perception, currenttime);

			} else if (isSimEnd) {
				processSimulationEnd(el_perception, currenttime);
			}

		} else if (type.equals("pong")) {
			NodeList nl = el_message.getChildNodes();
			for (int i = 0; i < nl.getLength(); i++) {
				Node n = nl.item(i);
				if (n.getNodeType() == Element.ELEMENT_NODE
						&& n.getNodeName().equalsIgnoreCase("payload")) {
					arch.processPong(((Element) n).getAttribute("value"));
					return true;
				}
			}
		}
		return true;
	}

	// Simulation

	public void processSimulationStart(Element simulation, long currenttime) {
		try {
			String simId = simulation.getAttribute("id");
			int steps = Integer.parseInt(simulation.getAttribute("steps"));
			int gsizex = Integer.parseInt(simulation.getAttribute("gsizex"));
			int gsizey = Integer.parseInt(simulation.getAttribute("gsizey"));
			int lineOfSight = Integer.parseInt(simulation.getAttribute("lineOfSight"));
			String opponent = simulation.getAttribute("opponent");

			arch.setSimId(simId);

			arch.gsizePerceived(gsizex, gsizey);
			arch.numberOfStepsPerceived(steps);

			arch.lineOfSightPerceived(lineOfSight);

			arch.opponentPerceived(opponent);

			int corralx0 = Integer.parseInt(simulation.getAttribute("corralx0"));
			int corralx1 = Integer.parseInt(simulation.getAttribute("corralx1"));
			int corraly0 = Integer.parseInt(simulation.getAttribute("corraly0"));
			int corraly1 = Integer.parseInt(simulation.getAttribute("corraly1"));
			arch.corralPerceived(new Location(corralx0, corraly0),
					             new Location(corralx1, corraly1));

			logger.info(arch.getAgName() + " ---> Start simulation processed ok!");

			responseId = simId;

		} catch (Exception e) {
			logger.log(Level.SEVERE, arch.getAgName() + " ---> error processing start", e);
		}
	}

	public void processSimulationEnd(Element e_result, long currenttime) {
		try {
			String result = e_result.getAttribute("result");
			String score = e_result.getAttribute("score");
			arch.simulationEndPerceived(score, result);
		} catch (Exception e) {
			logger.log(Level.SEVERE, arch.getAgName() + " ---> error processing end", e);
		}
	}

	private String xmlToString(Node node) {
		try {
			Source source = new DOMSource(node);
			StringWriter stringWriter = new StringWriter();
			Result result = new StreamResult(stringWriter);
			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = factory.newTransformer();
			transformer.transform(source, result);
			return stringWriter.getBuffer().toString();
		} catch (Exception e) {
			// e.printStackTrace();
			if (logger.isLoggable(Level.FINEST))
				logger.log(Level.SEVERE, arch.getAgName() + " ---> Server message - XML error", e);
		}
		return null;
	}

	private void processRequestAction(Element perception, long currenttime, long deadline) {
		if (logger.isLoggable(Level.FINEST))
			logger.info("RequestAction: " + xmlToString(perception));

		List<Literal> percepts = new ArrayList<Literal>();
		try {
			responseId = perception.getAttribute("id");
			int posx = Integer.parseInt(perception.getAttribute("posx"));
			int posy = Integer.parseInt(perception.getAttribute("posy"));

			int step = Integer.parseInt(perception.getAttribute("step"));

			int cowsInCorral = Integer.parseInt(perception.getAttribute("cowsInCorral"));
			
			// location
			Literal posLiteral = new LiteralImpl("pos").addTerms(
					new NumberTermImpl(posx), new NumberTermImpl(posy), new NumberTermImpl(step));
			
			// score
			percepts.add(new LiteralImpl("cowsInCorral").addTerms(new NumberTermImpl(cowsInCorral)));
			
			// add in perception what is around
			NodeList nl = perception.getElementsByTagName("cell");
			for (int i = 0; i < nl.getLength(); i++) {
				Element cell = (Element) nl.item(i);
				int cellx = Integer.parseInt(cell.getAttribute("x")); // relative position
				int celly = Integer.parseInt(cell.getAttribute("y")); // relative position
				int x = posx + cellx;
				int y = posy + celly;

				NodeList cnl = cell.getChildNodes();
				for (int j = 0; j < cnl.getLength(); j++) {
					if (cnl.item(j).getNodeType() == Element.ELEMENT_NODE
							&& cellx != 0 && celly != 0) {

						Element type = (Element) cnl.item(j);
						String nodeName = type.getNodeName();

						if (nodeName.equals("agent")) {
							boolean isAlly = type.getAttribute("type").equals("ally");
							boolean isEnemy = type.getAttribute("type").equals("enemy");
							String agentType = isAlly ||  !isEnemy ? "ally_pos" : "opponent_pos";

							Literal lit = new LiteralImpl(agentType).addTerms(
									new NumberTermImpl(x), new NumberTermImpl(y));
							percepts.add(lit);

							percepts.add(cellLiteral(x, y, isAlly ||  !isEnemy ? MassimTerms.aALLY : MassimTerms.aENEMY));

						} else if (nodeName.equals("cow")) {
							int cowId = Integer.parseInt(type.getAttribute("ID"));

							percepts.add(new LiteralImpl("cow").addTerms(
									new NumberTermImpl(cowId), new NumberTermImpl(x), new NumberTermImpl(y)));

						} else if (nodeName.equals("obstacle")) {
							percepts.add(cellLiteral(x, y, MassimTerms.aOBSTACLE));

						} else if (nodeName.equals("corral")) {
							if (type.getAttribute("type").equals("enemy")) {
//								Literal lit = new LiteralImpl("opponent_corral").addTerms(
//										new NumberTermImpl(x), new NumberTermImpl(y));
								Literal lit = cellLiteral(x, y, MassimTerms.aENEMYCORRAL);
								percepts.add(lit);
							}

						} else if (nodeName.equals("fence")) {
							String status = type.getAttribute("open").equals("true") ? "open" : "closed";

							percepts.add(new LiteralImpl("fence").addTerms(
									new NumberTermImpl(x), new NumberTermImpl(y), new Atom(status)));

						} else if (nodeName.equals("switch")) {
							percepts.add(new LiteralImpl("switch").addTerms(
									new Atom("corral/other"), new NumberTermImpl(x), new NumberTermImpl(y)));

						} else if (nodeName.equals("empty") || nodeName.equals("unknown")) {
							percepts.add(cellLiteral(x, y, MassimTerms.aEMPTY));
						}
					}
				}

				// if (logger.isLoggable(Level.FINEST))
				//     logger.info("Time: " + (System.currentTimeMillis()-start));
			}

			if (logger.isLoggable(Level.FINER))
				logger.info(arch.getAgName() + " ---> Request action for " + posLiteral + " | rid: "
						+ responseId + " | deadline: " + deadline);
			if (logger.isLoggable(Level.FINEST))
				logger.info(arch.getAgName() + " ---> Percepts: " + percepts);

			arch.stepPerceived(step, posLiteral, percepts, deadline);

		} catch (Exception e) {
			logger.log(Level.SEVERE, arch.getAgName() + " ---> error processing request", e);
		}
	}

	private Literal cellLiteral(int absx, int absy, Atom atom) {
		return Literal.parseLiteral("cell").addTerms(
				new NumberTermImpl(absx), new NumberTermImpl(absy), atom);
	}

	public void sendAction(String action) {
		try {
			Document doc = documentBuilder.newDocument();
			Element el_response = doc.createElement("message");

			el_response.setAttribute("type", "action");
			doc.appendChild(el_response);

			Element el_action = doc.createElement("action");
			if (action != null) {
				el_action.setAttribute("type", action);
			}
			el_action.setAttribute("id", responseId);
			el_response.appendChild(el_action);

			sendDocument(doc);
		} catch (Exception e) {
			logger.log(Level.SEVERE, arch.getAgName() + " ---> Server message - Error sending action.", e);
		}
	}

	public void sendPing(String ping) throws IOException {
		Document doc = documentBuilder.newDocument();
		try {
			Element root = doc.createElement("message");
			doc.appendChild(root);
			root.setAttribute("type", "ping");
			Element payload = doc.createElement("payload");
			payload.setAttribute("value", ping);
			root.appendChild(payload);
			sendDocument(doc);
		} catch (Exception e) {
			logger.log(Level.SEVERE, arch.getAgName() + " ---> error seding ping " + e);
		}
	}
}
