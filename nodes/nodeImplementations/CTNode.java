package projects.Chandra_and_Toueg.nodes.nodeImplementations;


import java.awt.Color;
import java.awt.Graphics;
import java.util.Iterator;

import projects.Chandra_and_Toueg.nodes.messages.AckMsg;
import projects.Chandra_and_Toueg.nodes.messages.ConfirmationMsg;
import projects.Chandra_and_Toueg.nodes.messages.DecideValueMsg;
import projects.Chandra_and_Toueg.nodes.messages.NAckMsg;
import projects.Chandra_and_Toueg.nodes.messages.ProposeValueMsg;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.nodes.edges.Edge;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.statistics.Distribution;


public class CTNode extends Node {
	public static int UNDECIDED = 1;
	public static int DECIDED = 2;
	public static int ALL_DECIDED = 3;

	public int proposedValue = 0; 
	public boolean isLeader = false;
	public boolean isACK = false;
	public boolean isNACK = false;
	public int state = UNDECIDED;
	public int proposeMessages = 0;
	public ProposeValueMsg maxProposeValueMsg;
	public int countAcks = 0;
	public int countNAcks = 0; 

	private int r = 1;
	private int TS = 0;
	private CTNode coordinator;
	
	public int N;
	public int halfPlusOne;
	
	// TODO POG
	public boolean sentValue = false;
	public boolean hasLeader = false;
	public boolean reset = false;

	@Override
	public void preStep() {
		if (this.reset) {
			this.nextConsensus();
		}
		this.TS++;
		this.isLeader = false;
		int coordinator_id = this.r;
		if (coordinator_id == this.ID) {
			this.isLeader = true; 
			return;
		}
		if (this.sentValue) {
			return;
		}
		if (this.proposedValue == 0) {
			if (!tryToProposeValue()) {
				return;
			}
		}
		for (Iterator<Edge> nodes = outgoingConnections.iterator(); nodes.hasNext();) {
			Edge edge = nodes.next();
			if (edge.endNode.ID == coordinator_id) {
				sendMessageToCoordinator((CTNode)edge.endNode);
				this.sentValue = true;
			}
		}
		// TODO TIMEOUT
	}
	
	private boolean tryToProposeValue() {
		if (!wantProposeValue()) {
			return false;
		}
		this.proposedValue = (int)(Math.random() * 9999) + 1;
		return true;
	}

	private boolean wantProposeValue() {
		String namespace = "CT/Propose/Want";
		Distribution dist;
        try {
	        dist = Distribution.getDistributionFromConfigFile(namespace + "/Want");
	        return (int)dist.nextSample() == 1;
        } catch (CorruptConfigurationEntryException e) {
	        e.printStackTrace();
        	return false;
        }
	}
	
	private void sendMessageToCoordinator(CTNode coordinator) {
		if (this.state == DECIDED) {
			return;
		}

		this.coordinator = coordinator;
		ProposeValueMsg msg = new ProposeValueMsg(this.coordinator, this.r, this.TS, this.proposedValue);
		send(msg, this.coordinator);
	}

	@Override
	public void handleMessages(Inbox inbox) {
		while (inbox.hasNext()) {
			Message msg = inbox.next();
			if (msg instanceof ProposeValueMsg) {
				receivedPropose((ProposeValueMsg) msg);
			}
			else if (msg instanceof DecideValueMsg) {
				receivedDecidedMsg((DecideValueMsg) msg);
			}
			else if (msg instanceof AckMsg) {
				receivedAckMsg((AckMsg) msg);
			}
			else if (msg instanceof AckMsg) {
				receivedNAckMsg((NAckMsg) msg);
			}
			else if (msg instanceof ConfirmationMsg) {
				receivedConfirmation((ConfirmationMsg) msg);
			}
		}
	}
	
	private void receivedPropose(ProposeValueMsg proposeValueMsg) {
		this.proposeMessages += 1;
		if (this.maxProposeValueMsg == null || proposeValueMsg.TS > this.maxProposeValueMsg.TS) {
			this.maxProposeValueMsg = proposeValueMsg;
		}
		decide();
	}
	
	private void decide() {
		if (this.maxProposeValueMsg == null) {
			return;
		}

		if (this.proposeMessages >= this.halfPlusOne) {
			DecideValueMsg msg = new DecideValueMsg(this, this.maxProposeValueMsg);
			broadcast(msg);
		}
	}
	
	private void receivedDecidedMsg(DecideValueMsg decidedValueMsg) {
		this.isACK = false;
		this.isNACK = false;
		if (this.coordinator != null && decidedValueMsg.node == this.coordinator) {
			this.isACK = true;
			this.proposedValue = decidedValueMsg.value;
			this.TS = decidedValueMsg.R;
			AckMsg ackMsg = new AckMsg(this, this.r); 
			send(ackMsg, this.coordinator);
		} else {
			this.isNACK = true;
			NAckMsg nackMsg = new NAckMsg(this, this.r); 
			send(nackMsg, decidedValueMsg.node);
		}
	}
	
	private void receivedAckMsg(AckMsg ackMsg) {
		this.countAcks++;
		handleDecision();
	}
	
	private void receivedNAckMsg(NAckMsg nackMsg) {
		this.countNAcks++;
		handleDecision();
	}
	
	private void handleDecision() {
		if (!this.isLeader) {
			return;
		}
		if (this.countAcks + this.countNAcks >= halfPlusOne) {
			ConfirmationMsg msg = new ConfirmationMsg(this); 
			broadcast(msg);
		}
	}
	
	private void receivedConfirmation(ConfirmationMsg confirmationMsg) {
		this.state = DECIDED;
		this.r = confirmationMsg.node.r;
	}

	@Override
	public void init() {
	}

	@Override
	public void neighborhoodChange() {
		decide();
		handleDecision();
	}
	
	public void nextConsensus() {
		this.r++;
		if (this.r > this.N) {
			this.r = 1;
		}
		this.state = UNDECIDED;
		this.sentValue = false;
		this.isACK = false;
		this.isNACK = false;
		this.proposedValue = 0; 
		this.isLeader = false;
		this.maxProposeValueMsg = null;
		this.proposeMessages = 0;
		this.countAcks = 0;
		this.countNAcks = 0; 
		this.coordinator = null;
		this.reset = false;
	}
	
	@Override
	public String toString() {
		return Integer.toString(this.ID);
	}
	
	public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
		String text = this.toString();

		if(this.isLeader) {
			this.setColor(Color.BLUE);
		} else if(this.state == DECIDED) {
			this.setColor(Color.PINK);
		} else if (this.isACK) {
			this.setColor(Color.GREEN);
		} else if (this.isNACK) {
			this.setColor(Color.RED);
		} else {
			this.setColor(Color.GRAY);
		} 
		super.drawNodeAsSquareWithText(g, pt, highlight, text, 25, Color.BLACK);
	}

	public int compareTo(CTNode tmp) {
		if(this.ID < tmp.ID) {
			return -1;
		}
		if(this.ID > tmp.ID) {
			return 1;
		}
		return 0;
	}
	
	@Override
	public void checkRequirements() throws WrongConfigurationException {
	}

	@Override
	public void postStep() {
	}
}
