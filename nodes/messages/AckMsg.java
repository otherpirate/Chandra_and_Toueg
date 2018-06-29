package projects.Chandra_and_Toueg.nodes.messages;

import projects.Chandra_and_Toueg.nodes.nodeImplementations.CTNode;
import sinalgo.nodes.messages.Message;

public class AckMsg extends Message {
	
	public CTNode node; 
	public int R;
	public AckMsg(CTNode node, int r) {
		this.node = node;
		this.R = r;
	}

	@Override
	public AckMsg clone() {
		return new AckMsg(this.node, this.R);
	}
}
