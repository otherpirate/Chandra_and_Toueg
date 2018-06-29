package projects.Chandra_and_Toueg.nodes.messages;

import projects.Chandra_and_Toueg.nodes.nodeImplementations.CTNode;
import sinalgo.nodes.messages.Message;

public class DecideValueMsg extends Message {
	
	public CTNode node; 
	private ProposeValueMsg propose;
	public int R;
	public int TS;
	public int value;
	public DecideValueMsg(CTNode node, ProposeValueMsg propose) {
		this.node = node;
		this.propose = propose;
		this.R = propose.R;
		this.TS = propose.TS;
		this.value = propose.value;
	}

	@Override
	public DecideValueMsg clone() {
		return new DecideValueMsg(this.node, this.propose);
	}
}
