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


/**
 * The class to simulate the sample2-project.
 */
public class CTNode extends Node {
	private static int UNDECIDED = 1;
	private static int DECIDED = 2;

	private int proposedValue = 0; 
	private boolean isLeader = false;
	private boolean isACK = false;
	private boolean isNACK = false;
	private int state = UNDECIDED;
	private int proposeMessages = 0;
	private ProposeValueMsg maxProposeValueMsg;
	private int countAcks = 0;
	private int countNAcks = 0; 

	private int r = 0;
	private int TS = 0;
	private CTNode coordinator;
	
	// TODO GET ALL NODES
	private int N = 5;
	private int halfPlusOne = (this.N/2) + 1;
	
	// TODO POG
	private boolean sentValue = false;
	

	@Override
	public void preStep() {
		this.isLeader = false;
		int coordinator_id = (this.r + 1 % this.N) + 1;
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
		this.r = 0;
		this.TS = 0;
		return true;
	}

	private boolean wantProposeValue() {
		String namespace = "CT/Propose/Want";
		Distribution dist;
        try {
	        dist = Distribution.getDistributionFromConfigFile(namespace + "/Want");
	        System.out.println(dist.nextSample());
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

		//this.r++;
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

		if (this.proposeMessages >= this.halfPlusOne) {
			DecideValueMsg msg = new DecideValueMsg(this, this.maxProposeValueMsg);
			broadcast(msg);
		}
	}
	
	private void receivedDecidedMsg(DecideValueMsg decidedValueMsg) {
		this.isACK = false;
		this.isNACK = false;
		if (decidedValueMsg.node == this.coordinator) {
			this.isACK = true;
			this.proposedValue = decidedValueMsg.value;
			this.TS = decidedValueMsg.R;
			AckMsg ackMsg = new AckMsg(this, this.r); 
			send(ackMsg, this.coordinator);
		} else {
			this.isNACK = true;
			NAckMsg nackMsg = new NAckMsg(this, this.r); 
			send(nackMsg, this.coordinator);
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
		this.isLeader = this.ID == 1;
	}

	@Override
	public void neighborhoodChange() {
		//for(Edge e : this.outgoingConnections){
			//neighbors.add((CTNode) e.endNode); // only adds really new neighbors
		//}
	}

	@Override
	public void postStep() {
	}
	
	@Override
	public String toString() {
		return Integer.toString(this.ID);
	}
	
	public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
		String text = this.toString() + " - PV" + Integer.toString(this.proposedValue);
		
		if (this.coordinator != null) {
			text += " - CI" + this.coordinator.ID;
		}

		if(this.state == DECIDED) {
			this.setColor(Color.PINK);
		} else if(this.isLeader) {
			this.setColor(Color.BLUE);
			text += " - AK" + Integer.toString(this.countAcks);
			text += " - NA" + Integer.toString(this.countNAcks);
			text += " - PM" + Integer.toString(this.proposeMessages);
			if (this.maxProposeValueMsg != null) {
				text += " - MP" + Integer.toString(this.maxProposeValueMsg.value);
			}
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
}
