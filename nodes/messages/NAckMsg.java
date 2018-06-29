package projects.Chandra_and_Toueg.nodes.messages;

import projects.Chandra_and_Toueg.nodes.nodeImplementations.CTNode;
import sinalgo.nodes.messages.Message;

public class NAckMsg extends Message {
	
	public CTNode node; 
	public int R;
	public NAckMsg(CTNode node, int r) {
		this.node = node;
		this.R = r;
	}

	@Override
	public NAckMsg clone() {
		return new NAckMsg(this.node, this.R);
	}
}
